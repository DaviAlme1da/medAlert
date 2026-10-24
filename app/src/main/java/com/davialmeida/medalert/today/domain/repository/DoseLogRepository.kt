package com.davialmeida.medalert.today.domain.repository

import com.davialmeida.medalert.today.domain.model.DoseLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface DoseLogRepository {

    fun observeByProfileAndDate(
        profileId: Long,
        date: LocalDate
    ): Flow<List<DoseLog>>

    fun observeByProfileInRange(
        profileId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<DoseLog>>

    suspend fun upsert(log: DoseLog)

    suspend fun delete(
        medicationId: Long,
        scheduledDateTime: LocalDateTime
    )

    suspend fun find(
        medicationId: Long,
        scheduledDateTime: LocalDateTime
    ): DoseLog?
}
