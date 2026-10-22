package com.davialmeida.medalert.today.domain.usecase

import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.repository.DoseLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime

internal class FakeDoseLogRepository : DoseLogRepository {

    var failOnUpsert = false
    var failOnDelete = false

    private val state = MutableStateFlow<List<DoseLog>>(emptyList())
    private var nextId = 1L

    val logs: List<DoseLog>
        get() = state.value

    fun addLog(log: DoseLog) {
        state.value = state.value + log.copy(id = nextId++)
    }

    override fun observeByProfileAndDate(
        profileId: Long,
        date: LocalDate
    ): Flow<List<DoseLog>> {
        return state.map { logs ->
            logs.filter { it.scheduledDateTime.toLocalDate() == date }
        }
    }

    override suspend fun upsert(log: DoseLog) {
        if (failOnUpsert) {
            throw IllegalStateException("Simulated upsert failure")
        }

        val existing = state.value.firstOrNull {
            it.medicationId == log.medicationId &&
                    it.scheduledDateTime == log.scheduledDateTime
        }

        state.value = if (existing != null) {
            state.value.map {
                if (it.id == existing.id) log.copy(id = existing.id) else it
            }
        } else {
            state.value + log.copy(id = nextId++)
        }
    }

    override suspend fun delete(
        medicationId: Long,
        scheduledDateTime: LocalDateTime
    ) {
        if (failOnDelete) {
            throw IllegalStateException("Simulated delete failure")
        }

        state.value = state.value.filterNot {
            it.medicationId == medicationId &&
                    it.scheduledDateTime == scheduledDateTime
        }
    }
}