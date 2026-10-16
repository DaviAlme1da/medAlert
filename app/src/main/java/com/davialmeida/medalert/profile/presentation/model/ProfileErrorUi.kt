package com.davialmeida.medalert.profile.presentation.model

import androidx.annotation.StringRes
import com.davialmeida.medalert.R
import com.davialmeida.medalert.core.domain.AppError
import com.davialmeida.medalert.profile.domain.error.ProfileError

sealed interface ProfileErrorUi {

    data object EmptyName : ProfileErrorUi
    data object NameTooShort : ProfileErrorUi
    data object NameTooLong : ProfileErrorUi
    data object CannotDeleteLastProfile : ProfileErrorUi
    data object ProfileNotFound : ProfileErrorUi
    data object DeleteFailed : ProfileErrorUi
    data object UnexpectedError : ProfileErrorUi

    val isNameError: Boolean
        get() = this is EmptyName || this is NameTooShort || this is NameTooLong

    @get:StringRes
    val messageRes: Int
        get() = when (this) {
            EmptyName -> R.string.profile_name_empty
            NameTooShort -> R.string.profile_name_too_short
            NameTooLong -> R.string.profile_name_too_long
            CannotDeleteLastProfile -> R.string.profile_cannot_delete_last
            ProfileNotFound -> R.string.profile_not_found
            DeleteFailed -> R.string.profile_delete_failed
            UnexpectedError -> R.string.profile_unexpected_error
        }
}

fun ProfileError.toUi(): ProfileErrorUi {
    return when (this) {
        ProfileError.EmptyName -> ProfileErrorUi.EmptyName
        ProfileError.NameTooShort -> ProfileErrorUi.NameTooShort
        ProfileError.NameTooLong -> ProfileErrorUi.NameTooLong
        ProfileError.CannotDeleteLastProfile -> ProfileErrorUi.CannotDeleteLastProfile
        ProfileError.ProfileNotFound -> ProfileErrorUi.ProfileNotFound
        ProfileError.DeleteFailed -> ProfileErrorUi.DeleteFailed
    }
}

fun AppError.toProfileErrorUi(): ProfileErrorUi {
    return when (this) {
        is ProfileError -> toUi()
        else -> ProfileErrorUi.UnexpectedError
    }
}