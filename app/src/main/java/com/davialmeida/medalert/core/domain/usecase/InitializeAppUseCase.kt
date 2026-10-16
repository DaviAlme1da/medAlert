package com.davialmeida.medalert.core.domain.usecase

import com.davialmeida.medalert.profile.domain.usecase.EnsureActiveProfileUseCase
import com.davialmeida.medalert.profile.domain.usecase.EnsureProfileUseCase
import javax.inject.Inject

class InitializeAppUseCase @Inject constructor(
    private val ensureProfileUseCase: EnsureProfileUseCase,
    private val ensureActiveProfileUseCase: EnsureActiveProfileUseCase
) {
    suspend operator fun invoke(defaultProfileName: String) {
        ensureProfileUseCase(defaultProfileName)
        ensureActiveProfileUseCase()
    }
}