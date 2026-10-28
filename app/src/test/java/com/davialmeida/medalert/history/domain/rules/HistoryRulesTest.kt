package com.davialmeida.medalert.history.domain.rules

import com.davialmeida.medalert.history.domain.model.HistoryPeriod
import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.usecase.sampleMedication
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import com.davialmeida.medalert.today.domain.model.DoseStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class HistoryRulesTest {


    private val date = LocalDate.of(2026, 10, 16)
    private val noon = date.atTime(12, 0)

    private fun log(
        day: LocalDate,
        status: DoseLogStatus = DoseLogStatus.TAKEN,
        medicationId: Long = 1
    ) = DoseLog(
        id = 0,
        medicationId = medicationId,
        scheduledDateTime = day.atTime(8, 0),
        status = status,
        loggedAt = Instant.parse("2026-10-16T12:00:00Z"),
        takenAt = if (status == DoseLogStatus.TAKEN) Instant.parse("2026-10-16T12:00:00Z") else null
    )

    private fun history(
        medications: List<Medication>,
        logs: List<DoseLog> = emptyList(),
        period: HistoryPeriod = HistoryPeriod.LAST_7_DAYS,
        now: LocalDateTime = noon
    ) = buildHistory(medications, logs, period, now.toLocalDate(), now)


    @Test
    fun `days are listed from the most recent to the oldest`() {
        val days = history(listOf(sampleMedication(start = date.minusDays(2))))

        assertEquals(
            listOf(date, date.minusDays(1), date.minusDays(2)),
            days.map { it.date }
        )
    }

    @Test
    fun `days before the medication started are omitted`() {
        val days = history(listOf(sampleMedication(start = date.minusDays(1))))

        assertEquals(2, days.size)
    }

    @Test
    fun `days after the medication ended are omitted`() {
        val medication = sampleMedication(start = date.minusDays(10), end = date.minusDays(3))

        val days = history(listOf(medication))

        assertEquals(4, days.size)
        assertEquals(date.minusDays(3), days.first().date)
    }

    @Test
    fun `seven day period covers exactly seven days`() {
        val days = history(listOf(sampleMedication(start = date.minusDays(30))))

        assertEquals(7, days.size)
        assertEquals(date.minusDays(6), days.last().date)
    }

    @Test
    fun `thirty day period covers exactly thirty days`() {
        val days = history(
            listOf(sampleMedication(start = date.minusDays(40))),
            period = HistoryPeriod.LAST_30_DAYS
        )

        assertEquals(30, days.size)
        assertEquals(date.minusDays(29), days.last().date)
    }

    @Test
    fun `no medications produce an empty history`() {
        assertTrue(history(emptyList()).isEmpty())
    }

    @Test
    fun `past dose without log is late and a logged one is taken`() {
        val days = history(
            medications = listOf(sampleMedication(start = date.minusDays(2))),
            logs = listOf(log(date.minusDays(1)))
        )

        assertEquals(
            listOf(DoseStatus.LATE, DoseStatus.TAKEN, DoseStatus.LATE),
            days.map { it.items.single().status }
        )
    }

    @Test
    fun `skipped log produces a skipped dose`() {
        val days = history(
            medications = listOf(sampleMedication(start = date.minusDays(1))),
            logs = listOf(log(date.minusDays(1), DoseLogStatus.SKIPPED))
        )

        assertEquals(DoseStatus.SKIPPED, days.last().items.single().status)
    }

    @Test
    fun `dose later today is still pending`() {
        val medication = sampleMedication(
            frequency = Frequency.Daily(listOf(LocalTime.of(20, 0)))
        )

        val today = history(listOf(medication)).first()

        assertEquals(DoseStatus.PENDING, today.items.single().status)
    }

    @Test
    fun `log of another day does not change the status`() {
        val days = history(
            medications = listOf(sampleMedication(start = date.minusDays(1))),
            logs = listOf(log(date.minusDays(1)))
        )

        assertEquals(DoseStatus.LATE, days.first().items.single().status)
    }

    @Test
    fun `filterBySeries without a series returns everything`() {
        val medications = listOf(sampleMedication(id = 1), sampleMedication(id = 2))

        assertEquals(medications, filterBySeries(medications, null))
    }

    @Test
    fun `filterBySeries keeps every version of the chosen series`() {
        val medications = listOf(
            sampleMedication(id = 1, seriesId = 1),
            sampleMedication(id = 2, seriesId = 1),
            sampleMedication(id = 3, seriesId = 3)
        )

        assertEquals(listOf(1L, 2L), filterBySeries(medications, 1L).map { it.id })
    }

    @Test
    fun `filterBySeries with an unknown series returns nothing`() {
        assertTrue(filterBySeries(listOf(sampleMedication()), 99L).isEmpty())
    }

    @Test
    fun `adherence counts taken skipped and late doses`() {
        val days = history(
            medications = listOf(sampleMedication(start = date.minusDays(3))),
            logs = listOf(
                log(date.minusDays(3), DoseLogStatus.TAKEN),
                log(date.minusDays(2), DoseLogStatus.SKIPPED)
            )
        )

        val summary = calculateAdherence(days)

        assertEquals(1, summary.taken)
        assertEquals(1, summary.skipped)
        assertEquals(2, summary.late)
        assertEquals(25, summary.percentage)
    }

    @Test
    fun `pending doses are left out of the adherence`() {
        val days = history(
            medications = listOf(sampleMedication(start = date.minusDays(2))),
            logs = listOf(log(date.minusDays(2))),
            now = date.atTime(7, 0)
        )

        val summary = calculateAdherence(days)

        assertEquals(1, summary.taken)
        assertEquals(1, summary.late)
        assertEquals(50, summary.percentage)
    }

    @Test
    fun `adherence of an empty history has no percentage`() {
        assertNull(calculateAdherence(emptyList()).percentage)
    }
}