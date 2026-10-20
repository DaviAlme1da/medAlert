package com.davialmeida.medalert.medication.presentation.state

import com.davialmeida.medalert.core.domain.AppError
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.rules.MedicationStatus

data class MedicationDetailUiState(
    val isLoading: Boolean = true,
    val medication: Medication? = null,
    val status: MedicationStatus = MedicationStatus.ACTIVE,
    val canEdit: Boolean = false,
    val canPause: Boolean = false,
    val canResume: Boolean = false,
    val isBusy: Boolean = false,
    val loadError: AppError? = null,
    val actionError: AppError? = null,
    val isDeleted: Boolean = false
)
