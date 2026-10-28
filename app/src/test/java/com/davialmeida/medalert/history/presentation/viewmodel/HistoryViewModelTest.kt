package com.davialmeida.medalert.history.presentation.viewmodel

import com.davialmeida.medalert.history.domain.model.HistoryPeriod
import com.davialmeida.medalert.history.domain.usecase.ObserveHistoryMedicationsUseCase
import com.davialmeida.medalert.history.domain.usecase.ObserveHistoryUseCase
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.medication.domain.usecase.FakeMedicationRepository
import com.davialmeida.medalert.medication.domain.usecase.sampleMedication
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import com.davialmeida.medalert.profile.domain.usecase.ObserveActiveProfileUseCase
import com.davialmeida.medalert.testutil.MainDispatcherRule
import com.davialmeida.medalert.testutil.TestProfileRepository
import com.davialmeida.medalert.today.domain.error.DoseError
import com.davialmeida.medalert.today.domain.usecase.FakeDoseLogRepository
import com.davialmeida.medalert.today.domain.usecase.FakeDoseRegistrationListener
import com.davialmeida.medalert.today.domain.usecase.MarkDoseUseCase
import com.davialmeida.medalert.today.domain.usecase.UndoDoseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val date = LocalDate.of(2026, 10, 16)
    private val clock = Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC)
    private val profile = Profile(1, "Pessoal", ProfileColor.BLUE)

    private val fakeMedicationRepository = FakeMedicationRepository()
    private val doseLogRepository = FakeDoseLogRepository()

    private fun viewModel(
        medicationRepository: MedicationRepository = fakeMedicationRepository,
        profiles: List<Profile> = listOf(profile)
    ): HistoryViewModel {
        val profileRepository = TestProfileRepository(profiles)

        return HistoryViewModel(
            observeHistoryUseCase = ObserveHistoryUseCase(
                profileRepository,
                medicationRepository,
                doseLogRepository,
                clock
            ),
            observeHistoryMedicationsUseCase = ObserveHistoryMedicationsUseCase(medicationRepository),
            observeActiveProfileUseCase = ObserveActiveProfileUseCase(profileRepository),
            markDoseUseCase = MarkDoseUseCase(
                medicationRepository = medicationRepository,
                doseLogRepository = doseLogRepository,
                registrationListener = FakeDoseRegistrationListener(),
                clock = clock
            ),
            undoDoseUseCase = UndoDoseUseCase(doseLogRepository)
        )
    }

    @Test
    fun `loads the last seven days of the active profile`() = runTest {
        fakeMedicationRepository.data.add(sampleMedication(start = date.minusDays(20)))

        val state = viewModel().uiState.value

        assertFalse(state.isLoading)
        assertFalse(state.hasLoadError)
        assertEquals(HistoryPeriod.LAST_7_DAYS, state.period)
        assertEquals(7, state.days.size)
        assertEquals(date, state.today)
        assertEquals(profile, state.activeProfile)
    }

    @Test
    fun `selecting thirty days loads a longer history`() = runTest {
        fakeMedicationRepository.data.add(sampleMedication(start = date.minusDays(20)))
        val viewModel = viewModel()

        viewModel.selectPeriod(HistoryPeriod.LAST_30_DAYS)

        assertEquals(21, viewModel.uiState.value.days.size)
    }

    @Test
    fun `filtering by medication keeps only that medication`() = runTest {
        fakeMedicationRepository.data.add(sampleMedication(id = 1, name = "A"))
        fakeMedicationRepository.data.add(sampleMedication(id = 2, name = "B"))
        val viewModel = viewModel()

        viewModel.selectSeries(2L)

        val names = viewModel.uiState.value.days
            .flatMap { day -> day.items.map { it.medication.name } }
            .toSet()
        assertEquals(setOf("B"), names)
    }

    @Test
    fun `filter goes back to all when the medication disappears`() = runTest {
        val medicationA = sampleMedication(id = 1, name = "A")
        val medicationB = sampleMedication(id = 2, name = "B")
        val medications = MutableStateFlow(listOf(medicationA, medicationB))

        val repository = object : MedicationRepository by FakeMedicationRepository() {
            override fun observeByProfile(profileId: Long): Flow<List<Medication>> = medications
        }
        val viewModel = viewModel(medicationRepository = repository)

        viewModel.selectSeries(2L)
        assertEquals(2L, viewModel.uiState.value.selectedSeriesId)

        medications.value = listOf(medicationA)

        val state = viewModel.uiState.value
        assertNull(state.selectedSeriesId)
        assertEquals(listOf("A"), state.medications.map { it.name })
    }

    @Test
    fun `marking a late dose as taken raises the adherence`() = runTest {
        fakeMedicationRepository.data.add(sampleMedication(start = date.minusDays(1)))
        val viewModel = viewModel()

        assertEquals(0, viewModel.uiState.value.summary.taken)

        viewModel.markTaken(1L, date.minusDays(1).atTime(8, 0))

        val summary = viewModel.uiState.value.summary
        assertEquals(1, summary.taken)
        assertEquals(1, summary.late)
        assertEquals(50, summary.percentage)
    }

    @Test
    fun `marking a future dose sets an action error`() = runTest {
        fakeMedicationRepository.data.add(sampleMedication())
        val viewModel = viewModel()

        viewModel.markTaken(1L, date.plusDays(1).atTime(8, 0))
        assertEquals(DoseError.FutureDose, viewModel.uiState.value.actionError)

        viewModel.clearActionError()
        assertNull(viewModel.uiState.value.actionError)
    }

    @Test
    fun `without profiles the history is empty`() = runTest {
        val state = viewModel(profiles = emptyList()).uiState.value

        assertFalse(state.isLoading)
        assertNull(state.activeProfile)
        assertTrue(state.days.isEmpty())
    }

    @Test
    fun `a repository failure shows the load error`() = runTest {
        val failing = object : MedicationRepository by FakeMedicationRepository() {
            override fun observeByProfile(profileId: Long): Flow<List<Medication>> =
                flow { throw IllegalStateException("boom") }
        }

        val state = viewModel(medicationRepository = failing).uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.hasLoadError)
    }
}