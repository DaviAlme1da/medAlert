package com.davialmeida.medalert.settings.domain.model

data class AppSettings(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val snoozeMinutes: Int = DEFAULT_SNOOZE_MINUTES,
    val onboardingCompleted: Boolean = false
)