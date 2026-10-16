package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FakeProfileRepository : ProfileRepository {

    private val profiles = MutableStateFlow<List<Profile>>(emptyList())
    private val activeProfileId = MutableStateFlow<Long?>(null)

    var failOnCreate = false
    var failOnUpdate = false
    var failOnDelete = false
    var failOnSetActiveProfile = false

    fun storedActiveProfileId(): Long? = activeProfileId.value

    override fun observeAll(): Flow<List<Profile>> = profiles

    override fun observeById(id: Long): Flow<Profile?> {
        return profiles.map { list -> list.find { it.id == id } }
    }

    override suspend fun create(profile: Profile): Long {
        if (failOnCreate) throw IllegalStateException("create failed")
        return insert(profile)
    }

    override suspend fun createIfEmpty(profile: Profile): Long? {
        if (profiles.value.isNotEmpty()) return null

        val id = insert(profile)
        activeProfileId.value = id
        return id
    }

    override suspend fun update(profile: Profile) {
        if (failOnUpdate) throw IllegalStateException("update failed")

        profiles.value = profiles.value.map {
            if (it.id == profile.id) profile else it
        }
    }

    override suspend fun delete(profile: Profile) {
        if (failOnDelete) throw IllegalStateException("delete failed")

        profiles.value = profiles.value.filterNot { it.id == profile.id }
    }

    override fun observeActiveProfile(): Flow<Profile?> {
        return combine(profiles, activeProfileId) { list, activeId ->
            list.find { it.id == activeId }
        }
    }

    override suspend fun setActiveProfileId(profileId: Long?) {
        if (failOnSetActiveProfile) throw IllegalStateException("set active failed")

        activeProfileId.value = profileId
    }

    private fun insert(profile: Profile): Long {
        val id = if (profile.id == 0L) {
            (profiles.value.maxOfOrNull { it.id } ?: 0L) + 1L
        } else {
            profile.id
        }

        profiles.value = profiles.value + profile.copy(id = id)
        return id
    }
}