package com.davialmeida.medalert.medication.presentation.state

import com.davialmeida.medalert.medication.domain.model.Medication
import java.time.LocalDateTime

data class MedicationListItem(
    val medication: Medication,
    val nextDose: LocalDateTime?,
    val nextDoseIsToday: Boolean
)

data class MedicationsUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val activeMedications: List<MedicationListItem> = emptyList(),
    val pausedMedications: List<MedicationListItem> = emptyList()
)
