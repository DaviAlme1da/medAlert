package com.davialmeida.medalert.reminder.domain.rules

import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.rules.nextDoseAfter
import com.davialmeida.medalert.today.domain.model.Dose
import com.davialmeida.medalert.today.domain.rules.calculateDosesForDate
import java.time.LocalDateTime

const val REMINDER_WINDOW_MINUTES = 1L

fun nextReminderAfter(
    medications: List<Medication>,
    after: LocalDateTime
): LocalDateTime? {
    return medications
        .asSequence()
        .filter { it.remindersEnabled }
        .mapNotNull { medication ->
            nextDoseAfter(medication, after)
        }
        .minOrNull()
}

fun dosesInWindow(
    medication: Medication,
    now: LocalDateTime,
    windowMinutes: Long
): List<Dose> {
    if (!medication.remindersEnabled || windowMinutes <= 0) {
        return emptyList()
    }

    val windowStart = now.minusMinutes(windowMinutes)
    val yesterday = now.toLocalDate().minusDays(1)
    val today = now.toLocalDate()

    return listOf(yesterday, today)
        .flatMap { date ->
            calculateDosesForDate(medication, date)
        }
        .filter { dose ->
            dose.scheduledDateTime > windowStart &&
                    dose.scheduledDateTime <= now
        }
        .distinctBy { it.medicationId to it.scheduledDateTime }
        .sortedBy { it.scheduledDateTime }
}