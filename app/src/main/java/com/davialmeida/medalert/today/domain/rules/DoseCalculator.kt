package com.davialmeida.medalert.today.domain.rules

import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.today.domain.model.Dose
import java.time.LocalDate

fun calculateDosesForDate(
    medication: Medication,
    date: LocalDate
): List<Dose> {

    if (!medication.isActiveOn(date)) {
        return emptyList()
    }

    return medication.frequency
        .calculateTimesForDate(date)
        .map { time ->
            Dose(
                medicationId = medication.id,
                scheduledDateTime = date.atTime(time)
            )
        }
}