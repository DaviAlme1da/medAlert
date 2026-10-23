package com.davialmeida.medalert.reminder.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.davialmeida.medalert.R

object ReminderNotificationChannel {

    const val CHANNEL_ID = "dose_reminders"

    fun create(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
        }
        manager.createNotificationChannel(channel)
    }
}