package com.davialmeida.medalert

import android.app.Application
import com.davialmeida.medalert.reminder.data.notification.ReminderNotificationChannel
import com.davialmeida.medalert.reminder.data.scheduler.ReminderSynchronizer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MedApplication : Application() {

    @Inject lateinit var reminderSynchronizer: ReminderSynchronizer

    override fun onCreate() {
        super.onCreate()
        ReminderNotificationChannel.create(this)
        reminderSynchronizer.start()
    }
}