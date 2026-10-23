package com.davialmeida.medalert.reminder.domain.model

data class ReminderPermissionStatus(
    val notificationsAllowed: Boolean,
    val exactAlarmsAllowed: Boolean
) {
    val isComplete: Boolean
        get() = notificationsAllowed && exactAlarmsAllowed
}