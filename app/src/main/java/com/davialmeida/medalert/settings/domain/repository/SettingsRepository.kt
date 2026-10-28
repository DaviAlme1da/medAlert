package com.davialmeida.medalert.settings.domain.repository

import com.davialmeida.medalert.settings.domain.model.AppSettings
import com.davialmeida.medalert.settings.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun observeSettings(): Flow<AppSettings>

    suspend fun setTheme(theme: ThemePreference)

    suspend fun setSnoozeMinutes(minutes: Int)

    suspend fun setOnboardingCompleted()
}