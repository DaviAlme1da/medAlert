package com.davialmeida.medalert.settings.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.settings.domain.model.SNOOZE_OPTIONS
import com.davialmeida.medalert.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class SetSnoozeMinutesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(minutes: Int): AppResult<Unit> {
        if (minutes !in SNOOZE_OPTIONS) {
            return AppResult.Error(UnexpectedAppError)
        }

        return try {
            settingsRepository.setSnoozeMinutes(minutes)
            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }
}