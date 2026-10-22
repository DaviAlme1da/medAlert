package com.davialmeida.medalert.today.domain.rules

import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.usecase.sampleMedication
import com.davialmeida.medalert.today.domain.model.DayProgress
import com.davialmeida.medalert.today.domain.model.DaySchedule
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import com.davialmeida.medalert.today.domain.model.DoseStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DayDoseRulesTest {

    private val date = LocalDate.of(2026, 10, 16)
    private val eight = LocalTime.of(8, 0)

    private fun log(
        medicationId: Long = 1,
        time: LocalTime = eight,
        status: DoseLogStatus = DoseLogStatus.TAKEN,
        day: LocalDate = date
    ) = DoseLog(
        id = 1,
        medicationId = medicationId,
        scheduledDateTime = day.atTime(time),
        status = status,
        loggedAt = Instant.parse("2026-10-16T12:00:00Z"),
        takenAt = if (status == DoseLogStatus.TAKEN) {
            Instant.parse("2026-10-16T12:00:00Z")
        } else {
            null
        }
    )

    private fun now(hour: Int, minute: Int): LocalDateTime =
        date.atTime(hour, minute)


    @Test
    fun `dose before its scheduled time is pending`() {
        val items = buildDoseItems(
            DaySchedule(listOf(sampleMedication()), emptyList()),
            date,
            now(7, 0)
        )

        assertEquals(DoseStatus.PENDING, items.single().status)
    }

    @Test
    fun `dose exactly at the tolerance limit is still pending`() {
        val items = buildDoseItems(
            DaySchedule(listOf(sampleMedication()), emptyList()),
            date,
            now(8, 30)
        )

        assertEquals(DoseStatus.PENDING, items.single().status)
    }

    @Test
    fun `dose after the tolerance limit is late`() {
        val items = buildDoseItems(
            DaySchedule(listOf(sampleMedication()), emptyList()),
            date,
            now(8, 31)
        )

        assertEquals(DoseStatus.LATE, items.single().status)
    }

    @Test
    fun `dose with taken log is taken even when it would be late`() {
        val items = buildDoseItems(
            DaySchedule(listOf(sampleMedication()), listOf(log())),
            date,
            now(23, 0)
        )

        assertEquals(DoseStatus.TAKEN, items.single().status)
        assertEquals(1L, items.single().log?.id)
    }

    @Test
    fun `dose with skipped log is skipped`() {
        val items = buildDoseItems(
            DaySchedule(
                listOf(sampleMedication()),
                listOf(log(status = DoseLogStatus.SKIPPED))
            ),
            date,
            now(23, 0)
        )

        assertEquals(DoseStatus.SKIPPED, items.single().status)
    }

    @Test
    fun `future date is always pending`() {
        val tomorrow = date.plusDays(1)

        val items = buildDoseItems(
            DaySchedule(listOf(sampleMedication()), emptyList()),
            tomorrow,
            now(23, 0)
        )

        assertEquals(DoseStatus.PENDING, items.single().status)
    }

    @Test
    fun `past date without log is late`() {
        val yesterday = date.minusDays(1)

        val items = buildDoseItems(
            DaySchedule(listOf(sampleMedication()), emptyList()),
            yesterday,
            now(10, 0)
        )

        assertEquals(DoseStatus.LATE, items.single().status)
    }


    @Test
    fun `log is matched only to its own medication`() {
        val first = sampleMedication(id = 1, name = "Aspirin")
        val second = sampleMedication(id = 2, name = "Zinc")

        val items = buildDoseItems(
            DaySchedule(listOf(first, second), listOf(log(medicationId = 2))),
            date,
            now(7, 0)
        )

        assertNull(items.first { it.medication.id == 1L }.log)
        assertEquals(
            DoseStatus.TAKEN,
            items.first { it.medication.id == 2L }.status
        )
    }

    @Test
    fun `items are sorted by time and then by name`() {
        val zinc = sampleMedication(id = 1, name = "Zinc")
        val aspirin = sampleMedication(id = 2, name = "Aspirin")
        val vitamin = sampleMedication(
            id = 3,
            name = "Vitamin",
            frequency = Frequency.Daily(listOf(LocalTime.of(7, 0)))
        )

        val items = buildDoseItems(
            DaySchedule(listOf(zinc, aspirin, vitamin), emptyList()),
            date,
            now(6, 0)
        )

        assertEquals(
            listOf("Vitamin", "Aspirin", "Zinc"),
            items.map { it.medication.name }
        )
    }

    @Test
    fun `log without a matching calculated dose is ignored`() {
        val items = buildDoseItems(
            DaySchedule(
                listOf(sampleMedication()),
                listOf(log(time = LocalTime.of(9, 0)))
            ),
            date,
            now(7, 0)
        )

        assertEquals(1, items.size)
        assertNull(items.single().log)
        assertEquals(DoseStatus.PENDING, items.single().status)
    }

    @Test
    fun `medication that already ended produces no doses`() {
        val ended = sampleMedication(end = date.minusDays(1))

        val items = buildDoseItems(
            DaySchedule(listOf(ended), emptyList()),
            date,
            now(7, 0)
        )

        assertTrue(items.isEmpty())
    }

    @Test
    fun `daily medication with two times produces two doses`() {
        val medication = sampleMedication(
            frequency = Frequency.Daily(listOf(eight, LocalTime.of(20, 0)))
        )

        val items = buildDoseItems(
            DaySchedule(listOf(medication), emptyList()),
            date,
            now(7, 0)
        )

        assertEquals(2, items.size)
    }

    @Test
    fun `progress of empty list is zero of zero`() {
        assertEquals(DayProgress(0, 0), calculateProgress(emptyList()))
    }

    @Test
    fun `progress counts only taken doses and skipped counts in total`() {
        val medication = sampleMedication(
            frequency = Frequency.Daily(
                listOf(LocalTime.of(8, 0), LocalTime.of(14, 0), LocalTime.of(20, 0))
            )
        )

        val items = buildDoseItems(
            DaySchedule(
                listOf(medication),
                listOf(
                    log(time = LocalTime.of(8, 0), status = DoseLogStatus.TAKEN),
                    log(time = LocalTime.of(14, 0), status = DoseLogStatus.SKIPPED)
                )
            ),
            date,
            now(15, 0)
        )

        assertEquals(DayProgress(taken = 1, total = 3), calculateProgress(items))
    }

    @Test
    fun `isDoseOf is true for a scheduled time on an active day`() {
        assertTrue(isDoseOf(sampleMedication(), date.atTime(eight)))
    }

    @Test
    fun `isDoseOf is false for a time that is not scheduled`() {
        assertFalse(isDoseOf(sampleMedication(), date.atTime(9, 0)))
    }

    @Test
    fun `isDoseOf is false before the medication starts`() {
        val medication = sampleMedication(start = date.plusDays(1))

        assertFalse(isDoseOf(medication, date.atTime(eight)))
    }

    @Test
    fun `isDoseOf is false after the medication ended`() {
        val medication = sampleMedication(end = date.minusDays(1))

        assertFalse(isDoseOf(medication, date.atTime(eight)))
    }

    @Test
    fun `isDoseOf is false on a weekday that is not selected`() {
        val medication = sampleMedication(
            frequency = Frequency.SpecificDays(
                daysOfWeek = setOf(DayOfWeek.MONDAY),
                times = listOf(eight)
            )
        )

        // 16/10/2026 é sexta-feira.
        assertFalse(isDoseOf(medication, date.atTime(eight)))
    }
}