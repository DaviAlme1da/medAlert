package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.MedAlertTheme

@Composable
fun DoseCard(
    name: String,
    dosage: String,
    time: String,
    status: DoseStatusUi,
    dotColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onTakeClick: (() -> Unit)? = null,
    showTime: Boolean = true,
) {
    MedCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MedicationDot(color = dotColor)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (showTime) "$time · $dosage" else dosage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (status == DoseStatusUi.Pending && onTakeClick != null) {
                MedButton(
                    text = stringResource(R.string.action_taken),
                    onClick = onTakeClick,
                    style = MedButtonStyle.Positive,
                    compact = true,
                    icon = painterResource(R.drawable.ic_med_check)
                )
            } else {
                DoseStatusChip(status = status)
            }
        }
    }
}

@Composable
private fun DoseStatusChip(status: DoseStatusUi) {
    val (textRes, kind) = when (status) {
        DoseStatusUi.Taken -> R.string.status_taken to MedStatusKind.Success
        DoseStatusUi.Skipped -> R.string.status_skipped to MedStatusKind.Warning
        DoseStatusUi.Late -> R.string.status_late to MedStatusKind.Danger
        else -> R.string.status_pending to MedStatusKind.Neutral
    }

    MedStatusChip(text = stringResource(textRes), kind = kind)
}

@Preview(showBackground = true)
@Composable
private fun DoseCardPreview() {
    MedAlertTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DoseCard(
                name = "Dipirona",
                dosage = "1 comprimido · 500 mg",
                status = DoseStatusUi.Pending,
                dotColor = MaterialTheme.colorScheme.primary,
                onTakeClick = {},
                time = "14:00",
                showTime = false
            )

            DoseCard(
                name = "Losartana",
                dosage = "50 mg",
                status = DoseStatusUi.Taken,
                dotColor = MaterialTheme.colorScheme.secondary,
                time = "08:00"
            )

            DoseCard(
                name = "Omeprazol",
                dosage = "20 mg",
                status = DoseStatusUi.Skipped,
                dotColor = MaterialTheme.colorScheme.tertiary,
                time = "08:00"
            )

            DoseCard(
                name = "Amoxicilina",
                dosage = "500 mg",
                status = DoseStatusUi.Late,
                dotColor = MaterialTheme.colorScheme.error,
                time = "08:00"
            )
        }
    }
}