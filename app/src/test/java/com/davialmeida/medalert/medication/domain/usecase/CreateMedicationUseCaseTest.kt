package com.davialmeida.medalert.medication.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CreateMedicationUseCaseTest {
    private val profile = Profile(1, "Pessoal", ProfileColor.BLUE)

    private val today = LocalDate.of(2026, 10, 16)

    @Test
    fun `create medication uses active profile and trims name`() = runBlocking {

        val repo = FakeMedicationRepository()

        val useCase = CreateMedicationUseCase(repo, FakeProfileRepository(profile))

        val result = useCase(sampleMedication(id = 0, seriesId = 0, name = "  Remédio  "))

        assertTrue(result is AppResult.Success)

        assertEquals("Remédio", repo.data.single().name)

        assertEquals(1L, repo.data.single().profileId)

        assertEquals(repo.data.single().id, repo.data.single().seriesId)

    }


    @Test
    fun `create medication without active profile returns error`() = runBlocking {

        val result =
            CreateMedicationUseCase(FakeMedicationRepository(), FakeProfileRepository(null))

                .invoke(sampleMedication())

        assertTrue(result is AppResult.Error)

    }


    @Test
    fun `create medication rejects invalid data`() = runBlocking {

        val repo = FakeMedicationRepository()

        val result = CreateMedicationUseCase(repo, FakeProfileRepository(profile))

            .invoke(sampleMedication(name = " "))

        assertTrue(result is AppResult.Error)

        assertTrue(repo.data.isEmpty())

    }
}
