package com.davialmeida.medalert.today.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.davialmeida.medalert.today.data.local.entity.DoseLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {

    @Query("""
        SELECT dose_logs.*
        FROM dose_logs
        INNER JOIN medications
            ON medications.id = dose_logs.medicationId
        WHERE medications.profileId = :profileId
        AND dose_logs.scheduledDate >= :startDate
        AND dose_logs.scheduledDate <= :endDate
        ORDER BY dose_logs.scheduledDate, dose_logs.scheduledTime
    """)
    fun observeForProfileInRange(
        profileId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE id = :id")
    suspend fun findById(id: Long): DoseLogEntity?

    @Query("""
        SELECT * FROM dose_logs
        WHERE medicationId = :medicationId
        AND scheduledDate = :scheduledDate
        AND scheduledTime = :scheduledTime
    """)
    suspend fun findByDose(
        medicationId: Long,
        scheduledDate: Long,
        scheduledTime: Int
    ): DoseLogEntity?

    @Insert
    suspend fun insert(doseLog: DoseLogEntity): Long

    @Update
    suspend fun update(doseLog: DoseLogEntity)

    @Delete
    suspend fun delete(doseLog: DoseLogEntity)
}