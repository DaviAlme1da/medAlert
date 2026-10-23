package com.davialmeida.medalert.reminder.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.medication.domain.model.Dosage
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.medication.domain.usecase.FakeMedicationRepository
import com.davialmeida.medalert.medication.domain.usecase.FakeProfileRepository
import com.davialmeida.medalert.medication.domain.usecase.sampleMedication
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import com.davialmeida.medalert.reminder.domain.model.DueReminder
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import com.davialmeida.medalert.today.domain.usecase.FakeDoseLogRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class GetDueRemindersUseCaseTest {

    private val date = LocalDate.of(2026, 10, 16)
    private val profile = Profile(1, "Pessoal", ProfileColor.BLUE)

    private val medicationRepository = FakeMedicationRepository()
    private val doseLogRepository = FakeDoseLogRepository()
    private val profileRepository = FakeProfileRepository(
        active = profile,
        profiles = listOf(profile)
    )

    private fun useCase(
        nowInstant: String = "2026-10-16T08:00:00Z",
        repository: MedicationRepository = medicationRepository
    ) = GetDueRemindersUseCase(
        repository,
        doseLogRepository,
        profileRepository,
        Clock.fixed(Instant.parse(nowInstant), ZoneOffset.UTC)
    )

    private fun trigger(time: String) = Instant.parse("2026-10-16T${time}:00Z")

    private fun daily(id: Long, vararg times: LocalTime): Medication =
        sampleMedication(
            id = id,
            name = "Med$id",
            frequency = Frequency.Daily(times.toList())
        )

    private fun takenLog(medicationId: Long, dateTime: LocalDateTime) = DoseLog(
        id = 0,
        medicationId = medicationId,
        scheduledDateTime = dateTime,
        status = DoseLogStatus.TAKEN,
        loggedAt = Instant.parse("2026-10-16T07:00:00Z"),
        takenAt = Instant.parse("2026-10-16T07:00:00Z")
    )

    @Test
    fun `returns the dose scheduled at the trigger with profile and dosage`() = runTest {
        medicationRepository.data.add(
            sampleMedication(
                id = 1,
                name = "Paracetamol",
                frequency = Frequency.Daily(listOf(LocalTime.of(8, 0)))
            )
        )

        val result = useCase()(trigger("08:00"))

        val expected = DueReminder(
            medicationId = 1,
            medicationName = "Paracetamol",
            dosage = Dosage(BigDecimal("500"), DosageUnit.MG),
            profileName = "Pessoal",
            scheduledDateTime = date.atTime(8, 0)
        )
        assertEquals(AppResult.Success(listOf(expected)), result)
    }

    @Test
    fun `returns nothing when no dose is scheduled at the trigger`() = runTest {
        medicationRepository.data.add(daily(1, LocalTime.of(8, 0)))

        val result = useCase()(trigger("09:00"))

        assertEquals(AppResult.Success(emptyList<DueReminder>()), result)
    }

    @Test
    fun `does not repeat an earlier dose when another one is close`() = runTest {
        medicationRepository.data.add(daily(1, LocalTime.of(8, 0)))
        medicationRepository.data.add(daily(2, LocalTime.of(8, 5)))

        val result = useCase()(trigger("08:05")) as AppResult.Success

        assertEquals(listOf("Med2"), result.data.map { it.medicationName })
    }

    @Test
    fun `returns every medication scheduled at the same time`() = runTest {
        medicationRepository.data.add(daily(1, LocalTime.of(8, 0)))
        medicationRepository.data.add(daily(2, LocalTime.of(8, 0)))

        val result = useCase()(trigger("08:00")) as AppResult.Success

        assertEquals(setOf("Med1", "Med2"), result.data.map { it.medicationName }.toSet())
    }

    @Test
    fun `skips a dose that already has a log`() = runTest {
        medicationRepository.data.add(daily(1, LocalTime.of(8, 0)))
        doseLogRepository.addLog(takenLog(1, date.atTime(8, 0)))

        val result = useCase()(trigger("08:00"))

        assertEquals(AppResult.Success(emptyList<DueReminder>()), result)
    }

    @Test
    fun `uses the alarm trigger and not the current time`() = runTest {
        medicationRepository.data.add(daily(1, LocalTime.of(8, 0)))

        val result = useCase(nowInstant = "2026-10-16T08:03:00Z")(trigger("08:00"))
                as AppResult.Success

        assertEquals(1, result.data.size)
    }

    @Test
    fun `ignores medications with reminders disabled`() = runTest {
        medicationRepository.data.add(
            daily(1, LocalTime.of(8, 0)).copy(remindersEnabled = false)
        )

        val result = useCase()(trigger("08:00"))

        assertEquals(AppResult.Success(emptyList<DueReminder>()), result)
    }

    @Test
    fun `uses an empty profile name when the profile is missing`() = runTest {
        medicationRepository.data.add(daily(1, LocalTime.of(8, 0)).copy(profileId = 99))

        val result = useCase()(trigger("08:00")) as AppResult.Success

        assertEquals("", result.data.single().profileName)
    }

    @Test
    fun `returns an error when the repository fails`() = runTest {
        val failing = object : MedicationRepository by FakeMedicationRepository() {
            override suspend fun findAllWithReminders(): List<Medication> =
                throw IllegalStateException("boom")
        }

        val result = useCase(repository = failing)(trigger("08:00"))

        assertTrue(result is AppResult.Error)
        assertEquals(AppResult.Error(UnexpectedAppError), result)
    }
}