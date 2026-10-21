package com.davialmeida.medalert.today.data.mapper

import com.davialmeida.medalert.today.data.local.entity.DoseLogEntity
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private const val MINUTES_PER_HOUR = 60

fun LocalTime.toMinutesOfDay(): Int {
    return hour * MINUTES_PER_HOUR + minute
}

fun LocalDateTime.toMinutesOfDay(): Int {
    return toLocalTime().toMinutesOfDay()
}

private fun minutesOfDayToLocalTime(minutes: Int): LocalTime {
    return LocalTime.of(
        minutes / MINUTES_PER_HOUR,
        minutes % MINUTES_PER_HOUR
    )
}

fun DoseLogEntity.toDomain(): DoseLog {
    return DoseLog(
        id = id,
        medicationId = medicationId,
        scheduledDateTime = LocalDate
            .ofEpochDay(scheduledDate)
            .atTime(minutesOfDayToLocalTime(scheduledTime)),
        status = DoseLogStatus.valueOf(status),
        loggedAt = loggedAt,
        takenAt = takenAt,
        notes = notes
    )
}

fun DoseLog.toEntity(): DoseLogEntity {
    return DoseLogEntity(
        id = id,
        medicationId = medicationId,
        scheduledDate = scheduledDateTime.toLocalDate().toEpochDay(),
        scheduledTime = scheduledDateTime.toMinutesOfDay(),
        status = status.name,
        loggedAt = loggedAt,
        takenAt = takenAt,
        notes = notes
    )
}