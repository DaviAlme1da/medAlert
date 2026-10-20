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

class ResumeMedicationUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository
) {

    /**
     * Cria uma nova versão (cópia da última) começando hoje.
     * Retorna o id da nova versão.
     */
    suspend operator fun invoke(
        medicationId: Long,
        today: LocalDate
    ): AppResult<Long> {
        return try {
            val medication = medicationRepository.findById(medicationId)
                ?: return AppResult.Error(MedicationError.NotFound)

            val versions = medicationRepository.findVersionsBySeries(
                profileId = medication.profileId,
                seriesId = medication.seriesId
            )

            val canResume =
                latestVersion(versions)?.id == medication.id &&
                        medication.statusOn(today) == MedicationStatus.ENDED

            if (!canResume) {
                return AppResult.Error(MedicationError.CannotResume)
            }

            val newId = medicationRepository.createVersion(
                medication.copy(
                    id = 0L,
                    startDate = today,
                    endDate = null
                )
            )

            AppResult.Success(newId)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }
}
