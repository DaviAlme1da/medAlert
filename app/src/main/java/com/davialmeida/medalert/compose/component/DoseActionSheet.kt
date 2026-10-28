package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.statusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoseActionSheet(
    medicationName: String,
    time: String,
    canAct: Boolean,
    hasLog: Boolean,
    onMarkTaken: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "$medicationName · $time",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            when {
                !canAct -> {
                    Text(
                        text = stringResource(R.string.dose_action_future_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                hasLog -> {
                    MedButton(
                        text = stringResource(R.string.action_undo),
                        onClick = onUndo,
                        modifier = Modifier.fillMaxWidth(),
                        style = MedButtonStyle.Outlined,
                    )
                }

                else -> {
                    MedButton(
                        text = stringResource(R.string.action_taken_now),
                        onClick = onMarkTaken,
                        modifier = Modifier.fillMaxWidth(),
                        style = MedButtonStyle.Positive,
                        icon = painterResource(R.drawable.ic_med_check),
                    )

                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.statusColors.onWarningContainer
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.action_skip),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }

            MedButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                style = MedButtonStyle.Text,
            )
        }
    }
}