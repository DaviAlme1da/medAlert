package com.davialmeida.medalert.reminder.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.usecase.FakeMedicationRepository
import com.davialmeida.medalert.medication.domain.usecase.sampleMedication
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.time.LocalTime

class RescheduleRemindersUseCaseTest {

    private val medicationRepository = FakeMedicationRepository()
    private val scheduler = FakeReminderScheduler()

    private fun useCase(now: String): RescheduleRemindersUseCase =
        RescheduleRemindersUseCase(
            medicationRepository,
            scheduler,
            Clock.fixed(Instant.parse(now), ZoneOffset.UTC)
        )

    private fun daily(id: Long, vararg times: LocalTime) = sampleMedication(
        id = id,
        frequency = Frequency.Daily(times.toList())
    )

    @Test
    fun `schedules the next dose of the day`() = runTest {
        medicationRepository.data.add(daily(1, LocalTime.of(8, 0)))

        val result = useCase("2026-10-16T07:00:00Z")()

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(
            listOf(Instant.parse("2026-10-16T08:00:00Z")),
            scheduler.scheduled
        )
        assertEquals(0, scheduler.cancelCount)
    }

    @Test
    fun `schedules the earliest dose among medications`() = runTest {
        medicationRepository.data.add(daily(1, LocalTime.of(20, 0)))
        medicationRepository.data.add(daily(2, LocalTime.of(9, 0)))

        useCase("2026-10-16T08:00:00Z")()

        assertEquals(
            listOf(Instant.parse("2026-10-16T09:00:00Z")),
            scheduler.scheduled
        )
    }

    @Test
    fun `schedules tomorrow when today's doses have passed`() = runTest {
        medicationRepository.data.add(daily(1, LocalTime.of(8, 0)))

        useCase("2026-10-16T09:00:00Z")()

        assertEquals(
            listOf(Instant.parse("2026-10-17T08:00:00Z")),
            scheduler.scheduled
        )
    }

    @Test
    fun `chains the next alarm right after one fires`() = runTest {
        medicationRepository.data.add(daily(1, LocalTime.of(8, 0), LocalTime.of(14, 0)))

        useCase("2026-10-16T08:00:20Z")()

        assertEquals(
            listOf(Instant.parse("2026-10-16T14:00:00Z")),
            scheduler.scheduled
        )
    }

    @Test
    fun `cancels the alarm when there are no medications with reminders`() = runTest {
        val result = useCase("2026-10-16T07:00:00Z")()

        assertEquals(AppResult.Success(Unit), result)
        assertTrue(scheduler.scheduled.isEmpty())
        assertEquals(1, scheduler.cancelCount)
    }

    @Test
    fun `cancels the alarm when reminders are disabled`() = runTest {
        medicationRepository.data.add(
            daily(1, LocalTime.of(8, 0)).copy(remindersEnabled = false)
        )

        useCase("2026-10-16T07:00:00Z")()

        assertTrue(scheduler.scheduled.isEmpty())
        assertEquals(1, scheduler.cancelCount)
    }

    @Test
    fun `returns an error when the scheduler fails`() = runTest {
        medicationRepository.data.add(daily(1, LocalTime.of(8, 0)))
        scheduler.failOnSchedule = true

        val result = useCase("2026-10-16T07:00:00Z")()

        assertEquals(AppResult.Error(UnexpectedAppError), result)
    }
}