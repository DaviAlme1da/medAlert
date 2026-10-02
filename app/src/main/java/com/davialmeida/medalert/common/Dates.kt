package com.davialmeida.medalert.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun LocalTime.toDisplayString(): String {
    return format(timeFormatter)
}

fun String.toLocalTimeOrNull(): LocalTime? {
    return try {
        LocalTime.parse(this, timeFormatter)
    } catch (_: DateTimeParseException) {
        null
    }
}

fun LocalDate.startOfDay(zone: ZoneId): Instant {
    return atStartOfDay(zone).toInstant()
}

fun Instant.toLocalDate(zone: ZoneId): LocalDate {
    return atZone(zone).toLocalDate()
}