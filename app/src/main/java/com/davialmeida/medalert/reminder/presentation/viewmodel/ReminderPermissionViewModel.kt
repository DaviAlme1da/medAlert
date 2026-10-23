package com.davialmeida.medalert.reminder.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davialmeida.medalert.reminder.domain.model.ReminderPermissionStatus
import com.davialmeida.medalert.reminder.domain.permission.ReminderPermissionChecker
import com.davialmeida.medalert.reminder.domain.usecase.RescheduleRemindersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderPermissionViewModel @Inject constructor(
    private val permissionChecker: ReminderPermissionChecker,
    private val rescheduleReminders: RescheduleRemindersUseCase,
) : ViewModel() {

    private val _status = MutableStateFlow(permissionChecker.status())
    val status: StateFlow<ReminderPermissionStatus> = _status.asStateFlow()

    fun refresh() {
        val previous = _status.value
        val current = permissionChecker.status()
        _status.value = current

        if (current.exactAlarmsAllowed && !previous.exactAlarmsAllowed) {
            viewModelScope.launch {
                try {
                    rescheduleReminders()
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Falha ao reagendar após permissão", e)
                }
            }
        }
    }

    fun onNotificationsPermissionGranted() = refresh()

    private companion object {
        const val TAG = "ReminderPermissionVM"
    }
}