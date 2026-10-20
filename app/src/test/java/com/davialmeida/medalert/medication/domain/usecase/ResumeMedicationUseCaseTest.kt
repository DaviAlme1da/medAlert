package com.davialmeida.medalert.medication.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ResumeMedicationUseCaseTest {
    private val today = LocalDate.of(2026, 10, 16)

    @Test
    fun `resume creates new active version and preserves old version`() = runBlocking {

        val old = sampleMedication(end = today.minusDays(1))

        val repo = FakeMedicationRepository().apply { data.add(old) }

        val result = ResumeMedicationUseCase(repo)(old.id, today)

        assertTrue(result is AppResult.Success)

        assertEquals(2, repo.data.size)

        assertEquals(today, repo.data.last().startDate)

        assertNull(repo.data.last().endDate)

        assertEquals(old.endDate, repo.data.first().endDate)

        assertEquals(old.seriesId, repo.data.last().seriesId)

    }


    @Test
    fun `resume rejects medication still active`() = runBlocking {

        val repo = FakeMedicationRepository().apply { data.add(sampleMedication()) }

        assertTrue(ResumeMedicationUseCase(repo)(1, today) is AppResult.Error)

    }
}
