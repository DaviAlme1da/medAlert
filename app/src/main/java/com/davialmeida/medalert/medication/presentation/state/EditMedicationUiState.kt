package com.davialmeida.medalert.medication.presentation.state

import com.davialmeida.medalert.core.domain.AppError

sealed interface EditMedicationLoadState {
    data object Loading : EditMedicationLoadState
    data object Ready : EditMedicationLoadState
    data class Failed(val error: AppError) : EditMedicationLoadState
}
