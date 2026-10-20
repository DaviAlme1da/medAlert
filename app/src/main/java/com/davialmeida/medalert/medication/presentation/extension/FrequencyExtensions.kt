
package com.davialmeida.medalert.medication.presentation.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.davialmeida.medalert.R
import com.davialmeida.medalert.medication.domain.model.Frequency
import java.time.DayOfWeek

@Composable
fun Frequency.toSummary(): String {
    return when (this) {
        is Frequency.Daily -> {
            val count = times.size

            pluralStringResource(
                id = R.plurals.frequency_daily_summary,
                count = count,
                count
            )
        }

        is Frequency.SpecificDays -> {
            val dayLabels = daysOfWeek
                .sortedBy { it.value }
                .map { day ->
                    stringResource(day.toShortLabelRes())
                }

            val days = dayLabels.joinToString(", ")

            pluralStringResource(
                id = R.plurals.frequency_specific_days_summary,
                count = times.size,
                days,
                times.size
            )
        }

        is Frequency.EveryXHours -> {
            pluralStringResource(
                id = R.plurals.frequency_every_x_hours_summary,
                count = intervalHours,
                intervalHours
            )
        }
    }
}

private fun DayOfWeek.toShortLabelRes(): Int {
    return when (this) {
        DayOfWeek.MONDAY -> R.string.weekday_monday_short
        DayOfWeek.TUESDAY -> R.string.weekday_tuesday_short
        DayOfWeek.WEDNESDAY -> R.string.weekday_wednesday_short
        DayOfWeek.THURSDAY -> R.string.weekday_thursday_short
        DayOfWeek.FRIDAY -> R.string.weekday_friday_short
        DayOfWeek.SATURDAY -> R.string.weekday_saturday_short
        DayOfWeek.SUNDAY -> R.string.weekday_sunday_short
    }
}
