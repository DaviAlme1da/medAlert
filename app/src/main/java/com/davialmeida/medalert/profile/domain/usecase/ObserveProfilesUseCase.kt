package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveProfilesUseCase @Inject constructor(
    private val repository: ProfileRepository
) {

    operator fun invoke(): Flow<List<Profile>> {
        return repository.observeAll()
    }
}