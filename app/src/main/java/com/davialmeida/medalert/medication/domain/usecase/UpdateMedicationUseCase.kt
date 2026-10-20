package com.davialmeida.medalert.medication.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.medication.domain.error.MedicationError
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.medication.domain.rules.MedicationStatus
import com.davialmeida.medalert.medication.domain.rules.latestVersion
import com.davialmeida.medalert.medication.domain.rules.requiresNewVersion
import com.davialmeida.medalert.medication.domain.rules.statusOn
import com.davialmeida.medalert.medication.domain.validation.MedicationValidator
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import javax.inject.Inject

class UpdateMedicationUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository
) {

    suspend operator fun invoke(
        medication: Medication,
        today: LocalDate
    ): AppResult<Long> {
        return try {
            val existing = medicationRepository.findById(medication.id)
                ?: return AppResult.Error(MedicationError.NotFound)

            if (
                existing.profileId != medication.profileId ||
                existing.seriesId != medication.seriesId
            ) {
                return AppResult.Error(MedicationError.NotFound)
            }

            val versions = medicationRepository.findVersionsBySeries(
                profileId = existing.profileId,
                seriesId = existing.seriesId
            )

            if (latestVersion(versions)?.id != existing.id) {
                return AppResult.Error(MedicationError.NotFound)
            }

            if (existing.statusOn(today) == MedicationStatus.ENDED) {
                return AppResult.Error(MedicationError.CannotEdit)
            }

            val updated = medication.copy(
                id = existing.id,
                seriesId = existing.seriesId,
                profileId = existing.profileId,
                startDate = existing.startDate,
                name = medication.name.trim()
            )

            val validation = MedicationValidator.validate(updated)

            if (validation is AppResult.Error) {
                return validation
            }

            val endDate = updated.endDate

            if (endDate != null && endDate < today) {
                return AppResult.Error(MedicationError.EndDateInPast)
            }

            val shouldCreateVersion = requiresNewVersion(
                medication = existing,
                newDosage = updated.dosage,
                newFrequency = updated.frequency,
                today = today
            )

            if (shouldCreateVersion) {
                val newVersion = updated.copy(
                    id = 0L,
                    startDate = today
                )

                val newId = medicationRepository.updateAndCreateVersion(
                    previousVersion = existing,
                    newVersion = newVersion,
                    previousEndDate = today.minusDays(1)
                )

                AppResult.Success(newId)
            } else {
                medicationRepository.update(updated)
                AppResult.Success(existing.id)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }
}
