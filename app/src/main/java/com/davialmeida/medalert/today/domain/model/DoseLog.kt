package com.davialmeida.medalert.today.domain.model

import java.time.Instant
import java.time.LocalDateTime

data class DoseLog(
    val id: Long,
    val medicationId: Long,
    val scheduledDateTime: LocalDateTime,
    val status: DoseLogStatus,
    val loggedAt: Instant,
    val takenAt: Instant? = null,
    val notes: String? = null
)