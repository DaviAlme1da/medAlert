package com.davialmeida.medalert.today.domain.usecase

import com.davialmeida.medalert.today.domain.listener.DoseRegistrationListener
import java.time.LocalDateTime

internal class FakeDoseRegistrationListener : DoseRegistrationListener {

    val registered = mutableListOf<Pair<Long, LocalDateTime>>()
    var failOnRegister = false

    override fun onDoseRegistered(medicationId: Long, scheduledDateTime: LocalDateTime) {
        if (failOnRegister) throw IllegalStateException("Simulated listener failure")
        registered.add(medicationId to scheduledDateTime)
    }
}