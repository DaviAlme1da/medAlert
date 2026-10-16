package com.davialmeida.medalert.profile.domain.validation

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.profile.domain.error.ProfileError

const val PROFILE_NAME_MIN_LENGTH = 3
const val PROFILE_NAME_MAX_LENGTH = 30

object ProfileValidator {

    fun validateName(name: String): AppResult<Unit> {
        val normalizedName = name.trim()

        return when {
            normalizedName.isEmpty() ->
                AppResult.Error(ProfileError.EmptyName)

            normalizedName.length < PROFILE_NAME_MIN_LENGTH ->
                AppResult.Error(ProfileError.NameTooShort)

            normalizedName.length > PROFILE_NAME_MAX_LENGTH ->
                AppResult.Error(ProfileError.NameTooLong)

            else ->
                AppResult.Success(Unit)
        }
    }
}