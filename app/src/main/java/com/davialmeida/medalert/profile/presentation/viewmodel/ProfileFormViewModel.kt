package com.davialmeida.medalert.profile.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.presentation.PROFILE_ID_ARG
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import com.davialmeida.medalert.profile.domain.usecase.CreateProfileUseCase
import com.davialmeida.medalert.profile.domain.usecase.ObserveProfileByIdUseCase
import com.davialmeida.medalert.profile.domain.usecase.UpdateProfileUseCase
import com.davialmeida.medalert.profile.presentation.model.ProfileErrorUi
import com.davialmeida.medalert.profile.presentation.model.toProfileErrorUi
import com.davialmeida.medalert.profile.presentation.state.ProfileFormUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileFormViewModel @Inject constructor(
    private val createProfileUseCase: CreateProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val observeProfileByIdUseCase: ObserveProfileByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val profileId: Long? = savedStateHandle[PROFILE_ID_ARG]

    private val _uiState = MutableStateFlow(
        ProfileFormUiState(
            profileId = profileId,
            isLoading = profileId != null
        )
    )
    val uiState: StateFlow<ProfileFormUiState> = _uiState.asStateFlow()

    init {
        if (profileId != null) {
            loadProfile(profileId)
        }
    }

    private fun loadProfile(id: Long) {
        viewModelScope.launch {
            try {
                val profile = observeProfileByIdUseCase(id).first()

                _uiState.update { state ->
                    if (profile == null) {
                        state.copy(isLoading = false, profileNotFound = true)
                    } else {
                        state.copy(
                            name = profile.name,
                            selectedColor = profile.color,
                            isLoading = false
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = ProfileErrorUi.UnexpectedError
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { state ->
            state.copy(name = name, error = null)
        }
    }

    fun onColorChange(color: ProfileColor) {
        _uiState.update { state ->
            state.copy(selectedColor = color, error = null)
        }
    }

    fun save() {
        val state = _uiState.value

        if (state.isSaving || state.isLoading || state.profileNotFound) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val result: AppResult<*> = if (state.profileId == null) {
                createProfileUseCase(state.name, state.selectedColor)
            } else {
                updateProfileUseCase(
                    Profile(
                        id = state.profileId,
                        name = state.name,
                        color = state.selectedColor
                    )
                )
            }

            _uiState.update { current ->
                when (result) {
                    is AppResult.Success -> current.copy(
                        isSaving = false,
                        isSaved = true
                    )

                    is AppResult.Error -> current.copy(
                        isSaving = false,
                        error = result.error.toProfileErrorUi()
                    )
                }
            }
        }
    }

    fun clearSaved() {
        _uiState.update { it.copy(isSaved = false) }
    }
}