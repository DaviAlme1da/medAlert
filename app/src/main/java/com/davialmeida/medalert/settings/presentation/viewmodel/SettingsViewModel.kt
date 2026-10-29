package com.davialmeida.medalert.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.settings.domain.model.ThemePreference
import com.davialmeida.medalert.settings.domain.usecase.ObserveSettingsUseCase
import com.davialmeida.medalert.settings.domain.usecase.SetSnoozeMinutesUseCase
import com.davialmeida.medalert.settings.domain.usecase.SetThemePreferenceUseCase
import com.davialmeida.medalert.settings.presentation.state.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettingsUseCase: ObserveSettingsUseCase,
    private val setThemePreferenceUseCase: SetThemePreferenceUseCase,
    private val setSnoozeMinutesUseCase: SetSnoozeMinutesUseCase
) : ViewModel() {

    private val saveError = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        observeSettingsUseCase(),
        saveError
    ) { settings, hasSaveError ->
        SettingsUiState(settings = settings, hasSaveError = hasSaveError)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun onThemeSelected(theme: ThemePreference) {
        viewModelScope.launch {
            handle(setThemePreferenceUseCase(theme))
        }
    }

    fun onSnoozeMinutesSelected(minutes: Int) {
        viewModelScope.launch {
            handle(setSnoozeMinutesUseCase(minutes))
        }
    }

    fun clearSaveError() {
        saveError.update { false }
    }

    private fun handle(result: AppResult<Unit>) {
        saveError.update { result is AppResult.Error }
    }
}