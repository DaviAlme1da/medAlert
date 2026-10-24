package com.davialmeida.medalert.history.domain.rules

import com.davialmeida.medalert.history.domain.model.AdherenceSummary
import com.davialmeida.medalert.history.domain.model.HistoryDay
import com.davialmeida.medalert.history.domain.model.HistoryPeriod
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.today.domain.model.DaySchedule
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseStatus
import com.davialmeida.medalert.today.domain.rules.buildDoseItems
import java.time.LocalDate
import java.time.LocalDateTime


fun buildHistory(
    medications: List<Medication>,
    logs: List<DoseLog>,
    period: HistoryPeriod,
    today: LocalDate,
    now: LocalDateTime
): List<HistoryDay> {
    val startDate = today.minusDays(period.days - 1)
    val history = mutableListOf<HistoryDay>()

    var date = startDate

    while (!date.isAfter(today)) {
        val activeMedications = medications.filter { medication ->
            medication.isActiveOn(date)
        }

        val dayLogs = logs.filter { log ->
            log.scheduledDateTime.toLocalDate() == date
        }

        val items = buildDoseItems(
            schedule = DaySchedule(
                medications = activeMedications,
                logs = dayLogs
            ),
            date = date,
            now = now
        )

        if (items.isNotEmpty()) {
            history.add(
                HistoryDay(
                    date = date,
                    items = items
                )
            )
        }

        date = date.plusDays(1)
    }

    return history.asReversed()
}

fun calculateAdherence(
    days: List<HistoryDay>
): AdherenceSummary {
    val items = days.flatMap { it.items }

    return AdherenceSummary(
        taken = items.count { it.status == DoseStatus.TAKEN },
        skipped = items.count { it.status == DoseStatus.SKIPPED },
        late = items.count { it.status == DoseStatus.LATE }
    )
}

fun filterBySeries(
    medications: List<Medication>,
    seriesId: Long?
): List<Medication> {
    return if (seriesId == null) {
        medications
    } else {
        medications.filter { it.seriesId == seriesId }
    }
}
