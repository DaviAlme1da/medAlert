package com.davialmeida.medalert.reminder.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.reminder.domain.model.ReminderPermissionStatus

@Composable
fun ReminderPermissionBanner(
    status: ReminderPermissionStatus,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (status.isComplete) return

    val missingNotifications = !status.notificationsAllowed
    val missingExactAlarms = !status.exactAlarmsAllowed

    val message = when {
        missingNotifications && missingExactAlarms ->
            stringResource(R.string.reminder_permission_message_both)

        missingNotifications ->
            stringResource(R.string.reminder_permission_message_notifications)

        else ->
            stringResource(R.string.reminder_permission_message_exact)
    }

    val buttonText = if (missingNotifications) {
        stringResource(R.string.reminder_permission_action_notifications)
    } else {
        stringResource(R.string.reminder_permission_action_exact)
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.reminder_permission_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(buttonText)
            }
        }
    }
}