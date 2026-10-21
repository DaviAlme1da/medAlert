package com.davialmeida.medalert.today.domain.model

import com.davialmeida.medalert.medication.domain.model.Medication
import java.time.LocalDateTime

data class DoseItem(
    val medication: Medication,
    val scheduledDateTime: LocalDateTime,
    val log: DoseLog?,
    val status: DoseStatus
)
