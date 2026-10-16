package com.davialmeida.medalert.profile.presentation.state

import com.davialmeida.medalert.profile.domain.model.ProfileColor
import com.davialmeida.medalert.profile.presentation.model.ProfileErrorUi

data class ProfileFormUiState(
    val profileId: Long? = null,
    val name: String = "",
    val selectedColor: ProfileColor = ProfileColor.BLUE,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val profileNotFound: Boolean = false,
    val error: ProfileErrorUi? = null
)