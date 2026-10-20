package com.davialmeida.medalert.medication.domain.rules

import com.davialmeida.medalert.medication.domain.model.Dosage
import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.model.Medication
import java.time.LocalDate
import java.time.LocalDateTime

enum class MedicationStatus {
    SCHEDULED,
    ACTIVE,
    ENDED
}

private const val NEXT_DOSE_SEARCH_DAYS = 8

fun Medication.statusOn(date: LocalDate): MedicationStatus {
    val end = endDate

    return when {
        date < startDate -> MedicationStatus.SCHEDULED
        end != null && date > end -> MedicationStatus.ENDED
        else -> MedicationStatus.ACTIVE
    }
}

fun latestVersion(versions: List<Medication>): Medication? {
    return versions.maxWithOrNull(
        compareBy<Medication> { it.startDate }.thenBy { it.id }
    )
}

fun latestPerSeries(medications: List<Medication>): List<Medication> {
    return medications
        .groupBy { it.seriesId }
        .values
        .mapNotNull { versions -> latestVersion(versions) }
}

fun nextDoseAfter(
    medication: Medication,
    after: LocalDateTime
): LocalDateTime? {
    var date = maxOf(after.toLocalDate(), medication.startDate)

    repeat(NEXT_DOSE_SEARCH_DAYS) {
        if (medication.statusOn(date) == MedicationStatus.ENDED) {
            return null
        }

        val nextTime = medication.frequency
            .calculateTimesForDate(date)
            .map { time -> LocalDateTime.of(date, time) }
            .filter { dateTime -> dateTime > after }
            .minOrNull()

        if (nextTime != null) {
            return nextTime
        }

        date = date.plusDays(1)
    }

    return null
}

fun Dosage.hasSameValueAs(other: Dosage): Boolean {
    return unit == other.unit && amount.compareTo(other.amount) == 0
}

fun Frequency.hasSameScheduleAs(other: Frequency): Boolean {
    return when (this) {
        is Frequency.Daily ->
            other is Frequency.Daily &&
                    times.sorted() == other.times.sorted()

        is Frequency.SpecificDays ->
            other is Frequency.SpecificDays &&
                    daysOfWeek == other.daysOfWeek &&
                    times.sorted() == other.times.sorted()

        is Frequency.EveryXHours ->
            other is Frequency.EveryXHours &&
                    intervalHours == other.intervalHours &&
                    firstTime == other.firstTime
    }
}

fun requiresNewVersion(
    medication: Medication,
    newDosage: Dosage,
    newFrequency: Frequency,
    today: LocalDate
): Boolean {
    val startedBeforeToday = medication.startDate < today
    val isActive = medication.statusOn(today) == MedicationStatus.ACTIVE
    val changed = !medication.dosage.hasSameValueAs(newDosage) ||
            !medication.frequency.hasSameScheduleAs(newFrequency)

    return startedBeforeToday && isActive && changed
}
