package com.davialmeida.medalert.reminder.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.settings.domain.model.AppSettings
import com.davialmeida.medalert.testutil.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class SnoozeReminderUseCaseTest {

    private val nowInstant = Instant.parse("2026-10-16T08:00:00Z")
    private val clock = Clock.fixed(nowInstant, ZoneOffset.UTC)
    private val dose = LocalDateTime.of(2026, 10, 16, 8, 0)

    private val settings = FakeSettingsRepository(AppSettings(snoozeMinutes = 15))
    private val scheduler = FakeSnoozeScheduler()
    private val useCase = SnoozeReminderUseCase(settings, scheduler, clock)

    @Test
    fun `schedules the snooze after the configured minutes`() = runTest {
        val result = useCase(1L, dose)

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(
            listOf(ScheduledSnooze(1L, dose, nowInstant.plusSeconds(15 * 60))),
            scheduler.scheduled
        )
    }

    @Test
    fun `uses the default duration when nothing was changed`() = runTest {
        val default = SnoozeReminderUseCase(FakeSettingsRepository(), scheduler, clock)

        default(1L, dose)

        assertEquals(nowInstant.plusSeconds(10 * 60), scheduler.scheduled.single().triggerAt)
    }

    @Test
    fun `a scheduler failure becomes an error`() = runTest {
        scheduler.failOnSchedule = true

        val result = useCase(1L, dose)

        assertEquals(AppResult.Error(UnexpectedAppError), result)
        assertTrue(scheduler.scheduled.isEmpty())
    }
}