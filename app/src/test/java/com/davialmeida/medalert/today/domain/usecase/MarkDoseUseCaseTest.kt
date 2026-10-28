package com.davialmeida.medalert.today.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.usecase.FakeMedicationRepository
import com.davialmeida.medalert.medication.domain.usecase.sampleMedication
import com.davialmeida.medalert.today.domain.error.DoseError
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class MarkDoseUseCaseTest {

    private val nowInstant = Instant.parse("2026-10-16T10:00:00Z")
    private val clock = Clock.fixed(nowInstant, ZoneOffset.UTC)
    private val date = LocalDate.of(2026, 10, 16)

    private val medicationRepository = FakeMedicationRepository()
    private val doseLogRepository = FakeDoseLogRepository()

    private val listener = FakeDoseRegistrationListener()

    private val useCase = MarkDoseUseCase(
        medicationRepository = medicationRepository,
        doseLogRepository = doseLogRepository,
        registrationListener = listener,
        clock = clock
    )

    // Medicamento id = 1, diário às 08:00, começou em 11/10/2026.
    private fun addMedication() {
        medicationRepository.data.add(sampleMedication())
    }

    @Test
    fun `marking a dose as taken stores a taken log with the current instant`() = runTest {
        addMedication()

        val result = useCase(1L, date.atTime(8, 0), DoseLogStatus.TAKEN)

        assertEquals(AppResult.Success(Unit), result)

        val saved = doseLogRepository.logs.single()
        assertEquals(1L, saved.medicationId)
        assertEquals(date.atTime(8, 0), saved.scheduledDateTime)
        assertEquals(DoseLogStatus.TAKEN, saved.status)
        assertEquals(nowInstant, saved.loggedAt)
        assertEquals(nowInstant, saved.takenAt)
    }

    @Test
    fun `marking a dose as skipped stores no taken instant`() = runTest {
        addMedication()

        val result = useCase(1L, date.atTime(8, 0), DoseLogStatus.SKIPPED)

        assertEquals(AppResult.Success(Unit), result)

        val saved = doseLogRepository.logs.single()
        assertEquals(DoseLogStatus.SKIPPED, saved.status)
        assertEquals(nowInstant, saved.loggedAt)
        assertNull(saved.takenAt)
    }

    @Test
    fun `marking the same dose twice updates the log instead of creating another`() = runTest {
        addMedication()

        useCase(1L, date.atTime(8, 0), DoseLogStatus.TAKEN)
        useCase(1L, date.atTime(8, 0), DoseLogStatus.SKIPPED)

        val saved = doseLogRepository.logs.single()
        assertEquals(DoseLogStatus.SKIPPED, saved.status)
        assertNull(saved.takenAt)
    }

    @Test
    fun `dose from a past day can be marked`() = runTest {
        addMedication()

        val result = useCase(
            1L,
            date.minusDays(1).atTime(8, 0),
            DoseLogStatus.TAKEN
        )

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, doseLogRepository.logs.size)
    }

    @Test
    fun `dose later today can be marked before its time`() = runTest {
        medicationRepository.data.add(
            sampleMedication(
                frequency = Frequency.Daily(listOf(LocalTime.of(20, 0)))
            )
        )

        val result = useCase(1L, date.atTime(20, 0), DoseLogStatus.TAKEN)

        assertEquals(AppResult.Success(Unit), result)
    }

    @Test
    fun `dose from a future day returns FutureDose and stores nothing`() = runTest {
        addMedication()

        val result = useCase(
            1L,
            date.plusDays(1).atTime(8, 0),
            DoseLogStatus.TAKEN
        )

        assertEquals(AppResult.Error(DoseError.FutureDose), result)
        assertTrue(doseLogRepository.logs.isEmpty())
    }

    @Test
    fun `time that is not scheduled returns DoseNotFound`() = runTest {
        addMedication()

        val result = useCase(1L, date.atTime(9, 0), DoseLogStatus.TAKEN)

        assertEquals(AppResult.Error(DoseError.DoseNotFound), result)
        assertTrue(doseLogRepository.logs.isEmpty())
    }

    @Test
    fun `day before the medication started returns DoseNotFound`() = runTest {
        medicationRepository.data.add(sampleMedication(start = date))

        val result = useCase(
            1L,
            date.minusDays(1).atTime(8, 0),
            DoseLogStatus.TAKEN
        )

        assertEquals(AppResult.Error(DoseError.DoseNotFound), result)
    }

    @Test
    fun `missing medication returns MedicationNotFound`() = runTest {
        val result = useCase(99L, date.atTime(8, 0), DoseLogStatus.TAKEN)

        assertEquals(AppResult.Error(DoseError.MedicationNotFound), result)
        assertTrue(doseLogRepository.logs.isEmpty())
    }

    @Test
    fun `repository failure returns UnexpectedAppError`() = runTest {
        addMedication()
        doseLogRepository.failOnUpsert = true

        val result = useCase(1L, date.atTime(8, 0), DoseLogStatus.TAKEN)

        assertEquals(AppResult.Error(UnexpectedAppError), result)
    }

    @Test
    fun `listener is told when a dose is registered`() = runTest {
        addMedication()

        useCase(1L, date.atTime(8, 0), DoseLogStatus.TAKEN)

        assertEquals(listOf(1L to date.atTime(8, 0)), listener.registered)
    }

    @Test
    fun `listener is not called when the registration fails`() = runTest {
        addMedication()

        useCase(1L, date.plusDays(1).atTime(8, 0), DoseLogStatus.TAKEN)

        assertTrue(listener.registered.isEmpty())
    }

    @Test
    fun `listener failure does not fail the registration`() = runTest {
        addMedication()
        listener.failOnRegister = true

        val result = useCase(1L, date.atTime(8, 0), DoseLogStatus.TAKEN)

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, doseLogRepository.logs.size)
    }

}