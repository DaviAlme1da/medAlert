package com.davialmeida.medalert.medication.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.medication.domain.error.MedicationError
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.medication.domain.rules.MedicationStatus
import com.davialmeida.medalert.medication.domain.rules.latestVersion
import com.davialmeida.medalert.medication.domain.rules.statusOn
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import javax.inject.Inject

class PauseMedicationUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository
) {

    suspend operator fun invoke(
        medicationId: Long,
        today: LocalDate
    ): AppResult<Unit> {
        return try {
            val medication = medicationRepository.findById(medicationId)
                ?: return AppResult.Error(MedicationError.NotFound)

            val versions = medicationRepository.findVersionsBySeries(
                profileId = medication.profileId,
                seriesId = medication.seriesId
            )

            val canPause =
                latestVersion(versions)?.id == medication.id &&
                        medication.startDate < today &&
                        medication.statusOn(today) == MedicationStatus.ACTIVE

            if (!canPause) {
                return AppResult.Error(MedicationError.CannotPause)
            }

            medicationRepository.update(
                medication.copy(endDate = today.minusDays(1))
            )

            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }
}
