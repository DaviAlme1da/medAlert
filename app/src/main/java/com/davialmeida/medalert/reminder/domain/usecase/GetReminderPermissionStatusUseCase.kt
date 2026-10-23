package com.davialmeida.medalert.reminder.domain.usecase

import com.davialmeida.medalert.reminder.domain.model.ReminderPermissionStatus
import com.davialmeida.medalert.reminder.domain.permission.ReminderPermissionChecker
import javax.inject.Inject

class GetReminderPermissionStatusUseCase @Inject constructor(
    private val permissionChecker: ReminderPermissionChecker
) {
    operator fun invoke(): ReminderPermissionStatus {
        return permissionChecker.status()
    }
}