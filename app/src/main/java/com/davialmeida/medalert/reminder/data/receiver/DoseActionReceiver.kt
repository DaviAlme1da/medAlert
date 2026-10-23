package com.davialmeida.medalert.reminder.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.reminder.data.notification.ReminderNotifier
import com.davialmeida.medalert.reminder.di.ApplicationScope
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
                if (medicationId > 0 && scheduled != null) {
                    val status = when (action) {
                        DoseActionContract.ACTION_TAKEN -> DoseLogStatus.TAKEN
                        DoseActionContract.ACTION_SKIPPED -> DoseLogStatus.SKIPPED
                        else -> null
                    }
                    if (status != null) {
                        val result = markDose(
                            medicationId,
                            scheduled,
                            status
                        )
                        if (result is AppResult.Error) Log.e(
                            TAG,
                            "Falha ao marcar dose: ${result.error}"
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao marcar dose", e)
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