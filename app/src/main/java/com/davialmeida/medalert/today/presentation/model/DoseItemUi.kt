package com.davialmeida.medalert.today.presentation.model

import com.davialmeida.medalert.compose.component.DoseStatusUi
import com.davialmeida.medalert.medication.domain.model.Dosage
import com.davialmeida.medalert.medication.domain.model.MedicationColor
import java.time.LocalDateTime
import java.time.LocalTime

data class DoseItemUi(
    val medicationId: Long,
    val scheduledDateTime: LocalDateTime,
    val name: String,
    val dosage: Dosage,
    val color: MedicationColor,
    val status: DoseStatusUi,
    val canAct: Boolean,
    val hasLog: Boolean,
) {
    val time: LocalTime
        get() = scheduledDateTime.toLocalTime()

    val key: String
        get() = "$medicationId|$scheduledDateTime"
}