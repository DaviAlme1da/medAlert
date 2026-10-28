package com.davialmeida.medalert.today.domain.listener

import java.time.LocalDateTime

interface DoseRegistrationListener {
    fun onDoseRegistered(medicationId: Long, scheduledDateTime: LocalDateTime)
}