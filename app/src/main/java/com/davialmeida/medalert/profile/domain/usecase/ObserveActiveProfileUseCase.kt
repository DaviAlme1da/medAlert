package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveActiveProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<Profile?> {
        return combine(
            repository.observeActiveProfile(),
            repository.observeAll()
        ) { activeProfile, profiles ->
            activeProfile ?: profiles.firstOrNull()
        }
    }
}
