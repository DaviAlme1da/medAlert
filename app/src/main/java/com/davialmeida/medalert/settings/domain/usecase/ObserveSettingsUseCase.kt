package com.davialmeida.medalert.settings.domain.usecase

import com.davialmeida.medalert.settings.domain.model.AppSettings
import com.davialmeida.medalert.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> {
        return settingsRepository.observeSettings()
    }
}