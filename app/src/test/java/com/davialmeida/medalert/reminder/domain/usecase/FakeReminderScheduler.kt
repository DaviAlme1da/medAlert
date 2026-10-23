package com.davialmeida.medalert.reminder.domain.usecase

import com.davialmeida.medalert.reminder.domain.scheduler.ReminderScheduler
import java.time.Instant

internal class FakeReminderScheduler : ReminderScheduler {

    val scheduled = mutableListOf<Instant>()
    var cancelCount = 0
    var failOnSchedule = false

    override fun scheduleAt(trigger: Instant) {
        if (failOnSchedule) {
            throw SecurityException("Simulated scheduling failure")
        }
        scheduled.add(trigger)
    }

    override fun cancel() {
        cancelCount++
    }
}