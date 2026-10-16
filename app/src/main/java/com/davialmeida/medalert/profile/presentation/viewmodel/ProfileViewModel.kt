package com.davialmeida.medalert.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.profile.domain.usecase.DeleteProfileUseCase
import com.davialmeida.medalert.profile.domain.usecase.ObserveActiveProfileUseCase
import com.davialmeida.medalert.profile.domain.usecase.ObserveProfilesUseCase
import com.davialmeida.medalert.profile.domain.usecase.SetActiveProfileUseCase
import com.davialmeida.medalert.profile.presentation.model.ProfileErrorUi
import com.davialmeida.medalert.profile.presentation.model.toProfileErrorUi
import com.davialmeida.medalert.profile.presentation.state.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeProfilesUseCase: ObserveProfilesUseCase,
    observeActiveProfileUseCase: ObserveActiveProfileUseCase,
    private val setActiveProfileUseCase: SetActiveProfileUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfilesUseCase()
            .onEach { profiles ->
                _uiState.update { state ->
                    state.copy(profiles = profiles, isLoading = false)
                }
            }
            .catch {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = ProfileErrorUi.UnexpectedError
                    )
                }
            }
            .launchIn(viewModelScope)

        observeActiveProfileUseCase()
            .onEach { activeProfile ->
                _uiState.update { state ->
                    state.copy(activeProfile = activeProfile)
                }
            }
            .catch {
                _uiState.update { state ->
                    state.copy(error = ProfileErrorUi.UnexpectedError)
                }
            }
            .launchIn(viewModelScope)
    }

    fun setActiveProfile(profileId: Long) {
        viewModelScope.launch {
            val result = setActiveProfileUseCase(profileId)

            _uiState.update { state ->
                state.copy(error = result.errorUi())
            }
        }
    }

    fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            val result = deleteProfileUseCase(profileId)

            _uiState.update { state ->
                state.copy(error = result.errorUi())
            }
        }
    }

    fun clearError() {
        _uiState.update { state ->
            state.copy(error = null)
        }
    }

    private fun AppResult<*>.errorUi(): ProfileErrorUi? {
        return when (this) {
            is AppResult.Success -> null
            is AppResult.Error -> error.toProfileErrorUi()
        }
    }
}