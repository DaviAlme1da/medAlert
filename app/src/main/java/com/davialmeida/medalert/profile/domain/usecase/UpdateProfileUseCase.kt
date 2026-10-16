package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.profile.domain.error.ProfileError
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import com.davialmeida.medalert.profile.domain.validation.ProfileValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {

    suspend operator fun invoke(profile: Profile): AppResult<Unit> {
        val validation = ProfileValidator.validateName(profile.name)

        if (validation is AppResult.Error) {
            return validation
        }

        return try {
            val existingProfile = repository.observeById(profile.id).first()
                ?: return AppResult.Error(ProfileError.ProfileNotFound)

            repository.update(
                existingProfile.copy(
                    name = profile.name.trim(),
                    color = profile.color
                )
            )

            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }
}