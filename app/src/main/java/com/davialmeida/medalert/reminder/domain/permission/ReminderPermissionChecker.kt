package com.davialmeida.medalert.reminder.domain.permission

import com.davialmeida.medalert.reminder.domain.model.ReminderPermissionStatus

interface ReminderPermissionChecker {
    fun status(): ReminderPermissionStatus
}