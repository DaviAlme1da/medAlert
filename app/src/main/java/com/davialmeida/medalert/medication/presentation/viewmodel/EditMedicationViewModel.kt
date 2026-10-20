package com.davialmeida.medalert.medication.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.presentation.MEDICATION_ID_ARG
import com.davialmeida.medalert.medication.domain.error.MedicationError
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.rules.MedicationStatus
import com.davialmeida.medalert.medication.domain.rules.statusOn
import com.davialmeida.medalert.medication.domain.usecase.GetMedicationDetailUseCase
import com.davialmeida.medalert.medication.domain.usecase.UpdateMedicationUseCase
import com.davialmeida.medalert.medication.presentation.mapper.toFormState
import com.davialmeida.medalert.medication.presentation.state.EditMedicationLoadState
import com.davialmeida.medalert.medication.presentation.state.MedicationFormUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EditMedicationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMedicationDetailUseCase: GetMedicationDetailUseCase,
    private val updateMedicationUseCase: UpdateMedicationUseCase,
    private val clock: Clock
) : MedicationFormViewModel(
    MedicationFormUiState(startDate = LocalDate.now(clock))
) {

    private val seriesId: Long =
        checkNotNull(savedStateHandle[MEDICATION_ID_ARG])

    private val mutableLoadState =
        MutableStateFlow<EditMedicationLoadState>(EditMedicationLoadState.Loading)

    val loadState: StateFlow<EditMedicationLoadState> =
        mutableLoadState.asStateFlow()

    private var original: Medication? = null

    init {
        loadMedication()
    }

    fun retry() {
        mutableLoadState.value = EditMedicationLoadState.Loading
        loadMedication()
    }

    override fun medicationTemplate(): Medication? = original

    override suspend fun persist(medication: Medication): AppResult<*> {
        return updateMedicationUseCase(medication, LocalDate.now(clock))
    }

    private fun loadMedication() {
        viewModelScope.launch {
            when (val result = getMedicationDetailUseCase(seriesId)) {
                is AppResult.Success -> {
                    val medication = result.data
                    val today = LocalDate.now(clock)

                    if (medication.statusOn(today) == MedicationStatus.ENDED) {
                        mutableLoadState.value = EditMedicationLoadState.Failed(
                            MedicationError.CannotEdit
                        )
                        return@launch
                    }

                    original = medication
                    mutableForm.value = medication.toFormState()
                    mutableLoadState.value = EditMedicationLoadState.Ready
                }

                is AppResult.Error -> {
                    mutableLoadState.value =
                        EditMedicationLoadState.Failed(result.error)
                }
            }
        }
    }
}
