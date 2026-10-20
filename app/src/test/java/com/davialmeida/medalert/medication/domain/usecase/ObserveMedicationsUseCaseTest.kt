package com.davialmeida.medalert.medication.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ObserveMedicationsUseCaseTest {
    private val today = LocalDate.of(2026, 10, 16)

    @Test
    fun `observe medications returns profile medications`() = runBlocking {

        val repo = FakeMedicationRepository().apply {

            data.add(sampleMedication(id = 1, seriesId = 1))

            data.add(sampleMedication(id = 2, seriesId = 2).copy(profileId = 2))

        }

        val result = ObserveMedicationsUseCase(repo)(1)

        assertEquals(1, result.first().size)

    }
}
