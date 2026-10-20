package com.davialmeida.medalert.medication.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.medication.domain.error.MedicationError
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.medication.domain.validation.MedicationValidator
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CreateMedicationUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val profileRepository: ProfileRepository
) {

    suspend operator fun invoke(
        medication: Medication
    ): AppResult<Long> {
        val validation = MedicationValidator.validate(medication)

        if (validation is AppResult.Error) {
            return validation
        }

        return try {
            val activeProfile = profileRepository
                .observeActiveProfile()
                .first()
                ?: return AppResult.Error(MedicationError.NoActiveProfile)

            val id = medicationRepository.create(
                medication.copy(
                    id = 0L,
                    seriesId = 0L,
                    profileId = activeProfile.id,
                    name = medication.name.trim()
                )
            )

            AppResult.Success(id)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }
}