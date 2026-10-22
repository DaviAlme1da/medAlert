
package com.davialmeida.medalert.today.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.today.domain.repository.DoseLogRepository
import kotlinx.coroutines.CancellationException
import java.time.LocalDateTime
import javax.inject.Inject

class UndoDoseUseCase @Inject constructor(
    private val doseLogRepository: DoseLogRepository
) {
    suspend operator fun invoke(
        medicationId: Long,
        scheduledDateTime: LocalDateTime
    ): AppResult<Unit> {
        return try {
            doseLogRepository.delete(
                medicationId = medicationId,
                scheduledDateTime = scheduledDateTime
            )

            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }
}
