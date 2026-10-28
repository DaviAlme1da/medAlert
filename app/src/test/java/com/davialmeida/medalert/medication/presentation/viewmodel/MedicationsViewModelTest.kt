package com.davialmeida.medalert.medication.presentation.viewmodel

import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.medication.domain.usecase.FakeMedicationRepository
import com.davialmeida.medalert.medication.domain.usecase.ObserveMedicationsUseCase
import com.davialmeida.medalert.medication.domain.usecase.sampleMedication
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import com.davialmeida.medalert.profile.domain.usecase.ObserveActiveProfileUseCase
import com.davialmeida.medalert.testutil.MainDispatcherRule
import com.davialmeida.medalert.testutil.TestProfileRepository
import kotlinx.coroutines.flow.Flow
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
import java.time.LocalTime
import java.time.ZoneOffset

class MedicationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val date = LocalDate.of(2026, 10, 16)
    private val clock = Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC)
    private val profile = Profile(1, "Pessoal", ProfileColor.BLUE)

    private val fakeRepository = FakeMedicationRepository()

    private fun viewModel(
        repository: MedicationRepository = fakeRepository,
        profiles: List<Profile> = listOf(profile)
    ): MedicationsViewModel {
        val profileRepository = TestProfileRepository(profiles)

        return MedicationsViewModel(
            observeActiveProfileUseCase = ObserveActiveProfileUseCase(profileRepository),
            observeMedicationsUseCase = ObserveMedicationsUseCase(repository),
            clock = clock
        )
    }

    @Test
    fun `without a profile both lists are empty`() = runTest {
        val state = viewModel(profiles = emptyList()).uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.activeMedications.isEmpty())
        assertTrue(state.pausedMedications.isEmpty())
    }

    @Test
    fun `splits active and paused medications`() = runTest {
        fakeRepository.data.add(sampleMedication(id = 1, name = "Ativo"))
        fakeRepository.data.add(
            sampleMedication(
                id = 2,
                name = "Pausado",
                start = date.minusDays(10),
                end = date.minusDays(1)
            )
        )

        val state = viewModel().uiState.value

        assertFalse(state.isLoading)
        assertEquals(listOf("Ativo"), state.activeMedications.map { it.medication.name })
        assertEquals(listOf("Pausado"), state.pausedMedications.map { it.medication.name })
    }

    @Test
    fun `active medication shows the next dose of today`() = runTest {
        fakeRepository.data.add(
            sampleMedication(frequency = Frequency.Daily(listOf(LocalTime.of(20, 0))))
        )

        val item = viewModel().uiState.value.activeMedications.single()

        assertEquals(date.atTime(20, 0), item.nextDose)
        assertTrue(item.nextDoseIsToday)
    }

    @Test
    fun `next dose moves to tomorrow when today's doses have passed`() = runTest {
        fakeRepository.data.add(sampleMedication())

        val item = viewModel().uiState.value.activeMedications.single()

        assertEquals(date.plusDays(1).atTime(8, 0), item.nextDose)
        assertFalse(item.nextDoseIsToday)
    }

    @Test
    fun `paused medication has no next dose`() = runTest {
        fakeRepository.data.add(
            sampleMedication(start = date.minusDays(10), end = date.minusDays(1))
        )

        val item = viewModel().uiState.value.pausedMedications.single()

        assertNull(item.nextDose)
    }

    @Test
    fun `only the latest version of a medication is listed`() = runTest {
        fakeRepository.data.add(
            sampleMedication(
                id = 1,
                seriesId = 1,
                start = date.minusDays(10),
                end = date.minusDays(3)
            )
        )
        fakeRepository.data.add(
            sampleMedication(id = 2, seriesId = 1, start = date.minusDays(2))
        )

        val state = viewModel().uiState.value

        assertEquals(listOf(2L), state.activeMedications.map { it.medication.id })
        assertTrue(state.pausedMedications.isEmpty())
    }

    @Test
    fun `a repository failure sets the error state`() = runTest {
        val failing = object : MedicationRepository by FakeMedicationRepository() {
            override fun observeByProfile(profileId: Long): Flow<List<Medication>> =
                flow { throw IllegalStateException("boom") }
        }

        val state = viewModel(repository = failing).uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.hasError)
    }
}