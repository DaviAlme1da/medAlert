package com.davialmeida.medalert.today.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.today.domain.error.DoseError
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import com.davialmeida.medalert.today.domain.repository.DoseLogRepository
import com.davialmeida.medalert.today.domain.rules.isDoseOf
import kotlinx.coroutines.CancellationException
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

class MarkDoseUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(
        medicationId: Long,
        scheduledDateTime: LocalDateTime,
        status: DoseLogStatus
    ): AppResult<Unit> {
        return try {
            val medication = medicationRepository.findById(medicationId)
                ?: return AppResult.Error(DoseError.MedicationNotFound)

            if (!isDoseOf(medication, scheduledDateTime)) {
                return AppResult.Error(DoseError.DoseNotFound)
            }

            val nowInstant = clock.instant()
            val now = LocalDateTime.ofInstant(
                nowInstant,
                clock.zone
            )

            if (scheduledDateTime.toLocalDate().isAfter(now.toLocalDate())) {
                return AppResult.Error(DoseError.FutureDose)
            }

            val log = DoseLog(
                id = 0L,
                medicationId = medicationId,
                scheduledDateTime = scheduledDateTime,
                status = status,
                loggedAt = nowInstant,
                takenAt = if (status == DoseLogStatus.TAKEN) {
                    nowInstant
                } else {
                    null
                },
                notes = null
            )

            doseLogRepository.upsert(log)

            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }
}
