package com.davialmeida.medalert.settings.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.davialmeida.medalert.settings.domain.model.AppSettings
import com.davialmeida.medalert.settings.domain.model.DEFAULT_SNOOZE_MINUTES
import com.davialmeida.medalert.settings.domain.model.SNOOZE_OPTIONS
import com.davialmeida.medalert.settings.domain.model.ThemePreference
import com.davialmeida.medalert.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val SNOOZE_MINUTES_KEY = intPreferencesKey("snooze_minutes")
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    }

    override fun observeSettings(): Flow<AppSettings> {
        return dataStore.data
            .catch { exception ->
                // Falha de leitura: usa os valores padrão em vez de derrubar o fluxo.
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences ->
                val theme = ThemePreference.entries
                    .firstOrNull { it.name == preferences[THEME_KEY] }
                    ?: ThemePreference.SYSTEM

                val snoozeMinutes = preferences[SNOOZE_MINUTES_KEY]
                    ?.takeIf { it in SNOOZE_OPTIONS }
                    ?: DEFAULT_SNOOZE_MINUTES

                AppSettings(
                    theme = theme,
                    snoozeMinutes = snoozeMinutes,
                    onboardingCompleted = preferences[ONBOARDING_COMPLETED_KEY] ?: false
                )
            }
    }

    override suspend fun setTheme(theme: ThemePreference) {
        dataStore.edit { it[THEME_KEY] = theme.name }
    }

    override suspend fun setSnoozeMinutes(minutes: Int) {
        require(minutes in SNOOZE_OPTIONS) { "Tempo de soneca inválido: $minutes" }

        dataStore.edit { it[SNOOZE_MINUTES_KEY] = minutes }
    }

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { it[ONBOARDING_COMPLETED_KEY] = true }
    }
}