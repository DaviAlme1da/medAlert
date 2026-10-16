package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.profile.domain.error.ProfileError
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {

    suspend operator fun invoke(profileId: Long): AppResult<Unit> {
        return try {
            val profile = repository.observeById(profileId).first()
                ?: return AppResult.Error(ProfileError.ProfileNotFound)

            val profiles = repository.observeAll().first()

            if (profiles.size <= 1) {
                return AppResult.Error(ProfileError.CannotDeleteLastProfile)
            }

            val activeProfile = repository.observeActiveProfile().first()

            if (activeProfile?.id == profileId) {
                val nextActiveProfile = profiles.firstOrNull {
                    it.id != profileId
                } ?: return AppResult.Error(
                    ProfileError.CannotDeleteLastProfile
                )

                repository.setActiveProfileId(nextActiveProfile.id)
            }

            repository.delete(profile)

            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error(ProfileError.DeleteFailed)
        }
    }
}