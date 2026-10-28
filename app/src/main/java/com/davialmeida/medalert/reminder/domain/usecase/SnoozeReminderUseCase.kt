package com.davialmeida.medalert.reminder.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.reminder.domain.scheduler.SnoozeScheduler
import com.davialmeida.medalert.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

class SnoozeReminderUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val snoozeScheduler: SnoozeScheduler,
    private val clock: Clock
) {

    suspend operator fun invoke(
        medicationId: Long,
        scheduledDateTime: LocalDateTime
    ): AppResult<Unit> {
        return try {
            val minutes = settingsRepository.observeSettings().first().snoozeMinutes

            snoozeScheduler.schedule(
                medicationId = medicationId,
                scheduledDateTime = scheduledDateTime,
                triggerAt = clock.instant().plusSeconds(minutes * 60L)
            )

            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }
}