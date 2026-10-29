package com.davialmeida.medalert.today.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.today.domain.error.DoseError
import com.davialmeida.medalert.today.domain.listener.DoseRegistrationListener
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import com.davialmeida.medalert.today.domain.repository.DoseLogRepository
import com.davialmeida.medalert.today.domain.rules.isDoseOf // AJUSTE: pacote da sua função
import kotlinx.coroutines.CancellationException
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

class MarkDoseUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val registrationListener: DoseRegistrationListener,
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

            val today = LocalDateTime.now(clock).toLocalDate()
            if (scheduledDateTime.toLocalDate().isAfter(today)) {
                return AppResult.Error(DoseError.FutureDose)
            }

            val loggedAt = clock.instant()

            doseLogRepository.upsert(
                DoseLog(
                    id = 0,
                    medicationId = medicationId,
                    scheduledDateTime = scheduledDateTime,
                    status = status,
                    loggedAt = loggedAt,
                    takenAt = if (status == DoseLogStatus.TAKEN) loggedAt else null
                )
            )

            notifyRegistered(medicationId, scheduledDateTime)

            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }

    private fun notifyRegistered(medicationId: Long, scheduledDateTime: LocalDateTime) {
        try {
            registrationListener.onDoseRegistered(medicationId, scheduledDateTime)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            // Ignorado de propósito.
        }
    }
}