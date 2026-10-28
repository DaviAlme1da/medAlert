package com.davialmeida.medalert.history.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.history.domain.model.HistoryPeriod
import com.davialmeida.medalert.history.domain.model.HistoryResult
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.medication.domain.usecase.FakeMedicationRepository
import com.davialmeida.medalert.medication.domain.usecase.sampleMedication
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import com.davialmeida.medalert.testutil.TestProfileRepository
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import com.davialmeida.medalert.today.domain.usecase.FakeDoseLogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveHistoryUseCaseTest {

    private val date = LocalDate.of(2026, 10, 16)
    private val clock = Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC)
    private val profile = Profile(1, "Pessoal", ProfileColor.BLUE)

    private val medicationRepository = FakeMedicationRepository()
    private val doseLogRepository = FakeDoseLogRepository()

    private fun useCase(
        profiles: List<Profile> = listOf(profile),
        repository: MedicationRepository = medicationRepository
    ) = ObserveHistoryUseCase(
        TestProfileRepository(profiles),
        repository,
        doseLogRepository,
        clock
    )

    private fun takenLog(medicationId: Long, dateTime: LocalDateTime) = DoseLog(
        id = 0,
        medicationId = medicationId,
        scheduledDateTime = dateTime,
        status = DoseLogStatus.TAKEN,
        loggedAt = Instant.parse("2026-10-16T09:00:00Z"),
        takenAt = Instant.parse("2026-10-16T09:00:00Z")
    )

    @Test
    fun `without an active profile the history is empty`() = runTest {
        val result = useCase(profiles = emptyList())().first() as AppResult.Success

        assertEquals(emptyList<Any>(), result.data.days)
        assertNull(result.data.summary.percentage)
        assertEquals(date, result.data.today)
    }

    @Test
    fun `builds the days and the summary of the active profile`() = runTest {
        medicationRepository.data.add(sampleMedication(start = date.minusDays(2)))
        doseLogRepository.addLog(takenLog(1, date.minusDays(2).atTime(8, 0)))

        val result = useCase()().first() as AppResult.Success

        assertEquals(3, result.data.days.size)
        assertEquals(1, result.data.summary.taken)
        assertEquals(2, result.data.summary.late)
    }

    @Test
    fun `uses the requested period`() = runTest {
        medicationRepository.data.add(sampleMedication(start = date.minusDays(20)))

        val result = useCase()(HistoryPeriod.LAST_30_DAYS).first() as AppResult.Success

        assertEquals(21, result.data.days.size)
    }

    @Test
    fun `filters by series`() = runTest {
        medicationRepository.data.add(sampleMedication(id = 1, name = "A"))
        medicationRepository.data.add(sampleMedication(id = 2, name = "B"))

        val result = useCase()(seriesId = 2L).first() as AppResult.Success

        val names = result.data.days.flatMap { day -> day.items.map { it.medication.name } }.toSet()
        assertEquals(setOf("B"), names)
    }

    @Test
    fun `emits again when a log is added`() = runTest {
        medicationRepository.data.add(sampleMedication(start = date.minusDays(1)))
        val results = mutableListOf<AppResult<HistoryResult>>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase()().toList(results)
        }
        doseLogRepository.addLog(takenLog(1, date.minusDays(1).atTime(8, 0)))

        assertEquals(2, results.size)
        assertEquals(0, (results.first() as AppResult.Success).data.summary.taken)
        assertEquals(1, (results.last() as AppResult.Success).data.summary.taken)
    }

    @Test
    fun `a repository failure becomes an error result`() = runTest {
        val failing = object : MedicationRepository by FakeMedicationRepository() {
            override fun observeByProfile(profileId: Long): Flow<List<Medication>> =
                flow { throw IllegalStateException("boom") }
        }

        val result = useCase(repository = failing)().first()

        assertEquals(AppResult.Error(UnexpectedAppError), result)
    }
}