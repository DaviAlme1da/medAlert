package com.davialmeida.medalert.today.domain.model

import java.time.LocalDateTime

data class Dose(
    val medicationId: Long,
    val scheduledDateTime: LocalDateTime
)