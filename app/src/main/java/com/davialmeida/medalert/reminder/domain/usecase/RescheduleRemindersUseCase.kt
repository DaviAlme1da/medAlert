package com.davialmeida.medalert.reminder.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.reminder.domain.rules.nextReminderAfter
import com.davialmeida.medalert.reminder.domain.scheduler.ReminderScheduler
import kotlinx.coroutines.CancellationException
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

class RescheduleRemindersUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val reminderScheduler: ReminderScheduler,
    private val clock: Clock
) {

    suspend operator fun invoke(): AppResult<Unit> {
        return try {
            val medications = medicationRepository.findAllWithReminders()
            val now = LocalDateTime.now(clock)

            val nextReminder = nextReminderAfter(
                medications = medications,
                after = now
            )

            if (nextReminder == null) {
                reminderScheduler.cancel()
            } else {
                reminderScheduler.scheduleAt(
                    nextReminder.atZone(clock.zone).toInstant()
                )
            }

            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }
}