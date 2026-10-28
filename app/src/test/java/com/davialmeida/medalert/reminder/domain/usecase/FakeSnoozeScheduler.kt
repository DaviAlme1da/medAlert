package com.davialmeida.medalert.reminder.domain.usecase

import com.davialmeida.medalert.reminder.domain.scheduler.SnoozeScheduler
import java.time.Instant
import java.time.LocalDateTime

internal data class ScheduledSnooze(
    val medicationId: Long,
    val scheduledDateTime: LocalDateTime,
    val triggerAt: Instant
)

internal class FakeSnoozeScheduler : SnoozeScheduler {

    val scheduled = mutableListOf<ScheduledSnooze>()
    val cancelled = mutableListOf<Pair<Long, LocalDateTime>>()
    var failOnSchedule = false

    override fun schedule(
        medicationId: Long,
        scheduledDateTime: LocalDateTime,
        triggerAt: Instant
    ) {
        if (failOnSchedule) throw SecurityException("Simulated scheduling failure")
        scheduled.add(ScheduledSnooze(medicationId, scheduledDateTime, triggerAt))
    }

    override fun cancel(medicationId: Long, scheduledDateTime: LocalDateTime) {
        cancelled.add(medicationId to scheduledDateTime)
    }
}