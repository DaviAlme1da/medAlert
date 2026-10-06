package com.davialmeida.medalert.medication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.davialmeida.medalert.medication.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Query("""
        SELECT * FROM medications
        WHERE profileId = :profileId
        ORDER BY name
    """)
    fun observeByProfile(profileId: Long): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun findById(id: Long): MedicationEntity?

    @Query("""
        SELECT * FROM medications
        WHERE profileId = :profileId
        AND startDate <= :date
        AND (endDate IS NULL OR endDate >= :date)
        ORDER BY name
    """)
    fun observeActiveOn(
        profileId: Long,
        date: Long
    ): Flow<List<MedicationEntity>>

    @Insert
    suspend fun insert(medication: MedicationEntity): Long

    @Update
    suspend fun update(medication: MedicationEntity)

    @Delete
    suspend fun delete(medication: MedicationEntity)
}