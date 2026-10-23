package com.davialmeida.medalert.reminder.domain.model

import com.davialmeida.medalert.medication.domain.model.Dosage
import java.time.LocalDateTime

data class DueReminder(
    val medicationId: Long,
    val medicationName: String,
    val dosage: Dosage,
    val profileName: String,
    val scheduledDateTime: LocalDateTime
)