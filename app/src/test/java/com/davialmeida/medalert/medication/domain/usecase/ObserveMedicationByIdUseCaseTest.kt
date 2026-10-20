package com.davialmeida.medalert.medication.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ObserveMedicationByIdUseCaseTest {
    private val today = LocalDate.of(2026, 10, 16)

    @Test
    fun `observe by id returns medication`() = runBlocking {

        val repo = FakeMedicationRepository().apply { data.add(sampleMedication()) }

        assertEquals(1L, ObserveMedicationByIdUseCase(repo)(1).first()?.id)

    }
}
