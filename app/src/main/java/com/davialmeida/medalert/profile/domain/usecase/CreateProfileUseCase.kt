package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import com.davialmeida.medalert.profile.domain.validation.ProfileValidator
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class CreateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {

    suspend operator fun invoke(
        name: String,
        color: ProfileColor
    ): AppResult<Long> {
        val validation = ProfileValidator.validateName(name)

        if (validation is AppResult.Error) {
            return validation
        }

        return try {
            val id = repository.create(
                Profile(
                    id = 0,
                    name = name.trim(),
                    color = color
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