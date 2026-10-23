package com.davialmeida.medalert.reminder.domain.rules

import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.usecase.sampleMedication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderRulesTest {

    private val date = LocalDate.of(2026, 10, 16)

    private fun medication(
        id: Long,
        vararg times: LocalTime,
        enabled: Boolean = true
    ): Medication = sampleMedication(
        id = id,
        name = "Med$id",
        frequency = Frequency.Daily(times.toList())
    ).copy(remindersEnabled = enabled)

    private fun at(hour: Int, minute: Int = 0, second: Int = 0): LocalDateTime =
        date.atTime(hour, minute, second)


    @Test
    fun `nextReminderAfter returns null without medications`() {
        assertNull(nextReminderAfter(emptyList(), at(7, 0)))
    }

    @Test
    fun `nextReminderAfter returns the earliest dose among medications`() {
        val result = nextReminderAfter(
            listOf(
                medication(1, LocalTime.of(20, 0)),
                medication(2, LocalTime.of(9, 0))
            ),
            at(8, 0)
        )

        assertEquals(at(9, 0), result)
    }

    @Test
    fun `nextReminderAfter ignores medications with reminders disabled`() {
        val result = nextReminderAfter(
            listOf(
                medication(1, LocalTime.of(8, 30), enabled = false),
                medication(2, LocalTime.of(12, 0))
            ),
            at(8, 0)
        )

        assertEquals(at(12, 0), result)
    }

    @Test
    fun `nextReminderAfter returns null when every reminder is disabled`() {
        val result = nextReminderAfter(
            listOf(medication(1, LocalTime.of(8, 30), enabled = false)),
            at(8, 0)
        )

        assertNull(result)
    }

    @Test
    fun `nextReminderAfter moves to tomorrow after the last dose of the day`() {
        val result = nextReminderAfter(
            listOf(medication(1, LocalTime.of(8, 0))),
            at(9, 0)
        )

        assertEquals(date.plusDays(1).atTime(8, 0), result)
    }

    @Test
    fun `nextReminderAfter ignores ended medications`() {
        val ended = sampleMedication(end = date.minusDays(1))

        assertNull(nextReminderAfter(listOf(ended), at(7, 0)))
    }

    @Test
    fun `dosesInWindow includes the dose scheduled exactly at now`() {
        val doses = dosesInWindow(
            medication(1, LocalTime.of(8, 0)),
            at(8, 0),
            REMINDER_WINDOW_MINUTES
        )

        assertEquals(listOf(at(8, 0)), doses.map { it.scheduledDateTime })
    }

    @Test
    fun `dosesInWindow includes a slightly delayed alarm`() {
        val doses = dosesInWindow(
            medication(1, LocalTime.of(8, 0)),
            at(8, 0, 30),
            REMINDER_WINDOW_MINUTES
        )

        assertEquals(1, doses.size)
    }

    @Test
    fun `dosesInWindow excludes a dose older than the window`() {
        val doses = dosesInWindow(
            medication(1, LocalTime.of(8, 0)),
            at(8, 1),
            REMINDER_WINDOW_MINUTES
        )

        assertTrue(doses.isEmpty())
    }

    @Test
    fun `dosesInWindow excludes a dose in the future`() {
        val doses = dosesInWindow(
            medication(1, LocalTime.of(8, 0)),
            at(7, 59),
            REMINDER_WINDOW_MINUTES
        )

        assertTrue(doses.isEmpty())
    }

    @Test
    fun `dosesInWindow returns empty when reminders are disabled`() {
        val doses = dosesInWindow(
            medication(1, LocalTime.of(8, 0), enabled = false),
            at(8, 0),
            REMINDER_WINDOW_MINUTES
        )

        assertTrue(doses.isEmpty())
    }

    @Test
    fun `dosesInWindow returns empty for a non positive window`() {
        val doses = dosesInWindow(
            medication(1, LocalTime.of(8, 0)),
            at(8, 0),
            0
        )

        assertTrue(doses.isEmpty())
    }

    @Test
    fun `dosesInWindow finds yesterday's dose across midnight`() {
        val doses = dosesInWindow(
            medication(1, LocalTime.of(23, 59)),
            date.plusDays(1).atTime(0, 2),
            windowMinutes = 5
        )

        assertEquals(listOf(date.atTime(23, 59)), doses.map { it.scheduledDateTime })
    }

    @Test
    fun `dosesInWindow returns several doses sorted by time`() {
        val doses = dosesInWindow(
            medication(1, LocalTime.of(8, 1), LocalTime.of(8, 0)),
            at(8, 2),
            windowMinutes = 5
        )

        assertEquals(
            listOf(at(8, 0), at(8, 1)),
            doses.map { it.scheduledDateTime }
        )
    }
}