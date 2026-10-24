package com.davialmeida.medalert.today.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.davialmeida.medalert.core.database.AppDatabase
import com.davialmeida.medalert.medication.data.local.entity.MedicationEntity
import com.davialmeida.medalert.profile.data.local.entity.ProfileEntity
import com.davialmeida.medalert.today.data.respository.DoseLogRepositoryImpl
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class DoseLogRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: DoseLogRepositoryImpl

    private val date = LocalDate.of(2026, 10, 16)
    private val loggedAt = Instant.parse("2026-10-16T10:00:00Z")

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = DoseLogRepositoryImpl(
            doseLogDao = database.doseLogDao(),
            database = database
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun insertProfile(name: String): Long {
        return database.profileDao().insert(
            ProfileEntity(name = name, color = "BLUE")
        )
    }

    private suspend fun insertMedication(profileId: Long): Long {
        return database.medicationDao().insert(
            MedicationEntity(
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
                startDate = date.minusDays(5).toEpochDay(),
                endDate = null,
                color = "BLUE",
                notes = "",
                remindersEnabled = true
            )
        )
    }

    private fun log(
        medicationId: Long,
        dateTime: LocalDateTime,
        status: DoseLogStatus = DoseLogStatus.TAKEN,
        notes: String? = null
    ) = DoseLog(
        id = 0,
        medicationId = medicationId,
        scheduledDateTime = dateTime,
        status = status,
        loggedAt = loggedAt,
        takenAt = if (status == DoseLogStatus.TAKEN) loggedAt else null,
        notes = notes
    )

    @Test
    fun upsert_andObserve_returnsSavedLogWithAllFields() = runTest {
        val profileId = insertProfile("Davi")
        val medicationId = insertMedication(profileId)

        repository.upsert(
            log(medicationId, date.atTime(8, 0), notes = "After lunch")
        )

        val saved = repository.observeByProfileAndDate(profileId, date)
            .first()
            .single()

        assertEquals(medicationId, saved.medicationId)
        assertEquals(date.atTime(8, 0), saved.scheduledDateTime)
        assertEquals(DoseLogStatus.TAKEN, saved.status)
        assertEquals(loggedAt, saved.loggedAt)
        assertEquals(loggedAt, saved.takenAt)
        assertEquals("After lunch", saved.notes)
    }

    @Test
    fun upsert_sameDoseTwice_keepsOneRowAndUpdatesIt() = runTest {
        val profileId = insertProfile("Davi")
        val medicationId = insertMedication(profileId)

        repository.upsert(log(medicationId, date.atTime(8, 0)))
        val firstId = repository.observeByProfileAndDate(profileId, date)
            .first()
            .single()
            .id

        repository.upsert(
            log(medicationId, date.atTime(8, 0), DoseLogStatus.SKIPPED)
        )

        val logs = repository.observeByProfileAndDate(profileId, date).first()

        assertEquals(1, logs.size)
        assertEquals(DoseLogStatus.SKIPPED, logs.single().status)
        assertNull(logs.single().takenAt)
        assertEquals(firstId, logs.single().id)
    }

    @Test
    fun upsert_differentTimes_keepsTwoRows() = runTest {
        val profileId = insertProfile("Davi")
        val medicationId = insertMedication(profileId)

        repository.upsert(log(medicationId, date.atTime(8, 0)))
        repository.upsert(log(medicationId, date.atTime(20, 0)))

        val logs = repository.observeByProfileAndDate(profileId, date).first()

        assertEquals(2, logs.size)
        assertEquals(
            listOf(date.atTime(8, 0), date.atTime(20, 0)),
            logs.map { it.scheduledDateTime }
        )
    }

    @Test
    fun delete_removesOnlyTheGivenDose() = runTest {
        val profileId = insertProfile("Davi")
        val medicationId = insertMedication(profileId)

        repository.upsert(log(medicationId, date.atTime(8, 0)))
        repository.upsert(log(medicationId, date.atTime(20, 0)))

        repository.delete(medicationId, date.atTime(8, 0))

        val logs = repository.observeByProfileAndDate(profileId, date).first()

        assertEquals(1, logs.size)
        assertEquals(date.atTime(20, 0), logs.single().scheduledDateTime)
    }

    @Test
    fun delete_ofDoseWithoutLog_doesNothing() = runTest {
        val profileId = insertProfile("Davi")
        val medicationId = insertMedication(profileId)

        repository.delete(medicationId, date.atTime(8, 0))

        assertTrue(
            repository.observeByProfileAndDate(profileId, date).first().isEmpty()
        )
    }

    @Test
    fun observe_returnsOnlyLogsOfTheGivenDate() = runTest {
        val profileId = insertProfile("Davi")
        val medicationId = insertMedication(profileId)

        repository.upsert(log(medicationId, date.atTime(8, 0)))
        repository.upsert(log(medicationId, date.minusDays(1).atTime(8, 0)))

        val logs = repository.observeByProfileAndDate(profileId, date).first()

        assertEquals(1, logs.size)
        assertEquals(date, logs.single().scheduledDateTime.toLocalDate())
    }

    @Test
    fun observe_doesNotReturnLogsOfAnotherProfile() = runTest {
        val davi = insertProfile("Davi")
        val maria = insertProfile("Maria")
        val daviMedication = insertMedication(davi)
        val mariaMedication = insertMedication(maria)

        repository.upsert(log(daviMedication, date.atTime(8, 0)))
        repository.upsert(log(mariaMedication, date.atTime(8, 0)))

        val daviLogs = repository.observeByProfileAndDate(davi, date).first()
        val mariaLogs = repository.observeByProfileAndDate(maria, date).first()

        assertEquals(listOf(daviMedication), daviLogs.map { it.medicationId })
        assertEquals(listOf(mariaMedication), mariaLogs.map { it.medicationId })
    }

    @Test
    fun deletingTheMedication_cascadesToItsLogs() = runTest {
        val profileId = insertProfile("Davi")
        val medicationId = insertMedication(profileId)

        repository.upsert(log(medicationId, date.atTime(8, 0)))

        val medication = database.medicationDao().findById(medicationId)
        assertNotNull(medication)
        database.medicationDao().delete(medication!!)

        assertTrue(
            repository.observeByProfileAndDate(profileId, date).first().isEmpty()
        )
    }
}