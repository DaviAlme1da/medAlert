package com.davialmeida.medalert.medication.presentation.extension

import androidx.annotation.StringRes
import com.davialmeida.medalert.R
import com.davialmeida.medalert.core.domain.AppError
import com.davialmeida.medalert.medication.domain.error.MedicationError

@StringRes
fun AppError.toMessageRes(): Int {
    return when (this) {
        is MedicationError -> toMedicationMessageRes()
        else -> R.string.medication_error_unexpected
    }
}

@StringRes
private fun MedicationError.toMedicationMessageRes(): Int = when (this) {
    MedicationError.EmptyName -> R.string.medication_error_empty_name
    MedicationError.NameTooLong -> R.string.medication_error_name_too_long
    MedicationError.InvalidDosageAmount -> R.string.medication_error_invalid_dosage
    MedicationError.NoTimes -> R.string.medication_error_no_times
    MedicationError.DuplicateTimes -> R.string.medication_error_duplicate_times
    MedicationError.NoDaysSelected -> R.string.medication_error_no_days
    MedicationError.InvalidInterval -> R.string.medication_error_invalid_interval
    MedicationError.EndDateBeforeStartDate -> R.string.medication_error_end_before_start
    MedicationError.EndDateInPast -> R.string.medication_error_end_in_past
    MedicationError.NoActiveProfile -> R.string.medication_error_no_active_profile
    MedicationError.NotFound -> R.string.medication_error_not_found
    MedicationError.CannotEdit -> R.string.medication_error_cannot_edit
    MedicationError.CannotPause -> R.string.medication_error_cannot_pause
    MedicationError.CannotResume -> R.string.medication_error_cannot_resume
}
