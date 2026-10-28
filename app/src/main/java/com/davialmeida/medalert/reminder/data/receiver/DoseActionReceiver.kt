package com.davialmeida.medalert.reminder.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.davialmeida.medalert.core.di.ApplicationScope
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.reminder.data.notification.ReminderNotifier
import com.davialmeida.medalert.reminder.domain.usecase.SnoozeReminderUseCase
import com.davialmeida.medalert.today.domain.model.DoseLogStatus
import com.davialmeida.medalert.today.domain.usecase.MarkDoseUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class DoseActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var markDose: MarkDoseUseCase

    @Inject
    lateinit var snoozeReminder: SnoozeReminderUseCase

    @Inject
    lateinit var notifier: ReminderNotifier

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(DoseActionContract.EXTRA_MEDICATION_ID, -1L)
        val scheduled = intent.getStringExtra(DoseActionContract.EXTRA_SCHEDULED_DATE_TIME)
            ?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
        val action = intent.getStringExtra(DoseActionContract.EXTRA_ACTION)
        val notificationId = intent.getIntExtra(DoseActionContract.EXTRA_NOTIFICATION_ID, -1)

        val pending = goAsync()

        applicationScope.launch {
            try {
                if (medicationId <= 0L || scheduled == null) {
                    Log.w(TAG, "Dados inválidos para a ação do lembrete")
                    return@launch
                }

                val result: AppResult<*>? = when (action) {
                    DoseActionContract.ACTION_TAKEN ->
                        markDose(medicationId, scheduled, DoseLogStatus.TAKEN)

                    DoseActionContract.ACTION_SKIPPED ->
                        markDose(medicationId, scheduled, DoseLogStatus.SKIPPED)

                    DoseActionContract.ACTION_SNOOZE ->
                        snoozeReminder(medicationId, scheduled)

                    else -> {
                        Log.w(TAG, "Ação desconhecida: $action")
                        null
                    }
                }

                if (result is AppResult.Error) {
                    Log.e(TAG, "Falha na ação $action: ${result.error}")
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e(TAG, "Erro ao processar ação do lembrete", exception)
            } finally {
                if (notificationId != -1) notifier.cancel(notificationId)
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "DoseActionReceiver"
    }
}