package com.davialmeida.medalert.settings.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.settings.domain.model.ThemePreference
import com.davialmeida.medalert.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class SetThemePreferenceUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(theme: ThemePreference): AppResult<Unit> {
        return try {
            settingsRepository.setTheme(theme)
            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }
}