package com.davialmeida.medalert.today.presentation.mapper

import com.davialmeida.medalert.compose.component.DoseStatusUi
import com.davialmeida.medalert.today.domain.model.DoseItem
import com.davialmeida.medalert.today.domain.model.DoseStatus
import com.davialmeida.medalert.today.presentation.model.DoseItemUi
import java.time.LocalDate

fun DoseItem.toUi(today: LocalDate): DoseItemUi {
    return DoseItemUi(
        medicationId = medication.id,
        scheduledDateTime = scheduledDateTime,
        name = medication.name,
        dosage = medication.dosage,
        color = medication.color,
        status = status.toUi(),
        canAct = !scheduledDateTime.toLocalDate().isAfter(today),
        hasLog = log != null
    )
}

private fun DoseStatus.toUi(): DoseStatusUi {
    return when (this) {
        DoseStatus.PENDING -> DoseStatusUi.Pending
        DoseStatus.TAKEN -> DoseStatusUi.Taken
        DoseStatus.SKIPPED -> DoseStatusUi.Skipped
        DoseStatus.LATE -> DoseStatusUi.Late
    }
}