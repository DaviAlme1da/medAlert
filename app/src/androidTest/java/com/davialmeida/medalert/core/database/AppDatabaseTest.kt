package com.davialmeida.medalert.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.davialmeida.medalert.medication.data.local.entity.MedicationEntity
import com.davialmeida.medalert.profile.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider
            .getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertProfile_andFindById_returnsProfile() = runTest {
        val profile = ProfileEntity(
            name = "Davi",
            color = "BLUE"
        )

        val id = database
            .profileDao()
            .insert(profile)

        val savedProfile = database
            .profileDao()
            .findById(id)

        assertEquals("Davi", savedProfile?.name)
        assertEquals("BLUE", savedProfile?.color)
    }
    @Test
    fun insertMedication_andFindByProfile_returnsMedication() = runTest {
        val profile = ProfileEntity(
            name = "Davi",
            color = "BLUE"
        )

        val profileId = database.profileDao().insert(profile)

        val medication = MedicationEntity(
            profileId = profileId,
            name = "Paracetamol",
            doseAmount = BigDecimal("500"),
            doseUnit = "MG",
            form = "TABLET",
            frequencyType = "DAILY",
            frequencyTimes = "08:00,20:00",
            frequencyDays = null,
            frequencyIntervalHours = null,
            frequencyFirstTime = null,
            startDate = LocalDate.of(2026, 10, 1).toEpochDay(),
            endDate = null,
            color = "BLUE",
            notes = "",
            remindersEnabled = true
        )

        database.medicationDao().insert(medication)

        val medications = database.medicationDao()
            .observeByProfile(profileId)
            .first()

        assertEquals(1, medications.size)
        assertEquals("Paracetamol", medications.first().name)
        assertEquals(profileId, medications.first().profileId)
    }
}