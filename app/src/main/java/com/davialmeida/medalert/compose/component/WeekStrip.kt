package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle

@Composable
fun WeekStrip(
    days: List<LocalDate>,
    selected: LocalDate,
    today: LocalDate,
    onSelect: (LocalDate) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val previousDescription = stringResource(R.string.week_strip_previous)
    val nextDescription = stringResource(R.string.week_strip_next)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = onPreviousWeek,
                modifier = Modifier.semantics {
                    contentDescription = previousDescription
                },
            ) {
                Text(
                    text = "‹",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            Text(
                text = selected.month
                    .getDisplayName(TextStyle.FULL, locale)
                    .replaceFirstChar { it.uppercase(locale) } +
                        " ${selected.year}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            TextButton(
                onClick = onNextWeek,
                modifier = Modifier.semantics {
                    contentDescription = nextDescription
                },
            ) {
                Text(
                    text = "›",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            days.forEach { date ->
                val isSelected = date == selected
                val isToday = date == today

                Surface(
                    onClick = { onSelect(date) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    border = if (isToday && !isSelected) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        null
                    },
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 2.dp,
                            vertical = 8.dp,
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = date.dayOfWeek
                                .getDisplayName(TextStyle.SHORT, locale)
                                .replace(".", ""),
                            style = MaterialTheme.typography.labelSmall,
                        )

                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeekStripPreview() {
    val today = LocalDate.of(2026, 10, 21)
    val days = (0..6).map { today.with(DayOfWeek.MONDAY).plusDays(it.toLong()) }

    MedAlertTheme {
        WeekStrip(
            days = days,
            selected = today,
            today = today,
            onSelect = {},
            onPreviousWeek = {},
            onNextWeek = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}