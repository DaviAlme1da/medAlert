package com.davialmeida.medalert.medication.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.medication.domain.error.MedicationError
import com.davialmeida.medalert.medication.domain.model.Dosage
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.medication.domain.model.Frequency
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalTime

class UpdateMedicationUseCaseTest {

    private fun repositoryWith(vararg medications: com.davialmeida.medalert.medication.domain.model.Medication) =
        FakeMedicationRepository().apply { data.addAll(medications) }

    @Test
    fun `update without dosage or frequency change edits same version`() = runTest {
        val original = sampleMedication()
        val repository = repositoryWith(original)

        val result = UpdateMedicationUseCase(repository)(
            original.copy(name = "  New name "),
            today
        )

        assertEquals(AppResult.Success(1L), result)
        assertEquals(1, repository.data.size)
        assertEquals("New name", repository.data.single().name)
    }

    @Test
    fun `changing dosage of older active medication creates new version`() = runTest {
        val original = sampleMedication()
        val repository = repositoryWith(original)

        val result = UpdateMedicationUseCase(repository)(
            original.copy(dosage = Dosage(BigDecimal("750"), DosageUnit.MG)),
            today
        )

        assertTrue(result is AppResult.Success)
        assertEquals(2, repository.data.size)
        assertEquals(today.minusDays(1), repository.data.first().endDate)
        assertEquals(today, repository.data.last().startDate)
        assertEquals(original.seriesId, repository.data.last().seriesId)
    }

    @Test
    fun `same dosage with different scale does not create new version`() = runTest {
        val original = sampleMedication(dosage = BigDecimal("500"))
        val repository = repositoryWith(original)

        UpdateMedicationUseCase(repository)(
            original.copy(dosage = Dosage(BigDecimal("500.00"), DosageUnit.MG)),
            today
        )

        assertEquals(1, repository.data.size)
    }

    @Test
    fun `same times in different order do not create new version`() = runTest {
        val original = sampleMedication(
            frequency = Frequency.Daily(listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)))
        )
        val repository = repositoryWith(original)

        UpdateMedicationUseCase(repository)(
            original.copy(
                frequency = Frequency.Daily(listOf(LocalTime.of(20, 0), LocalTime.of(8, 0)))
            ),
            today
        )

        assertEquals(1, repository.data.size)
    }

    @Test
    fun `changing dosage of medication that starts today edits in place`() = runTest {
        val original = sampleMedication(start = today)
        val repository = repositoryWith(original)

        UpdateMedicationUseCase(repository)(
            original.copy(dosage = Dosage(BigDecimal("750"), DosageUnit.MG)),
            today
        )

        assertEquals(1, repository.data.size)
        assertEquals(BigDecimal("750"), repository.data.single().dosage.amount)
    }

    @Test
    fun `scheduled medication can be edited`() = runTest {
        val original = sampleMedication(start = today.plusDays(3))
        val repository = repositoryWith(original)

        val result = UpdateMedicationUseCase(repository)(
            original.copy(name = "Scheduled"),
            today
        )

        assertTrue(result is AppResult.Success)
        assertEquals("Scheduled", repository.data.single().name)
    }

    @Test
    fun `ended medication cannot be edited`() = runTest {
        val original = sampleMedication(end = today.minusDays(1))
        val repository = repositoryWith(original)

        val result = UpdateMedicationUseCase(repository)(
            original.copy(name = "Other"),
            today
        )

        assertEquals(AppResult.Error(MedicationError.CannotEdit), result)
        assertEquals("Paracetamol", repository.data.single().name)
    }

    @Test
    fun `end date in the past is rejected`() = runTest {
        val original = sampleMedication()
        val repository = repositoryWith(original)

        val result = UpdateMedicationUseCase(repository)(
            original.copy(endDate = today.minusDays(2)),
            today
        )

        assertEquals(AppResult.Error(MedicationError.EndDateInPast), result)
    }

    @Test
    fun `end date before original start date is rejected`() = runTest {
        val original = sampleMedication(start = today.plusDays(5))
        val repository = repositoryWith(original)

        val result = UpdateMedicationUseCase(repository)(
            original.copy(endDate = today.plusDays(2)),
            today
        )

        assertEquals(AppResult.Error(MedicationError.EndDateBeforeStartDate), result)
    }

    @Test
    fun `start date sent by caller is ignored`() = runTest {
        val original = sampleMedication()
        val repository = repositoryWith(original)

        UpdateMedicationUseCase(repository)(
            original.copy(startDate = today.plusDays(10)),
            today
        )

        assertEquals(original.startDate, repository.data.single().startDate)
    }

    @Test
    fun `missing medication returns NotFound`() = runTest {
        val result = UpdateMedicationUseCase(FakeMedicationRepository())(
            sampleMedication(),
            today
        )

        assertEquals(AppResult.Error(MedicationError.NotFound), result)
    }

    @Test
    fun `older version of a series cannot be edited`() = runTest {
        val old = sampleMedication(id = 1, seriesId = 1, end = today.minusDays(1))
        val latest = sampleMedication(id = 2, seriesId = 1, start = today)
        val repository = repositoryWith(old, latest)

        val result = UpdateMedicationUseCase(repository)(old, today)

        assertEquals(AppResult.Error(MedicationError.NotFound), result)
    }
}
