package com.davialmeida.medalert.testutil

import com.davialmeida.medalert.reminder.domain.model.ReminderPermissionStatus
import com.davialmeida.medalert.reminder.domain.permission.ReminderPermissionChecker

internal class FakeReminderPermissionChecker(
    var current: ReminderPermissionStatus = ReminderPermissionStatus(
        notificationsAllowed = false,
        exactAlarmsAllowed = false
    )
) : ReminderPermissionChecker {

    override fun status(): ReminderPermissionStatus = current
}