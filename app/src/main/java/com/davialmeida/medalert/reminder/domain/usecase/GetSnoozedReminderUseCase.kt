package com.davialmeida.medalert.reminder.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import com.davialmeida.medalert.reminder.domain.model.DueReminder
import com.davialmeida.medalert.today.domain.repository.DoseLogRepository
import com.davialmeida.medalert.today.domain.rules.calculateDosesForDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject

class GetSnoozedReminderUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val profileRepository: ProfileRepository
) {

    /** Devolve `null` quando a soneca não deve mais notificar. */
    suspend operator fun invoke(
        medicationId: Long,
        scheduledDateTime: LocalDateTime
    ): AppResult<DueReminder?> {
        return try {
            AppResult.Success(find(medicationId, scheduledDateTime))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }

    private suspend fun find(
        medicationId: Long,
        scheduledDateTime: LocalDateTime
    ): DueReminder? {
        val medication = medicationRepository.findById(medicationId) ?: return null
        val date = scheduledDateTime.toLocalDate()

        if (!medication.remindersEnabled || !medication.isActiveOn(date)) return null

        val stillScheduled = calculateDosesForDate(medication, date)
            .any { it.scheduledDateTime == scheduledDateTime }

        if (!stillScheduled) return null

        if (doseLogRepository.find(medicationId, scheduledDateTime) != null) return null

        val profileName = profileRepository
            .observeById(medication.profileId)
            .first()
            ?.name
            .orEmpty()

        return DueReminder(
            medicationId = medication.id,
            medicationName = medication.name,
            dosage = medication.dosage,
            profileName = profileName,
            scheduledDateTime = scheduledDateTime
        )
    }
}