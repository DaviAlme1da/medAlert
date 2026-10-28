package com.davialmeida.medalert.reminder.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.medication.domain.model.Dosage
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.medication.domain.usecase.FakeMedicationRepository
import com.davialmeida.medalert.medication.domain.usecase.sampleMedication
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import com.davialmeida.medalert.reminder.domain.model.DueReminder
import com.davialmeida.medalert.testutil.TestProfileRepository
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import com.davialmeida.medalert.today.domain.usecase.FakeDoseLogRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

class GetSnoozedReminderUseCaseTest {

    private val date = LocalDate.of(2026, 10, 16)
    private val dose = date.atTime(8, 0)

    private val medicationRepository = FakeMedicationRepository()
    private val doseLogRepository = FakeDoseLogRepository()
    private val profileRepository = TestProfileRepository(
        listOf(Profile(1, "Pessoal", ProfileColor.BLUE))
    )

    private fun useCase(repository: MedicationRepository = medicationRepository) =
        GetSnoozedReminderUseCase(repository, doseLogRepository, profileRepository)

    private fun nullResult(): AppResult<DueReminder?> = AppResult.Success(null)

    @Test
    fun `returns the reminder when the dose is still pending`() = runTest {
        medicationRepository.data.add(sampleMedication(name = "Paracetamol"))

        val result = useCase()(1L, dose)

        val expected = DueReminder(
            medicationId = 1,
            medicationName = "Paracetamol",
            dosage = Dosage(BigDecimal("500"), DosageUnit.MG),
            profileName = "Pessoal",
            scheduledDateTime = dose
        )
        assertEquals(AppResult.Success<DueReminder?>(expected), result)
    }

    @Test
    fun `returns nothing when the dose already has a log`() = runTest {
        medicationRepository.data.add(sampleMedication())
        doseLogRepository.addLog(
            DoseLog(
                id = 0,
                medicationId = 1,
                scheduledDateTime = dose,
                status = DoseLogStatus.TAKEN,
                loggedAt = Instant.parse("2026-10-16T07:00:00Z"),
                takenAt = Instant.parse("2026-10-16T07:00:00Z")
            )
        )

        assertEquals(nullResult(), useCase()(1L, dose))
    }

    @Test
    fun `returns nothing when reminders were turned off`() = runTest {
        medicationRepository.data.add(sampleMedication().copy(remindersEnabled = false))

        assertEquals(nullResult(), useCase()(1L, dose))
    }

    @Test
    fun `returns nothing when the medication no longer exists`() = runTest {
        assertEquals(nullResult(), useCase()(99L, dose))
    }

    @Test
    fun `returns nothing when no dose is scheduled at that time`() = runTest {
        medicationRepository.data.add(sampleMedication())

        assertEquals(nullResult(), useCase()(1L, date.atTime(9, 0)))
    }

    @Test
    fun `returns nothing when the medication version already ended`() = runTest {
        medicationRepository.data.add(sampleMedication(end = date.minusDays(1)))

        assertEquals(nullResult(), useCase()(1L, dose))
    }

    @Test
    fun `returns nothing for a day before the medication started`() = runTest {
        medicationRepository.data.add(sampleMedication(start = date))

        assertEquals(nullResult(), useCase()(1L, date.minusDays(1).atTime(8, 0)))
    }

    @Test
    fun `a repository failure becomes an error`() = runTest {
        val failing = object : MedicationRepository by FakeMedicationRepository() {
            override suspend fun findById(id: Long): Medication? =
                throw IllegalStateException("boom")
        }

        assertEquals(AppResult.Error(UnexpectedAppError), useCase(failing)(1L, dose))
    }
}