package com.davialmeida.medalert.today.data.repository

import androidx.room.withTransaction
import com.davialmeida.medalert.core.database.AppDatabase
import com.davialmeida.medalert.today.data.local.dao.DoseLogDao
import com.davialmeida.medalert.today.data.mapper.toDomain
import com.davialmeida.medalert.today.data.mapper.toEntity
import com.davialmeida.medalert.today.data.mapper.toMinutesOfDay
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.repository.DoseLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class DoseLogRepositoryImpl @Inject constructor(
    private val doseLogDao: DoseLogDao,
    private val database: AppDatabase
) : DoseLogRepository {

    override fun observeByProfileAndDate(
        profileId: Long,
        date: LocalDate
    ): Flow<List<DoseLog>> =
        doseLogDao.observeByProfileAndDate(
            profileId = profileId,
            date = date.toEpochDay()
        ).map { logs ->
            logs.map { it.toDomain() }
        }

    override fun observeByProfileInRange(
        profileId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<DoseLog>> =
        doseLogDao.observeForProfileInRange(
            profileId = profileId,
            startDate = startDate.toEpochDay(),
            endDate = endDate.toEpochDay()
        ).map { logs ->
            logs.map { it.toDomain() }
        }

    override suspend fun upsert(log: DoseLog) {
        database.withTransaction {
            val entity = log.toEntity()

            val existingLog = doseLogDao.findByDose(
                medicationId = entity.medicationId,
                scheduledDate = entity.scheduledDate,
                scheduledTime = entity.scheduledTime
            )

            if (existingLog != null) {
                doseLogDao.update(entity.copy(id = existingLog.id))
            } else {
                doseLogDao.insert(entity.copy(id = 0L))
            }
        }
    }

    override suspend fun delete(
        medicationId: Long,
        scheduledDateTime: LocalDateTime
    ) {
        doseLogDao.deleteByDose(
            medicationId = medicationId,
            scheduledDate = scheduledDateTime.toLocalDate().toEpochDay(),
            scheduledTime = scheduledDateTime.toMinutesOfDay()
        )
    }

    override suspend fun find(
        medicationId: Long,
        scheduledDateTime: LocalDateTime
    ): DoseLog? {
        val entity = doseLogDao.findByDose(
            medicationId = medicationId,
            scheduledDate = scheduledDateTime.toLocalDate().toEpochDay(),
            scheduledTime = scheduledDateTime.toMinutesOfDay()
        )

        return entity?.toDomain()
    }
}