package com.davialmeida.medalert.medication.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ObserveActiveMedicationsUseCaseTest {
    private val today = LocalDate.of(2026, 10, 16)

    @Test
    fun `observe active medications filters by date`() = runBlocking {

        val repo = FakeMedicationRepository().apply {

            data.add(sampleMedication(id = 1, seriesId = 1))

            data.add(sampleMedication(id = 2, seriesId = 2, start = today.plusDays(1)))

        }

        assertEquals(1, ObserveActiveMedicationsUseCase(repo)(1, today).first().size)

    }
}
