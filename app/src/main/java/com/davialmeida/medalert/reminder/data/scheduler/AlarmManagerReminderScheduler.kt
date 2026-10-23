package com.davialmeida.medalert.reminder.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.davialmeida.medalert.reminder.data.receiver.ReminderAlarmReceiver
import com.davialmeida.medalert.reminder.domain.permission.ReminderPermissionChecker
import com.davialmeida.medalert.reminder.domain.scheduler.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject

class AlarmManagerReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager,
    private val permissionChecker: ReminderPermissionChecker
) : ReminderScheduler {

    override fun scheduleAt(trigger: Instant) {
        val triggerAtMillis = trigger.toEpochMilli()

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmContract.EXTRA_TRIGGER_AT_MILLIS, triggerAtMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ReminderAlarmContract.REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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

    override fun cancel() {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ReminderAlarmContract.REQUEST_CODE,
            Intent(context, ReminderAlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}