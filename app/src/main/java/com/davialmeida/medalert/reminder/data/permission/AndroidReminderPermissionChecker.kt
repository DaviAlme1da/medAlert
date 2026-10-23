package com.davialmeida.medalert.reminder.data.permission

import android.app.AlarmManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.davialmeida.medalert.reminder.domain.model.ReminderPermissionStatus
import com.davialmeida.medalert.reminder.domain.permission.ReminderPermissionChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import javax.inject.Inject

class AndroidReminderPermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) : ReminderPermissionChecker {

    override fun status(): ReminderPermissionStatus {
        val notificationsAllowed =
            NotificationManagerCompat.from(context).areNotificationsEnabled()

        val exactAlarmsAllowed =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

        return ReminderPermissionStatus(
            notificationsAllowed = notificationsAllowed,
            exactAlarmsAllowed = exactAlarmsAllowed
        )
    }
}