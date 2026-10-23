package com.davialmeida.medalert.reminder.domain.scheduler

import java.time.Instant

interface ReminderScheduler {
    fun scheduleAt(trigger: Instant)

    fun cancel()
}