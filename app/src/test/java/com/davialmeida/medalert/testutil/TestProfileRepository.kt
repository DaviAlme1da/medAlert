package com.davialmeida.medalert.testutil

import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

internal class TestProfileRepository(
    initial: List<Profile> = emptyList(),
    activeId: Long? = initial.firstOrNull()?.id
) : ProfileRepository {

    private val profiles = MutableStateFlow(initial)
    private val activeProfileId = MutableStateFlow(activeId)

    override fun observeAll(): Flow<List<Profile>> = profiles

    override fun observeById(id: Long): Flow<Profile?> =
        profiles.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun create(profile: Profile): Long {
        val id = (profiles.value.maxOfOrNull { it.id } ?: 0L) + 1
        profiles.value = profiles.value + profile.copy(id = id)
        return id
    }

    override suspend fun createIfEmpty(profile: Profile): Long? {
        return if (profiles.value.isEmpty()) create(profile) else null
    }

    override suspend fun update(profile: Profile) {
        profiles.value = profiles.value.map { if (it.id == profile.id) profile else it }
    }

    override suspend fun delete(profile: Profile) {
        profiles.value = profiles.value.filterNot { it.id == profile.id }
    }

    override fun observeActiveProfile(): Flow<Profile?> =
        combine(profiles, activeProfileId) { list, id -> list.firstOrNull { it.id == id } }

    override suspend fun setActiveProfileId(profileId: Long?) {
        activeProfileId.value = profileId
    }
}