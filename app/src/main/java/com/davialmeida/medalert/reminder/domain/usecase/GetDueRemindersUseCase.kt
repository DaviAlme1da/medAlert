package com.davialmeida.medalert.reminder.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import com.davialmeida.medalert.reminder.domain.model.DueReminder
import com.davialmeida.medalert.reminder.domain.rules.REMINDER_WINDOW_MINUTES
import com.davialmeida.medalert.reminder.domain.rules.dosesInWindow
import com.davialmeida.medalert.today.domain.repository.DoseLogRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject

class GetDueRemindersUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val profileRepository: ProfileRepository,
    private val clock: Clock
) {

    suspend operator fun invoke(
        triggerAt: Instant
    ): AppResult<List<DueReminder>> {
        return try {
            val trigger = LocalDateTime.ofInstant(triggerAt, clock.zone)
            val medications = medicationRepository.findAllWithReminders()

            val profileNames = medications
                .map { it.profileId }
                .distinct()
                .associateWith { profileId ->
                    profileRepository.observeById(profileId).first()?.name.orEmpty()
                }

            val reminders = buildList {
                for (medication in medications) {
                    val doses = dosesInWindow(
                        medication = medication,
                        now = trigger,
                        windowMinutes = REMINDER_WINDOW_MINUTES
                    )

                    for (dose in doses) {
                        val log = doseLogRepository.find(
                            medicationId = dose.medicationId,
                            scheduledDateTime = dose.scheduledDateTime
                        )

                        if (log != null) continue

                        add(
                            DueReminder(
                                medicationId = medication.id,
                                medicationName = medication.name,
                                dosage = medication.dosage,
                                profileName = profileNames.getValue(medication.profileId),
                                scheduledDateTime = dose.scheduledDateTime
                            )
                        )
                    }
                }
            }.sortedBy { it.scheduledDateTime }

            AppResult.Success(reminders)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }
}