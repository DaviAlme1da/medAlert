package com.davialmeida.medalert.medication.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PauseMedicationUseCaseTest {
    private val today = LocalDate.of(2026, 10, 16)

    @Test
    fun `pause closes active medication yesterday`() = runBlocking {

        val repo = FakeMedicationRepository().apply { data.add(sampleMedication()) }

        val result = PauseMedicationUseCase(repo)(1, today)

        assertEquals(AppResult.Success(Unit), result)

        assertEquals(today.minusDays(1), repo.data.single().endDate)

    }


    @Test
    fun `pause rejects medication that starts today`() = runBlocking {

        val repo = FakeMedicationRepository().apply { data.add(sampleMedication(start = today)) }

        assertEquals(
            AppResult.Error(com.davialmeida.medalert.medication.domain.error.MedicationError.CannotPause),

            PauseMedicationUseCase(repo)(1, today)
        )

    }


    @Test
    fun `pause returns not found for missing medication`() = runBlocking {

        val result = PauseMedicationUseCase(FakeMedicationRepository())(404, today)

        assertTrue(result is AppResult.Error)

    }
}
