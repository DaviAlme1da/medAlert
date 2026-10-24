package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.MedAlertTheme

@Composable
fun AdherenceSummaryCard(
    percentage: Int?,
    taken: Int,
    skipped: Int,
    late: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.history_adherence_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = percentage?.let {
                    stringResource(R.string.history_adherence_percentage, it)
                } ?: stringResource(R.string.history_adherence_empty),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = stringResource(R.string.status_taken),
                    value = taken
                )

                SummaryItem(
                    label = stringResource(R.string.status_skipped),
                    value = skipped
                )

                SummaryItem(
                    label = stringResource(R.string.status_late),
                    value = late
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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