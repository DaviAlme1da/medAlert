
package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class EnsureProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {

    suspend operator fun invoke(defaultProfileName: String) {
        val profile = Profile(
            id = 0,
            name = defaultProfileName,
            color = ProfileColor.BLUE
        )

        repository.createIfEmpty(profile)
    }
}
