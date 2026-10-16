package com.davialmeida.medalert.profile.domain.error

import com.davialmeida.medalert.core.domain.AppError

sealed interface ProfileError : AppError {

    data object EmptyName : ProfileError

    data object NameTooShort : ProfileError

    data object NameTooLong : ProfileError

    data object CannotDeleteLastProfile : ProfileError

    data object ProfileNotFound : ProfileError

    data object DeleteFailed : ProfileError
}