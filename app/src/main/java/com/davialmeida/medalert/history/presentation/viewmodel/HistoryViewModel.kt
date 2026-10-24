package com.davialmeida.medalert.history.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.history.domain.model.HistoryPeriod
import com.davialmeida.medalert.history.domain.usecase.ObserveHistoryMedicationsUseCase
import com.davialmeida.medalert.history.domain.usecase.ObserveHistoryUseCase
import com.davialmeida.medalert.history.presentation.state.HistoryUiState
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.profile.domain.usecase.ObserveActiveProfileUseCase
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import com.davialmeida.medalert.today.domain.usecase.MarkDoseUseCase
import com.davialmeida.medalert.today.domain.usecase.UndoDoseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val observeHistoryUseCase: ObserveHistoryUseCase,
    private val observeHistoryMedicationsUseCase: ObserveHistoryMedicationsUseCase,
    private val observeActiveProfileUseCase: ObserveActiveProfileUseCase,
    private val markDoseUseCase: MarkDoseUseCase,
    private val undoDoseUseCase: UndoDoseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var profileJob: Job? = null
    private var medicationsJob: Job? = null
    private var historyJob: Job? = null

    init {
        observeProfile()
        observeHistory()
    }

    fun retry() {
        observeProfile()
        observeHistory()
    }

    fun selectPeriod(period: HistoryPeriod) {
        if (_uiState.value.period == period) return

        _uiState.update { it.copy(period = period) }
        observeHistory()
    }

    fun selectSeries(seriesId: Long?) {
        if (_uiState.value.selectedSeriesId == seriesId) return

        _uiState.update { it.copy(selectedSeriesId = seriesId) }
        observeHistory()
    }

    fun markTaken(medicationId: Long, scheduledDateTime: LocalDateTime) {
        runAction {
            markDoseUseCase(
                medicationId = medicationId,
                scheduledDateTime = scheduledDateTime,
                status = DoseLogStatus.TAKEN
            )
        }
    }

    fun markSkipped(medicationId: Long, scheduledDateTime: LocalDateTime) {
        runAction {
            markDoseUseCase(
                medicationId = medicationId,
                scheduledDateTime = scheduledDateTime,
                status = DoseLogStatus.SKIPPED
            )
        }
    }

    fun undo(medicationId: Long, scheduledDateTime: LocalDateTime) {
        runAction {
            undoDoseUseCase(
                medicationId = medicationId,
                scheduledDateTime = scheduledDateTime
            )
        }
    }

    fun clearActionError() {
        _uiState.update { it.copy(actionError = null) }
    }

    private fun observeProfile() {
        profileJob?.cancel()

        profileJob = observeActiveProfileUseCase()
            .distinctUntilChanged()
            .onEach { profile ->
                _uiState.update { it.copy(activeProfile = profile) }

                medicationsJob?.cancel()

                if (profile == null) {
                    _uiState.update { it.copy(medications = emptyList()) }
                    resetSeriesIfMissing(emptyList())
                } else {
                    medicationsJob = observeHistoryMedicationsUseCase(profile.id)
                        .onEach { medications ->
                            _uiState.update { it.copy(medications = medications) }
                            resetSeriesIfMissing(medications)
                        }
                        .catch { exception ->
                            if (exception is CancellationException) throw exception
                            showLoadError()
                        }
                        .launchIn(viewModelScope)
                }
            }
            .catch { exception ->
                if (exception is CancellationException) throw exception
                showLoadError()
            }
            .launchIn(viewModelScope)
    }

    private fun observeHistory() {
        historyJob?.cancel()

        val state = _uiState.value

        _uiState.update { it.copy(isLoading = true, hasLoadError = false) }

        historyJob = observeHistoryUseCase(
            period = state.period,
            seriesId = state.selectedSeriesId
        )
            .onEach { result ->
                when (result) {
                    is AppResult.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasLoadError = false,
                            days = result.data.days,
                            summary = result.data.summary,
                            today = result.data.today
                        )
                    }

                    is AppResult.Error -> showLoadError()
                }
            }
            .catch { exception ->
                if (exception is CancellationException) throw exception
                showLoadError()
            }
            .launchIn(viewModelScope)
    }

    private fun resetSeriesIfMissing(medications: List<Medication>) {
        val selected = _uiState.value.selectedSeriesId ?: return

        if (medications.none { it.seriesId == selected }) {
            _uiState.update { it.copy(selectedSeriesId = null) }
            observeHistory()
        }
    }

    private fun showLoadError() {
        _uiState.update { it.copy(isLoading = false, hasLoadError = true) }
    }

    private fun runAction(action: suspend () -> AppResult<*>) {
        viewModelScope.launch {
            when (val result = action()) {
                is AppResult.Success -> clearActionError()
                is AppResult.Error -> _uiState.update {
                    it.copy(actionError = result.error)
                }
            }
        }
    }
}