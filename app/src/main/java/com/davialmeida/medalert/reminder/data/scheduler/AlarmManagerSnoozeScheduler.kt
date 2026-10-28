package com.davialmeida.medalert.reminder.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.davialmeida.medalert.reminder.data.receiver.SnoozeAlarmReceiver
import com.davialmeida.medalert.reminder.data.receiver.SnoozeContract
import com.davialmeida.medalert.reminder.domain.permission.ReminderPermissionChecker
import com.davialmeida.medalert.reminder.domain.scheduler.SnoozeScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject

class AlarmManagerSnoozeScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager,
    private val permissionChecker: ReminderPermissionChecker
) : SnoozeScheduler {

    override fun schedule(
        medicationId: Long,
        scheduledDateTime: LocalDateTime,
        triggerAt: Instant
    ) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(medicationId, scheduledDateTime),
            createIntent(medicationId, scheduledDateTime),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = triggerAt.toEpochMilli()

        if (permissionChecker.status().exactAlarmsAllowed) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                return
            } catch (exception: SecurityException) {
                // Permissão revogada entre a checagem e a chamada: usa o inexato.
            }
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    override fun cancel(
        medicationId: Long,
        scheduledDateTime: LocalDateTime
    ) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(medicationId, scheduledDateTime),
            createIntent(medicationId, scheduledDateTime),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun createIntent(
        medicationId: Long,
        scheduledDateTime: LocalDateTime
    ): Intent {
        return Intent(context, SnoozeAlarmReceiver::class.java).apply {
            action = SnoozeContract.ACTION_SNOOZE
            // Uri única por dose: sem ela, uma soneca sobrescreveria a outra.
            data = Uri.parse(
                "medalert://snooze/$medicationId/${Uri.encode(scheduledDateTime.toString())}"
            )
            putExtra(SnoozeContract.EXTRA_MEDICATION_ID, medicationId)
            putExtra(
                SnoozeContract.EXTRA_SCHEDULED_DATE_TIME,
                scheduledDateTime.toString()
            )
        }
    }

    private fun requestCode(
        medicationId: Long,
        scheduledDateTime: LocalDateTime
    ): Int = "$medicationId|$scheduledDateTime".hashCode()
}