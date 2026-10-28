package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onPreviousWeek,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_med_chevron_left),
                    contentDescription = previousDescription,
                    modifier = Modifier.size(20.dp),
                )
            }

            Text(
                text = selected.month
                    .getDisplayName(TextStyle.FULL, locale)
                    .replaceFirstChar { it.uppercase(locale) } +
                        " ${selected.year}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            IconButton(
                onClick = onNextWeek,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_med_chevron_right),
                    contentDescription = nextDescription,
                    modifier = Modifier.size(20.dp),
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
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
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
                            vertical = 10.dp,
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = date.dayOfWeek
                                .getDisplayName(TextStyle.SHORT, locale)
                                .replace(".", "")
                                .uppercase(locale),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )

                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
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