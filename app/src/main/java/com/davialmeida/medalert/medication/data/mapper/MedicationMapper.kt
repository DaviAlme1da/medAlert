package com.davialmeida.medalert.medication.data.mapper

import com.davialmeida.medalert.medication.data.local.entity.MedicationEntity
import com.davialmeida.medalert.medication.domain.model.Dosage
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.model.MedicationColor
import com.davialmeida.medalert.medication.domain.model.MedicationForm
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private const val DATABASE_TIME_FORMAT = "HH:mm"
private const val FREQUENCY_DAILY = "DAILY"
private const val FREQUENCY_SPECIFIC_DAYS = "SPECIFIC_DAYS"
private const val FREQUENCY_EVERY_X_HOURS = "EVERY_X_HOURS"

private val databaseTimeFormatter =
    DateTimeFormatter.ofPattern(DATABASE_TIME_FORMAT)

fun MedicationEntity.toDomain(): Medication {
    val frequency = when (frequencyType) {
        FREQUENCY_DAILY -> Frequency.Daily(
            times = parseTimes(frequencyTimes)
        )

        FREQUENCY_SPECIFIC_DAYS -> Frequency.SpecificDays(
            daysOfWeek = parseDays(frequencyDays),
            times = parseTimes(frequencyTimes)
        )

        FREQUENCY_EVERY_X_HOURS -> Frequency.EveryXHours(
            intervalHours = requireNotNull(frequencyIntervalHours),
            firstTime = parseTime(requireNotNull(frequencyFirstTime))
        )

        else -> error("Unknown frequency type: $frequencyType")
    }

    return Medication(
        id = id,
        seriesId = seriesId,
        profileId = profileId,
        name = name,
        dosage = Dosage(
            amount = doseAmount,
            unit = DosageUnit.valueOf(doseUnit)
        ),
        form = MedicationForm.valueOf(form),
        frequency = frequency,
        startDate = LocalDate.ofEpochDay(startDate),
        endDate = endDate?.let(LocalDate::ofEpochDay),
        color = MedicationColor.valueOf(color),
        notes = notes,
        remindersEnabled = remindersEnabled
    )
}

fun Medication.toEntity(): MedicationEntity {
    val frequencyData = frequency.toEntityData()

    return MedicationEntity(
        id = id,
        seriesId = seriesId,
        profileId = profileId,
        name = name,
        doseAmount = dosage.amount,
        doseUnit = dosage.unit.name,
        form = form.name,
        frequencyType = frequencyData.type,
        frequencyTimes = frequencyData.times,
        frequencyDays = frequencyData.days,
        frequencyIntervalHours = frequencyData.intervalHours,
        frequencyFirstTime = frequencyData.firstTime,
        startDate = startDate.toEpochDay(),
        endDate = endDate?.toEpochDay(),
        color = color.name,
        notes = notes,
        remindersEnabled = remindersEnabled
    )
}


private fun Frequency.toEntityData(): FrequencyEntityData {
    return when (this) {
        is Frequency.Daily -> FrequencyEntityData(
            type = FREQUENCY_DAILY,
            times = formatTimes(times),
            days = null,
            intervalHours = null,
            firstTime = null
        )

        is Frequency.SpecificDays -> FrequencyEntityData(
            type = FREQUENCY_SPECIFIC_DAYS,
            times = formatTimes(times),
            days = daysOfWeek.joinToString(",") { it.name },
            intervalHours = null,
            firstTime = null
        )

        is Frequency.EveryXHours -> FrequencyEntityData(
            type = FREQUENCY_EVERY_X_HOURS,
            times = "",
            days = null,
            intervalHours = intervalHours,
            firstTime = formatTime(firstTime)
        )
    }
}

private fun parseTimes(value: String): List<LocalTime> {
    if (value.isBlank()) {
        return emptyList()
    }

    return value
        .split(",")
        .map { parseTime(it) }
}

private data class FrequencyEntityData(
    val type: String,
    val times: String,
    val days: String?,
    val intervalHours: Int?,
    val firstTime: String?
)
private fun formatTimes(times: List<LocalTime>): String {
    return times.joinToString(",") {
        formatTime(it)
    }
}

private fun parseTime(value: String): LocalTime {
    return LocalTime.parse(
        value,
        databaseTimeFormatter
    )
}

private fun formatTime(time: LocalTime): String {
    return time.format(databaseTimeFormatter)
}

private fun parseDays(value: String?): Set<DayOfWeek> {
    if (value.isNullOrBlank()) {
        return emptySet()
    }

    return value
        .split(",")
        .map { DayOfWeek.valueOf(it) }
        .toSet()
}