package com.davialmeida.medalert.medication.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.presentation.MEDICATION_ID_ARG
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.rules.MedicationStatus
import com.davialmeida.medalert.medication.domain.rules.statusOn
import com.davialmeida.medalert.medication.domain.usecase.DeleteMedicationUseCase
import com.davialmeida.medalert.medication.domain.usecase.GetMedicationDetailUseCase
import com.davialmeida.medalert.medication.domain.usecase.PauseMedicationUseCase
import com.davialmeida.medalert.medication.domain.usecase.ResumeMedicationUseCase
import com.davialmeida.medalert.medication.presentation.state.MedicationDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MedicationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMedicationDetailUseCase: GetMedicationDetailUseCase,
    private val pauseMedicationUseCase: PauseMedicationUseCase,
    private val resumeMedicationUseCase: ResumeMedicationUseCase,
    private val deleteMedicationUseCase: DeleteMedicationUseCase,
    private val clock: Clock
) : ViewModel() {

    private val seriesId: Long =
        checkNotNull(savedStateHandle[MEDICATION_ID_ARG])

    private val _uiState = MutableStateFlow(MedicationDetailUiState())
    val uiState: StateFlow<MedicationDetailUiState> = _uiState.asStateFlow()

    init {
        load(showLoading = true)
    }

    fun retry() = load(showLoading = true)

    /** A tela de detalhe volta a aparecer depois de editar: recarrega. */
    fun refresh() = load(showLoading = false)

    fun clearActionError() {
        _uiState.update { it.copy(actionError = null) }
    }

    fun pause() = runAction { medication, today ->
        pauseMedicationUseCase(medication.id, today)
    }

    fun resume() = runAction { medication, today ->
        resumeMedicationUseCase(medication.id, today)
    }

    fun delete() {
        val medication = _uiState.value.medication ?: return

        if (_uiState.value.isBusy) return

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, actionError = null) }

            when (val result = deleteMedicationUseCase(medication.id)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isBusy = false, isDeleted = true)
                }

                is AppResult.Error -> _uiState.update {
                    it.copy(isBusy = false, actionError = result.error)
                }
            }
        }
    }

    private fun runAction(
        action: suspend (Medication, LocalDate) -> AppResult<*>
    ) {
        val medication = _uiState.value.medication ?: return

        if (_uiState.value.isBusy) return

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, actionError = null) }

            when (val result = action(medication, LocalDate.now(clock))) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isBusy = false) }
                    load(showLoading = false)
                }

                is AppResult.Error -> _uiState.update {
                    it.copy(isBusy = false, actionError = result.error)
                }
            }
        }
    }

    private fun load(showLoading: Boolean) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, loadError = null) }
            }

            when (val result = getMedicationDetailUseCase(seriesId)) {
                is AppResult.Success -> {
                    val medication = result.data
                    val status = medication.statusOn(LocalDate.now(clock))
                    val today = LocalDate.now(clock)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            medication = medication,
                            status = status,
                            canEdit = status != MedicationStatus.ENDED,
                            canPause = status == MedicationStatus.ACTIVE &&
                                    medication.startDate < today,
                            canResume = status == MedicationStatus.ENDED,
                            loadError = null
                        )
                    }
                }

                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, loadError = result.error)
                }
            }
        }
    }
}
