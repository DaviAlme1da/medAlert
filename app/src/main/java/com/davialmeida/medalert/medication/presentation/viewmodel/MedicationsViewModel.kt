package com.davialmeida.medalert.medication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.rules.MedicationStatus
import com.davialmeida.medalert.medication.domain.rules.nextDoseAfter
import com.davialmeida.medalert.medication.domain.rules.statusOn
import com.davialmeida.medalert.medication.domain.usecase.ObserveMedicationsUseCase
import com.davialmeida.medalert.medication.presentation.state.MedicationListItem
import com.davialmeida.medalert.medication.presentation.state.MedicationsUiState
import com.davialmeida.medalert.profile.domain.usecase.ObserveActiveProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MedicationsViewModel @Inject constructor(
    private val observeActiveProfileUseCase: ObserveActiveProfileUseCase,
    private val observeMedicationsUseCase: ObserveMedicationsUseCase,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicationsUiState())
    val uiState: StateFlow<MedicationsUiState> = _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        startObserving()
    }

    fun retry() {
        _uiState.value = MedicationsUiState()
        startObserving()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startObserving() {
        observationJob?.cancel()

        observationJob = observeActiveProfileUseCase()
            .flatMapLatest { profile ->
                if (profile == null) {
                    flowOf(emptyList())
                } else {
                    observeMedicationsUseCase(profile.id)
                }
            }
            .onEach { medications -> _uiState.value = medications.toUiState() }
            .catch {
                _uiState.value = MedicationsUiState(
                    isLoading = false,
                    hasError = true
                )
            }
            .launchIn(viewModelScope)
    }

    private fun List<Medication>.toUiState(): MedicationsUiState {
        val now = LocalDateTime.now(clock)
        val today = now.toLocalDate()

        val items = map { medication ->
            val status = medication.statusOn(today)

            val nextDose = if (status == MedicationStatus.ENDED) {
                null
            } else {
                nextDoseAfter(medication, now)
            }

            status to MedicationListItem(
                medication = medication,
                nextDose = nextDose,
                nextDoseIsToday = nextDose?.toLocalDate() == today
            )
        }

        return MedicationsUiState(
            isLoading = false,
            activeMedications = items
                .filter { (status, _) -> status != MedicationStatus.ENDED }
                .map { (_, item) -> item },
            pausedMedications = items
                .filter { (status, _) -> status == MedicationStatus.ENDED }
                .map { (_, item) -> item }
        )
    }
}
