package com.davialmeida.medalert.today.domain.rules

import com.davialmeida.medalert.today.domain.model.Dose
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import com.davialmeida.medalert.today.domain.model.DoseStatus
import java.time.LocalDateTime

const val DOSE_LATE_TOLERANCE_MINUTES = 30

fun deriveDoseStatus(
    dose: Dose,
    doseLog: DoseLog?,
    now: LocalDateTime
): DoseStatus {

    if (doseLog != null) {
        return when (doseLog.status) {
            DoseLogStatus.TAKEN -> DoseStatus.TAKEN
            DoseLogStatus.SKIPPED -> DoseStatus.SKIPPED
        }
    }

    val limitTime = dose.scheduledDateTime
        .plusMinutes(
            DOSE_LATE_TOLERANCE_MINUTES.toLong()
        )

    return if (now > limitTime) {
        DoseStatus.LATE
    } else {
        DoseStatus.PENDING
    }
}