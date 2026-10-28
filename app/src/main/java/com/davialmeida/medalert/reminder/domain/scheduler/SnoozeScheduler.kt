package com.davialmeida.medalert.reminder.domain.scheduler

import java.time.Instant
import java.time.LocalDateTime

interface SnoozeScheduler {

    fun schedule(
        medicationId: Long,
        scheduledDateTime: LocalDateTime,
        triggerAt: Instant
    )

    fun cancel(
        medicationId: Long,
        scheduledDateTime: LocalDateTime
    )
}