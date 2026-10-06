package com.davialmeida.medalert.today.data.mapper

import com.davialmeida.medalert.today.data.local.entity.DoseLogEntity
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import java.time.LocalDate
import java.time.LocalTime

fun DoseLogEntity.toDomain(): DoseLog {
    return DoseLog(
        id = id,
        medicationId = medicationId,
        scheduledDateTime = LocalDate
            .ofEpochDay(scheduledDate)
            .atTime(
                LocalTime.of(
                    scheduledTime / 60,
                    scheduledTime % 60
                )
            ),
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
        scheduledDate = scheduledDateTime
            .toLocalDate()
            .toEpochDay(),
        scheduledTime =
            scheduledDateTime.toLocalTime().hour * 60 +
                    scheduledDateTime.toLocalTime().minute,
        status = status.name,
        loggedAt = loggedAt,
        takenAt = takenAt,
        notes = notes
    )
}