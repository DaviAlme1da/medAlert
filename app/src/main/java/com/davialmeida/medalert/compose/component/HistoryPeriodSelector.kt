package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import com.davialmeida.medalert.history.domain.model.HistoryPeriod

@Composable
fun HistoryPeriodSelector(
    selectedPeriod: HistoryPeriod,
    onPeriodSelected: (HistoryPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedPeriod == HistoryPeriod.LAST_7_DAYS,
            onClick = {
                onPeriodSelected(HistoryPeriod.LAST_7_DAYS)
            },
            label = {
                Text(
                    text = stringResource(R.string.history_period_7_days)
                )
            }
        )

        FilterChip(
            selected = selectedPeriod == HistoryPeriod.LAST_30_DAYS,
            onClick = {
                onPeriodSelected(HistoryPeriod.LAST_30_DAYS)
            },
            label = {
                Text(
                    text = stringResource(R.string.history_period_30_days)
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryPeriodSelectorPreview() {
    MedAlertTheme {
        HistoryPeriodSelector(
            selectedPeriod = HistoryPeriod.LAST_7_DAYS,
            onPeriodSelected = {}
        )
    }
}
