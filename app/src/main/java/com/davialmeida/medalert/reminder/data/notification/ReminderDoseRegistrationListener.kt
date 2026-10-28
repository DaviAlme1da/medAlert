package com.davialmeida.medalert.reminder.data.notification

import com.davialmeida.medalert.reminder.domain.scheduler.SnoozeScheduler
import com.davialmeida.medalert.today.domain.listener.DoseRegistrationListener
import java.time.LocalDateTime
import javax.inject.Inject

class ReminderDoseRegistrationListener @Inject constructor(
    private val notifier: ReminderNotifier,
    private val snoozeScheduler: SnoozeScheduler
) : DoseRegistrationListener {

    override fun onDoseRegistered(medicationId: Long, scheduledDateTime: LocalDateTime) {
        notifier.cancel(notificationId(medicationId, scheduledDateTime))
        snoozeScheduler.cancel(medicationId, scheduledDateTime)
    }
}