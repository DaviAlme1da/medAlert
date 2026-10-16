
package com.davialmeida.medalert.profile.domain.repository

import com.davialmeida.medalert.profile.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    fun observeAll(): Flow<List<Profile>>

    fun observeById(id: Long): Flow<Profile?>

    suspend fun create(profile: Profile): Long

    suspend fun createIfEmpty(profile: Profile): Long?

    suspend fun update(profile: Profile)

    suspend fun delete(profile: Profile)

    fun observeActiveProfile(): Flow<Profile?>

    suspend fun setActiveProfileId(profileId: Long?)
}
