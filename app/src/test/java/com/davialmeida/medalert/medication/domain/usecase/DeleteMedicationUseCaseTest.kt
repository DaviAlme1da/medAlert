package com.davialmeida.medalert.medication.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DeleteMedicationUseCaseTest {
    private val today = LocalDate.of(2026, 10, 16)

    @Test
    fun `delete removes every version in series`() = runBlocking {

        val repo = FakeMedicationRepository().apply {

            data.add(sampleMedication(id = 1, seriesId = 1))

            data.add(sampleMedication(id = 2, seriesId = 1, start = today))

            data.add(sampleMedication(id = 3, seriesId = 3))

        }

        val result = DeleteMedicationUseCase(repo)(1)

        assertEquals(AppResult.Success(Unit), result)

        assertEquals(listOf(3L), repo.data.map { it.id })

    }


    @Test
    fun `delete missing medication returns error`() = runBlocking {

        assertTrue(DeleteMedicationUseCase(FakeMedicationRepository())(99) is AppResult.Error)

    }
}
