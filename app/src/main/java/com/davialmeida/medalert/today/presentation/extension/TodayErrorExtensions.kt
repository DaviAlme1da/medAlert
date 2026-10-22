package com.davialmeida.medalert.today.presentation.extension

import androidx.annotation.StringRes
import com.davialmeida.medalert.R
import com.davialmeida.medalert.core.domain.AppError
import com.davialmeida.medalert.today.domain.error.DoseError

@StringRes
fun AppError.toTodayMessageRes(): Int {
    return when (this) {
        DoseError.FutureDose -> R.string.today_error_future_dose
        DoseError.DoseNotFound -> R.string.today_error_dose_not_found
        DoseError.MedicationNotFound -> R.string.today_error_medication_not_found
        else -> R.string.today_error_unexpected
    }
}