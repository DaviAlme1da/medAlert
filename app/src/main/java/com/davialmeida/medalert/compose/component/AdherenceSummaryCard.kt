package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import com.davialmeida.medalert.compose.theme.statusColors

@Composable
fun AdherenceSummaryCard(
    percentage: Int?,
    taken: Int,
    skipped: Int,
    late: Int,
    modifier: Modifier = Modifier
) {
    MedCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.history_adherence_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (percentage != null) {
                    MedStatusChip(
                        text = stringResource(
                            R.string.history_adherence_percentage,
                            percentage
                        ),
                        kind = MedStatusKind.Neutral
                    )
                }
            }

            if (percentage != null) {
                LinearProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
            } else {
                Text(
                    text = stringResource(R.string.history_adherence_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = stringResource(R.string.status_taken),
                    value = taken,
                    dotColor = MaterialTheme.statusColors.success
                )

                SummaryItem(
                    label = stringResource(R.string.status_skipped),
                    value = skipped,
                    dotColor = MaterialTheme.statusColors.warning
                )

                SummaryItem(
                    label = stringResource(R.string.status_late),
                    value = late,
                    dotColor = MaterialTheme.statusColors.danger
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: Int,
    dotColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AdherenceSummaryCardPreview() {
    MedAlertTheme {
        AdherenceSummaryCard(
            percentage = 85,
            taken = 17,
            skipped = 2,
            late = 1,
            modifier = Modifier.padding(16.dp)
        )
    }
}