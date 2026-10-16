package com.davialmeida.medalert.profile.data.repository

import androidx.room.withTransaction
import com.davialmeida.medalert.core.database.AppDatabase
import com.davialmeida.medalert.profile.data.local.dao.ProfileDao
import com.davialmeida.medalert.profile.data.mapper.toDomain
import com.davialmeida.medalert.profile.data.mapper.toEntity
import com.davialmeida.medalert.profile.data.storage.ActiveProfileStorage
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlinx.coroutines.flow.flowOf

class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val activeProfileStorage: ActiveProfileStorage,
    private val database: AppDatabase
) : ProfileRepository {

    override fun observeAll(): Flow<List<Profile>> {
        return profileDao.observeAll().map { profiles ->
            profiles.map { it.toDomain() }
        }
    }

    override fun observeById(id: Long): Flow<Profile?> {
        return profileDao.observeById(id).map { profile ->
            profile?.toDomain()
        }
    }

    override suspend fun create(profile: Profile): Long {
        return profileDao.insert(profile.toEntity())
    }

    override suspend fun createIfEmpty(profile: Profile): Long? {
        return database.withTransaction {
            if (profileDao.count() > 0) {
                null
            } else {
                profileDao.insert(profile.toEntity())
            }
        }?.also { profileId ->
            activeProfileStorage.setActiveProfileId(profileId)
        }
    }

    override suspend fun update(profile: Profile) {
        profileDao.update(profile.toEntity())
    }

    override suspend fun delete(profile: Profile) {
        profileDao.delete(profile.toEntity())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeActiveProfile(): Flow<Profile?> {
        return activeProfileStorage.observeActiveProfileId()
            .flatMapLatest { activeProfileId ->
                if (activeProfileId == null) {
                    flowOf(null)
                } else {
                    profileDao.observeById(activeProfileId)
                        .map { profile -> profile?.toDomain() }
                }
            }
    }


    override suspend fun setActiveProfileId(profileId: Long?) {
        activeProfileStorage.setActiveProfileId(profileId)
    }
}
