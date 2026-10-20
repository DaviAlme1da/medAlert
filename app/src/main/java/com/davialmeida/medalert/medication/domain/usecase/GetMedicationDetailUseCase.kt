package com.davialmeida.medalert.medication.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.medication.domain.error.MedicationError
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.medication.domain.rules.latestVersion
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetMedicationDetailUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val profileRepository: ProfileRepository
) {

    suspend operator fun invoke(seriesId: Long): AppResult<Medication> {
        return try {
            val profile = profileRepository
                .observeActiveProfile()
                .first()
                ?: return AppResult.Error(MedicationError.NoActiveProfile)

            val versions = medicationRepository.findVersionsBySeries(
                profileId = profile.id,
                seriesId = seriesId
            )

            val medication = latestVersion(versions)
                ?: return AppResult.Error(MedicationError.NotFound)

            AppResult.Success(medication)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }
}
