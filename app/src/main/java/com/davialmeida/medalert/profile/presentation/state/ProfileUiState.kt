package com.davialmeida.medalert.profile.presentation.state

import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.presentation.model.ProfileErrorUi

data class ProfileUiState(
    val profiles: List<Profile> = emptyList(),
    val activeProfile: Profile? = null,
    val isLoading: Boolean = true,
    val error: ProfileErrorUi? = null
)