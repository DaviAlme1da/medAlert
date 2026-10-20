package com.davialmeida.medalert.medication.domain.repository

import com.davialmeida.medalert.medication.domain.model.Medication
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MedicationRepository {

    fun observeByProfile(
        profileId: Long
    ): Flow<List<Medication>>

    fun observeActiveOn(
        profileId: Long,
        date: LocalDate
    ): Flow<List<Medication>>

    fun observeById(
        id: Long
    ): Flow<Medication?>

    suspend fun findById(
        id: Long
    ): Medication?

    suspend fun findVersionsBySeries(
        profileId: Long,
        seriesId: Long
    ): List<Medication>

    suspend fun create(
        medication: Medication
    ): Long

    suspend fun createVersion(
        medication: Medication
    ): Long

    suspend fun update(
        medication: Medication
    )

    suspend fun updateAndCreateVersion(
        previousVersion: Medication,
        newVersion: Medication,
        previousEndDate: LocalDate
    ): Long

    suspend fun deleteSeries(
        profileId: Long,
        seriesId: Long
    )
}