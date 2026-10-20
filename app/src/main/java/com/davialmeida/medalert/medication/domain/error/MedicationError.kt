package com.davialmeida.medalert.medication.domain.error

import com.davialmeida.medalert.core.domain.AppError

sealed interface MedicationError : AppError {
    data object EmptyName : MedicationError
    data object NameTooLong : MedicationError
    data object NoTimes : MedicationError
    data object DuplicateTimes : MedicationError
    data object EndDateBeforeStartDate : MedicationError
    data object EndDateInPast : MedicationError
    data object InvalidInterval : MedicationError
    data object NoDaysSelected : MedicationError
    data object InvalidDosageAmount : MedicationError
    data object NoActiveProfile : MedicationError
    data object NotFound : MedicationError
    data object CannotEdit : MedicationError
    data object CannotPause : MedicationError
    data object CannotResume : MedicationError
}
