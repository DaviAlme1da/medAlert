
package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveProfileByIdUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(id: Long): Flow<Profile?> {
        return profileRepository.observeById(id)
    }
}
