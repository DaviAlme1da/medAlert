package com.davialmeida.medalert.profile.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.davialmeida.medalert.core.database.AppDatabase
import com.davialmeida.medalert.profile.data.storage.ActiveProfileStorage
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ProfileRepositoryImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var database: AppDatabase
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var storage: ActiveProfileStorage
    private lateinit var repository: ProfileRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        val dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = {
                File(temporaryFolder.newFolder(), "test.preferences_pb")
            }
        )

        storage = ActiveProfileStorage(dataStore)
        repository = ProfileRepositoryImpl(
            profileDao = database.profileDao(),
            activeProfileStorage = storage,
            database = database
        )
    }

    @After
    fun tearDown() {
        database.close()
        dataStoreScope.cancel()
    }

    private fun profile(name: String) = Profile(
        id = 0,
        name = name,
        color = ProfileColor.BLUE
    )

    @Test
    fun createIfEmpty_calledTwice_keepsOnlyOneProfile() = runTest {
        val first = repository.createIfEmpty(profile("Default"))
        val second = repository.createIfEmpty(profile("Other"))

        assertNotNull(first)
        assertNull(second)
        assertEquals(1, repository.observeAll().first().size)
    }

    @Test
    fun createIfEmpty_makesTheNewProfileActive() = runTest {
        val id = repository.createIfEmpty(profile("Default"))

        assertEquals(id, storage.observeActiveProfileId().first())
        assertEquals(id, repository.observeActiveProfile().first()?.id)
    }

    @Test
    fun observeActiveProfile_returnsNullWhenStoredIdDoesNotExist() = runTest {
        repository.create(profile("Davi"))
        storage.setActiveProfileId(999L)

        assertNull(repository.observeActiveProfile().first())
    }

    @Test
    fun observeActiveProfile_returnsTheProfileOfTheStoredId() = runTest {
        repository.create(profile("Davi"))
        val secondId = repository.create(profile("Joao"))
        storage.setActiveProfileId(secondId)

        assertEquals("Joao", repository.observeActiveProfile().first()?.name)
    }
}