package com.davialmeida.medalert.reminder.data.scheduler

import android.util.Log
import com.davialmeida.medalert.core.di.ApplicationScope
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.reminder.domain.usecase.RescheduleRemindersUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderSynchronizer @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val rescheduleReminders: RescheduleRemindersUseCase,
    @ApplicationScope private val scope: CoroutineScope,
) {

    private val started = AtomicBoolean(false)

    fun start() {
        if (!started.compareAndSet(false, true)) return

        medicationRepository.observeAllWithReminders()
            .distinctUntilChanged()
            .onEach {
                val result = rescheduleReminders()
                if (result is AppResult.Error) Log.e(TAG, "Falha ao reagendar: ${result.error}")
            }
            .catch { Log.e(TAG, "Observação de lembretes interrompida", it) }
            .launchIn(scope)
    }

    private companion object {
        const val TAG = "ReminderSynchronizer"
    }
}