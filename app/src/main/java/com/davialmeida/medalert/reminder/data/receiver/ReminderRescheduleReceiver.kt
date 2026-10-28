package com.davialmeida.medalert.reminder.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.davialmeida.medalert.core.di.ApplicationScope
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.reminder.domain.usecase.RescheduleRemindersUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderRescheduleReceiver : BroadcastReceiver() {

    @Inject
    lateinit var rescheduleReminders: RescheduleRemindersUseCase
    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        applicationScope.launch {
            try {
                val result = rescheduleReminders()
                if (result is AppResult.Error) Log.e(
                    TAG,
                    "Falha ao reagendar (${intent.action}): ${result.error}"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao reagendar (${intent.action})", e)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "ReminderReschedule"
    }
}