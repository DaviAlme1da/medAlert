
package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class EnsureActiveProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke() {
        val activeProfile = repository.observeActiveProfile().first()

        if (activeProfile != null) {
            return
        }

        val profiles = repository.observeAll().first()

        val firstProfile = profiles.firstOrNull() ?: return

        repository.setActiveProfileId(firstProfile.id)
    }
}
