
package com.davialmeida.medalert.today.domain.rules

import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.today.domain.model.DayProgress
import com.davialmeida.medalert.today.domain.model.DaySchedule
import com.davialmeida.medalert.today.domain.model.DoseItem
import com.davialmeida.medalert.today.domain.model.DoseStatus
import java.time.LocalDate
import java.time.LocalDateTime

fun buildDoseItems(
    schedule: DaySchedule,
    date: LocalDate,
    now: LocalDateTime
): List<DoseItem> {
    return schedule.medications
        .flatMap { medication ->
            calculateDosesForDate(medication, date).map { dose ->
                val log = schedule.logs.firstOrNull {
                    it.medicationId == dose.medicationId &&
                            it.scheduledDateTime == dose.scheduledDateTime
                }

                val status = if (date.isAfter(now.toLocalDate())) {
                    DoseStatus.PENDING
                } else {
                    deriveDoseStatus(
                        dose = dose,
                        doseLog = log,
                        now = now
                    )
                }

                DoseItem(
                    medication = medication,
                    scheduledDateTime = dose.scheduledDateTime,
                    log = log,
                    status = status
                )
            }
        }
        .sortedWith(
            compareBy<DoseItem> { it.scheduledDateTime }
                .thenBy { it.medication.name }
        )
}

fun calculateProgress(items: List<DoseItem>): DayProgress {
    return DayProgress(
        taken = items.count { it.status == DoseStatus.TAKEN },
        total = items.size
    )
}


fun isDoseOf(
    medication: Medication,
    dateTime: LocalDateTime
): Boolean {
    val date = dateTime.toLocalDate()

    if (!medication.isActiveOn(date)) {
        return false
    }

    return medication.frequency
        .calculateTimesForDate(date)
        .contains(dateTime.toLocalTime())
}
