package com.davialmeida.medalert.today.domain.usecase

import com.davialmeida.medalert.medication.domain.usecase.FakeMedicationRepository
import com.davialmeida.medalert.medication.domain.usecase.sampleMedication
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ObserveDayScheduleUseCaseTest {

    private val date = LocalDate.of(2026, 10, 16)
    private val medicationRepository = FakeMedicationRepository()
    private val doseLogRepository = FakeDoseLogRepository()

    private val useCase = ObserveDayScheduleUseCase(
        medicationRepository,
        doseLogRepository
    )

    private fun log(medicationId: Long, day: LocalDate) = DoseLog(
        id = 0,
        medicationId = medicationId,
        scheduledDateTime = day.atTime(8, 0),
        status = DoseLogStatus.TAKEN,
        loggedAt = Instant.parse("2026-10-16T10:00:00Z"),
        takenAt = Instant.parse("2026-10-16T10:00:00Z")
    )

    @Test
    fun `schedule contains only medications active on the date`() = runTest {
        medicationRepository.data.add(sampleMedication(id = 1, name = "Active"))
        medicationRepository.data.add(
            sampleMedication(id = 2, name = "Ended", end = date.minusDays(1))
        )
        medicationRepository.data.add(
            sampleMedication(id = 3, name = "Future", start = date.plusDays(2))
        )

        val schedule = useCase(profileId = 1L, date = date).first()

        assertEquals(listOf("Active"), schedule.medications.map { it.name })
    }

    @Test
    fun `schedule contains only logs of the date`() = runTest {
        medicationRepository.data.add(sampleMedication(id = 1))
        doseLogRepository.addLog(log(1L, date))
        doseLogRepository.addLog(log(1L, date.minusDays(1)))

        val schedule = useCase(profileId = 1L, date = date).first()

        assertEquals(1, schedule.logs.size)
        assertEquals(date, schedule.logs.single().scheduledDateTime.toLocalDate())
    }

    @Test
    fun `schedule is empty when there is nothing on the date`() = runTest {
        val schedule = useCase(profileId = 1L, date = date).first()

        assertEquals(emptyList<Any>(), schedule.medications)
        assertEquals(emptyList<Any>(), schedule.logs)
    }
}