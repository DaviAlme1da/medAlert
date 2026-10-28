package com.davialmeida.medalert.reminder.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.davialmeida.medalert.core.di.ApplicationScope
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.reminder.data.notification.ReminderNotifier
import com.davialmeida.medalert.reminder.domain.usecase.GetSnoozedReminderUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class SnoozeAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var getSnoozedReminder: GetSnoozedReminderUseCase

    @Inject
    lateinit var notifier: ReminderNotifier

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(SnoozeContract.EXTRA_MEDICATION_ID, -1L)
        val scheduled = intent.getStringExtra(SnoozeContract.EXTRA_SCHEDULED_DATE_TIME)
            ?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }

        if (medicationId <= 0L || scheduled == null) {
            Log.w(TAG, "Dados inválidos na soneca")
            return
        }

        val pending = goAsync()

        applicationScope.launch {
            try {
                when (val result = getSnoozedReminder(medicationId, scheduled)) {
                    is AppResult.Success -> result.data?.let(notifier::show)
                    is AppResult.Error -> Log.e(TAG, "Falha na soneca: ${result.error}")
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e(TAG, "Erro ao exibir a soneca", exception)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "SnoozeAlarmReceiver"
    }
}