package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.profile.domain.error.ProfileError
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SetActiveProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {

    suspend operator fun invoke(profileId: Long): AppResult<Unit> {
        return try {
            val profile = repository.observeById(profileId).first()

            if (profile == null) {
                return AppResult.Error(ProfileError.ProfileNotFound)
            }

            repository.setActiveProfileId(profileId)

            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(UnexpectedAppError)
        }
    }

}