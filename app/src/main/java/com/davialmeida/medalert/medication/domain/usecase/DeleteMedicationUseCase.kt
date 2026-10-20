
package com.davialmeida.medalert.medication.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.medication.domain.error.MedicationError
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class DeleteMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {

    suspend operator fun invoke(
        medicationId: Long
    ): AppResult<Unit> {
        return try {
            val medication = repository.findById(medicationId)
                ?: return AppResult.Error(MedicationError.NotFound)

            repository.deleteSeries(
                profileId = medication.profileId,
                seriesId = medication.seriesId
            )

            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }
}
