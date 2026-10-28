package com.davialmeida.medalert.testutil

import com.davialmeida.medalert.settings.domain.model.AppSettings
import com.davialmeida.medalert.settings.domain.model.ThemePreference
import com.davialmeida.medalert.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeSettingsRepository(
    initial: AppSettings = AppSettings()
) : SettingsRepository {

    private val state = MutableStateFlow(initial)

    var failOnWrite = false

    val settings: AppSettings
        get() = state.value

    override fun observeSettings(): Flow<AppSettings> = state

    override suspend fun setTheme(theme: ThemePreference) {
        failIfNeeded()
        state.value = state.value.copy(theme = theme)
    }

    override suspend fun setSnoozeMinutes(minutes: Int) {
        failIfNeeded()
        state.value = state.value.copy(snoozeMinutes = minutes)
    }

    override suspend fun setOnboardingCompleted() {
        failIfNeeded()
        state.value = state.value.copy(onboardingCompleted = true)
    }

    private fun failIfNeeded() {
        if (failOnWrite) throw IllegalStateException("Simulated write failure")
    }
}