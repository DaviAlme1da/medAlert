package com.davialmeida.medalert.profile.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ActiveProfileStorage @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private val activeProfileIdKey = longPreferencesKey("active_profile_id")

    fun observeActiveProfileId(): Flow<Long?> {
        return dataStore.data
            .map { preferences ->
                preferences[activeProfileIdKey]
            }
    }

    suspend fun setActiveProfileId(profileId: Long?) {
        dataStore.edit { preferences ->
            if (profileId == null) {
                preferences.remove(activeProfileIdKey)
            } else {
                preferences[activeProfileIdKey] = profileId
            }
        }
    }
}