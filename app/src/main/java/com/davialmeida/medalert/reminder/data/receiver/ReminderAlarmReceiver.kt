package com.davialmeida.medalert.reminder.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.davialmeida.medalert.core.di.ApplicationScope
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.reminder.data.notification.ReminderNotifier
import com.davialmeida.medalert.reminder.data.scheduler.ReminderAlarmContract
import com.davialmeida.medalert.reminder.domain.usecase.GetDueRemindersUseCase
import com.davialmeida.medalert.reminder.domain.usecase.RescheduleRemindersUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var getDueReminders: GetDueRemindersUseCase
    @Inject lateinit var rescheduleReminders: RescheduleRemindersUseCase
    @Inject lateinit var notifier: ReminderNotifier
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val triggerMillis = intent.getLongExtra(ReminderAlarmContract.EXTRA_TRIGGER_AT_MILLIS, -1L)
        val pending = goAsync()

        applicationScope.launch {
            try {
                if (triggerMillis > 0) {
                    when (val result = getDueReminders(Instant.ofEpochMilli(triggerMillis))) {
                        is AppResult.Success -> result.data.forEach(notifier::show)
                        is AppResult.Error -> Log.e(TAG, "Falha ao buscar lembretes: ${result.error}")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao exibir lembretes", e)
            } finally {
                try {
                    val r = rescheduleReminders()
                    if (r is AppResult.Error) Log.e(TAG, "Falha ao reagendar: ${r.error}")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao reagendar", e)
                } finally {
                    pending.finish()
                }
            }
        }
    }

    private companion object {
        const val TAG = "ReminderAlarmReceiver"
    }
}