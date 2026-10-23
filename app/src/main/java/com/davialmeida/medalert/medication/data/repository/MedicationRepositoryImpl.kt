package com.davialmeida.medalert.medication.data.repository

import androidx.room.withTransaction
import com.davialmeida.medalert.core.database.AppDatabase
import com.davialmeida.medalert.medication.data.local.dao.MedicationDao
import com.davialmeida.medalert.medication.data.mapper.toDomain
import com.davialmeida.medalert.medication.data.mapper.toEntity
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao,
    private val database: AppDatabase
) : MedicationRepository {

    override fun observeByProfile(
        profileId: Long
    ): Flow<List<Medication>> =
        medicationDao.observeByProfile(profileId)
            .map { medications ->
                medications.map { it.toDomain() }
            }

    override fun observeActiveOn(
        profileId: Long,
        date: LocalDate
    ): Flow<List<Medication>> =
        medicationDao.observeActiveOn(
            profileId,
            date.toEpochDay()
        ).map { medications ->
            medications.map { it.toDomain() }
        }

    override fun observeById(
        id: Long
    ): Flow<Medication?> =
        medicationDao.observeById(id)
            .map { medication -> medication?.toDomain() }

    override suspend fun findById(
        id: Long
    ): Medication? =
        medicationDao.findById(id)?.toDomain()

    override suspend fun findVersionsBySeries(
        profileId: Long,
        seriesId: Long
    ): List<Medication> =
        medicationDao.findVersionsByProfileAndSeries(
            profileId,
            seriesId
        ).map { it.toDomain() }

    override suspend fun create(
        medication: Medication
    ): Long = database.withTransaction {
        val entity = medication
            .copy(id = 0L, seriesId = 0L)
            .toEntity()

        val generatedId = medicationDao.insert(entity)

        medicationDao.update(
            entity.copy(
                id = generatedId,
                seriesId = generatedId
            )
        )

        generatedId
    }

    override suspend fun createVersion(
        medication: Medication
    ): Long = database.withTransaction {
        val seriesId = medication.seriesId
            .takeIf { it != 0L }
            ?: medication.id

        val entity = medication
            .copy(id = 0L, seriesId = seriesId)
            .toEntity()

        medicationDao.insert(entity)
    }

    override suspend fun update(
        medication: Medication
    ) {
        medicationDao.update(medication.toEntity())
    }

    override suspend fun updateAndCreateVersion(
        previousVersion: Medication,
        newVersion: Medication,
        previousEndDate: LocalDate
    ): Long = database.withTransaction {

        medicationDao.update(
            previousVersion.copy(
                endDate = previousEndDate
            ).toEntity()
        )

        val entity = newVersion.copy(
            id = 0L,
            seriesId = previousVersion.seriesId
        ).toEntity()

        medicationDao.insert(entity)
    }

    override suspend fun deleteSeries(
        profileId: Long,
        seriesId: Long
    ) {
        database.withTransaction {
            val versions =
                medicationDao.findVersionsByProfileAndSeries(
                    profileId = profileId,
                    seriesId = seriesId
                )

            if (versions.isNotEmpty()) {
                medicationDao.deleteByProfileAndSeries(
                    profileId = profileId,
                    seriesId = seriesId
                )
            }
        }
    }

    override suspend fun findAllWithReminders(): List<Medication> =
        medicationDao.findAllWithReminders()
            .map { entity -> entity.toDomain() }

    override fun observeAllWithReminders(): Flow<List<Medication>> =
        medicationDao.observeAllWithReminders()
            .map { medications ->
                medications.map { entity -> entity.toDomain() }
            }
}