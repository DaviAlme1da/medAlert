package com.davialmeida.medalert.today.domain.rules

import com.davialmeida.medalert.today.domain.model.Dose
import com.davialmeida.medalert.today.domain.model.DoseLog
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import com.davialmeida.medalert.today.domain.model.DoseStatus
import java.time.Instant
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DoseRulesTest {

    private val dose = Dose(
        medicationId = 1L,
        scheduledDateTime = LocalDateTime.of(
            2026,
            10,
            5,
            8,
            0
        )
    )

    @Test
    fun returnsPendingWhenNowIsBeforeScheduledTime() {
        val now = LocalDateTime.of(
            2026,
            10,
            5,
            7,
            59
        )

        val result = deriveDoseStatus(
            dose = dose,
            doseLog = null,
            now = now
        )

        assertEquals(DoseStatus.PENDING, result)
    }

    @Test
    fun returnsPendingWhenNowIsExactlyScheduledTime() {
        val now = LocalDateTime.of(
            2026,
            10,
            5,
            8,
            0
        )

        val result = deriveDoseStatus(
            dose = dose,
            doseLog = null,
            now = now
        )

        assertEquals(DoseStatus.PENDING, result)
    }

    @Test
    fun returnsPendingWhenNowIsWithinTolerance() {
        val now = LocalDateTime.of(
            2026,
            10,
            5,
            8,
            15
        )

        val result = deriveDoseStatus(
            dose = dose,
            doseLog = null,
            now = now
        )

        assertEquals(DoseStatus.PENDING, result)
    }

    @Test
    fun returnsPendingWhenNowIsExactlyAtToleranceLimit() {
        val now = LocalDateTime.of(
            2026,
            10,
            5,
            8,
            30
        )

        val result = deriveDoseStatus(
            dose = dose,
            doseLog = null,
            now = now
        )

        assertEquals(DoseStatus.PENDING, result)
    }

    @Test
    fun returnsLateWhenNowIsAfterTolerance() {
        val now = LocalDateTime.of(
            2026,
            10,
            5,
            8,
            31
        )

        val result = deriveDoseStatus(
            dose = dose,
            doseLog = null,
            now = now
        )

        assertEquals(DoseStatus.LATE, result)
    }

    @Test
    fun returnsTakenWhenDoseLogIsTaken() {
        val doseLog = DoseLog(
            id = 1L,
            medicationId = 1L,
            scheduledDateTime = dose.scheduledDateTime,
            status = DoseLogStatus.TAKEN,
            loggedAt = Instant.parse(
                "2026-10-05T11:10:00Z"
            )
        )

        val result = deriveDoseStatus(
            dose = dose,
            doseLog = doseLog,
            now = LocalDateTime.of(
                2026,
                10,
                5,
                10,
                0
            )
        )

        assertEquals(DoseStatus.TAKEN, result)
    }

    @Test
    fun returnsSkippedWhenDoseLogIsSkipped() {
        val doseLog = DoseLog(
            id = 1L,
            medicationId = 1L,
            scheduledDateTime = dose.scheduledDateTime,
            status = DoseLogStatus.SKIPPED,
            loggedAt = Instant.parse(
                "2026-10-05T11:10:00Z"
            )
        )

        val result = deriveDoseStatus(
            dose = dose,
            doseLog = doseLog,
            now = LocalDateTime.of(
                2026,
                10,
                5,
                10,
                0
            )
        )

        assertEquals(DoseStatus.SKIPPED, result)
    }
}