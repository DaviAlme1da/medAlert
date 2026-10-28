package com.davialmeida.medalert.core.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davialmeida.medalert.R
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.usecase.InitializeAppUseCase
import com.davialmeida.medalert.settings.domain.model.AppSettings
import com.davialmeida.medalert.settings.domain.usecase.CompleteOnboardingUseCase
import com.davialmeida.medalert.settings.domain.usecase.ObserveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface StartupUiState {
    data object Loading : StartupUiState
    data object Onboarding : StartupUiState
    data object Ready : StartupUiState
    data object Error : StartupUiState
}

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val application: Application,
    private val initializeAppUseCase: InitializeAppUseCase,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StartupUiState>(StartupUiState.Loading)
    val uiState: StateFlow<StartupUiState> = _uiState.asStateFlow()

    private var initializationJob: Job? = null

    init {
        initializeApp()
    }

    private fun initializeApp() {
        if (initializationJob?.isActive == true) return

        initializationJob = viewModelScope.launch {
            _uiState.value = StartupUiState.Loading

            try {
                initializeAppUseCase(
                    defaultProfileName = application.getString(
                        R.string.profile_default_name
                    )
                )

                val settings: AppSettings = observeSettingsUseCase().first()

                _uiState.value = if (settings.onboardingCompleted) {
                    StartupUiState.Ready
                } else {
                    StartupUiState.Onboarding
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e(TAG, "App initialization failed", exception)
                _uiState.value = StartupUiState.Error
            }
        }
    }

    fun retry() {
        initializeApp()
    }

    fun completeOnboarding() {
        if (initializationJob?.isActive == true) return

        initializationJob = viewModelScope.launch {
            _uiState.value = StartupUiState.Loading

            try {
                _uiState.value = when (completeOnboardingUseCase()) {
                    is AppResult.Success -> StartupUiState.Ready
                    is AppResult.Error -> StartupUiState.Error
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to complete onboarding", exception)
                _uiState.value = StartupUiState.Error
            }
        }
    }

    private companion object {
        const val TAG = "StartupViewModel"
    }
}