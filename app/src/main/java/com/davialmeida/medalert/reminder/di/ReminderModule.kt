package com.davialmeida.medalert.reminder.di

import android.app.AlarmManager
import android.content.Context
import com.davialmeida.medalert.reminder.data.notification.ReminderDoseRegistrationListener
import com.davialmeida.medalert.reminder.data.permission.AndroidReminderPermissionChecker
import com.davialmeida.medalert.reminder.data.scheduler.AlarmManagerReminderScheduler
import com.davialmeida.medalert.reminder.data.scheduler.AlarmManagerSnoozeScheduler
import com.davialmeida.medalert.reminder.domain.permission.ReminderPermissionChecker
import com.davialmeida.medalert.reminder.domain.scheduler.ReminderScheduler
import com.davialmeida.medalert.reminder.domain.scheduler.SnoozeScheduler
import com.davialmeida.medalert.today.domain.listener.DoseRegistrationListener
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(
        implementation: AlarmManagerReminderScheduler
    ): ReminderScheduler

    @Binds
    @Singleton
    abstract fun bindReminderPermissionChecker(
        implementation: AndroidReminderPermissionChecker
    ): ReminderPermissionChecker

    @Binds
    @Singleton
    abstract fun bindSnoozeScheduler(
        implementation: AlarmManagerSnoozeScheduler
    ): SnoozeScheduler

    @Binds
    abstract fun bindDoseRegistrationListener(
        implementation: ReminderDoseRegistrationListener
    ): DoseRegistrationListener

    companion object {

        @Provides
        @Singleton
        fun provideAlarmManager(
            @ApplicationContext context: Context
        ): AlarmManager {
            return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
    }
}