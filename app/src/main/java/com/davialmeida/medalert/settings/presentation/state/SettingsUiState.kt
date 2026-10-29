package com.davialmeida.medalert.settings.presentation.state

import com.davialmeida.medalert.settings.domain.model.AppSettings

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val hasSaveError: Boolean = false
)