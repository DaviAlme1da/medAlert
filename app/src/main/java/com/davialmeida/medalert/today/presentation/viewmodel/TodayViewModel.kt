package com.davialmeida.medalert.today.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.profile.domain.usecase.ObserveActiveProfileUseCase
import com.davialmeida.medalert.profile.domain.usecase.ObserveProfilesUseCase
import com.davialmeida.medalert.profile.domain.usecase.SetActiveProfileUseCase
import com.davialmeida.medalert.today.domain.model.DayProgress
import com.davialmeida.medalert.today.domain.model.DaySchedule
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import com.davialmeida.medalert.today.domain.rules.buildDoseItems
import com.davialmeida.medalert.today.domain.rules.calculateProgress
import com.davialmeida.medalert.today.domain.usecase.MarkDoseUseCase
import com.davialmeida.medalert.today.domain.usecase.ObserveDayScheduleUseCase
import com.davialmeida.medalert.today.domain.usecase.UndoDoseUseCase
import com.davialmeida.medalert.today.presentation.mapper.toUi
import com.davialmeida.medalert.today.presentation.model.DoseItemUi
import com.davialmeida.medalert.today.presentation.state.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

private const val TICK_MILLIS = 60_000L

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val observeProfilesUseCase: ObserveProfilesUseCase,
    private val observeActiveProfileUseCase: ObserveActiveProfileUseCase,
    private val setActiveProfileUseCase: SetActiveProfileUseCase,
    private val observeDayScheduleUseCase: ObserveDayScheduleUseCase,
    private val markDoseUseCase: MarkDoseUseCase,
    private val undoDoseUseCase: UndoDoseUseCase,
    private val clock: Clock
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now(clock))

    private val _uiState = MutableStateFlow(
        LocalDate.now(clock).let { today ->
            TodayUiState(
                selectedDate = today,
                today = today,
                weekDays = weekOf(today)
            )
        }
    )
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private var profilesJob: Job? = null
    private var dayJob: Job? = null

    init {
        observeProfiles()
        observeDay()
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, hasLoadError = false) }
        observeProfiles()
        observeDay()
    }

    fun selectDate(date: LocalDate) {
        if (date == selectedDate.value) return

        selectedDate.value = date

        _uiState.update {
            it.copy(
                selectedDate = date,
                weekDays = weekOf(date),
                isLoading = true
            )
        }
    }

    fun previousWeek() = selectDate(selectedDate.value.minusWeeks(1))

    fun nextWeek() = selectDate(selectedDate.value.plusWeeks(1))

    fun markTaken(medicationId: Long, scheduledDateTime: LocalDateTime) {
        runAction {
            markDoseUseCase(medicationId, scheduledDateTime, DoseLogStatus.TAKEN)
        }
    }

    fun markSkipped(medicationId: Long, scheduledDateTime: LocalDateTime) {
        runAction {
            markDoseUseCase(medicationId, scheduledDateTime, DoseLogStatus.SKIPPED)
        }
    }

    fun undo(medicationId: Long, scheduledDateTime: LocalDateTime) {
        runAction { undoDoseUseCase(medicationId, scheduledDateTime) }
    }

    fun selectProfile(profileId: Long) {
        runAction { setActiveProfileUseCase(profileId) }
    }

    fun clearActionError() {
        _uiState.update { it.copy(actionError = null) }
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

    private fun observeProfiles() {
        profilesJob?.cancel()

        profilesJob = combine(
            observeProfilesUseCase(),
            observeActiveProfileUseCase()
        ) { profiles, activeProfile ->
            profiles to activeProfile
        }
            .onEach { (profiles, activeProfile) ->
                _uiState.update {
                    it.copy(profiles = profiles, activeProfile = activeProfile)
                }
            }
            .catch {
                _uiState.update {
                    it.copy(isLoading = false, hasLoadError = true)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeDay() {
        dayJob?.cancel()

        // Espera o primeiro valor REAL do perfil ativo (sem "null" inicial falso).
        val activeProfileId = observeActiveProfileUseCase()
            .map { profile -> profile?.id }
            .distinctUntilChanged()

        dayJob = combine(selectedDate, activeProfileId) { date, profileId ->
            date to profileId
        }
            .flatMapLatest { (date, profileId) ->
                if (profileId == null) {
                    flowOf(emptyContent())
                } else {
                    combine(
                        observeDayScheduleUseCase(profileId, date),
                        ticker()
                    ) { schedule, _ ->
                        buildContent(schedule, date)
                    }
                }
            }
            .onEach { content ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasLoadError = false,
                        today = content.today,
                        items = content.items,
                        progress = content.progress
                    )
                }
            }
            .catch {
                _uiState.update {
                    it.copy(isLoading = false, hasLoadError = true)
                }
            }
            .launchIn(viewModelScope)
    }

    /** Reemite a cada minuto para PENDING virar LATE sozinho. */
    private fun ticker(): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(TICK_MILLIS)
        }
    }

    private fun buildContent(schedule: DaySchedule, date: LocalDate): DayContent {
        val now = LocalDateTime.now(clock)
        val today = now.toLocalDate()
        val domainItems = buildDoseItems(schedule, date, now)

        return DayContent(
            today = today,
            items = domainItems.map { it.toUi(today) },
            progress = calculateProgress(domainItems)
        )
    }

    private fun emptyContent() = DayContent(
        today = LocalDate.now(clock),
        items = emptyList(),
        progress = DayProgress(taken = 0, total = 0)
    )

    private fun weekOf(date: LocalDate): List<LocalDate> {
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        return (0L..6L).map { offset -> monday.plusDays(offset) }
    }

    private data class DayContent(
        val today: LocalDate,
        val items: List<DoseItemUi>,
        val progress: DayProgress
    )
}