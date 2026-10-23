package com.davialmeida.medalert.reminder.di

import android.app.AlarmManager
import android.content.Context
import com.davialmeida.medalert.reminder.data.permission.AndroidReminderPermissionChecker
import com.davialmeida.medalert.reminder.data.scheduler.AlarmManagerReminderScheduler
import com.davialmeida.medalert.reminder.domain.permission.ReminderPermissionChecker
import com.davialmeida.medalert.reminder.domain.scheduler.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

    companion object {

        @Provides
        @Singleton
        fun provideAlarmManager(
            @ApplicationContext context: Context
        ): AlarmManager {
            return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }

        @Provides
        @Singleton
        @ApplicationScope
        fun provideApplicationScope(): CoroutineScope {
            return CoroutineScope(
                SupervisorJob() + Dispatchers.Default
            )
        }
    }
}