package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.history.domain.model.HistoryDay
import com.davialmeida.medalert.medication.presentation.extension.toComposeColor
import com.davialmeida.medalert.medication.presentation.extension.toDisplayText
import com.davialmeida.medalert.today.presentation.mapper.toUi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class HistoryDoseSelection(
    val medicationId: Long,
    val scheduledDateTime: LocalDateTime,
    val medicationName: String,
    val canAct: Boolean,
    val hasLog: Boolean
)

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm")

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)

@Composable
fun HistoryDaySection(
    day: HistoryDay,
    today: LocalDate,
    onDoseClick: (HistoryDoseSelection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = day.date.format(dateFormatter),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        day.items.forEach { item ->
            val ui = item.toUi(today)

            DoseCard(
                name = ui.name,
                dosage = ui.dosage.toDisplayText(),
                time = ui.time.format(timeFormatter),
                status = ui.status,
                dotColor = ui.color.toComposeColor(),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onDoseClick(
                        HistoryDoseSelection(
                            medicationId = ui.medicationId,
                            scheduledDateTime = ui.scheduledDateTime,
                            medicationName = ui.name,
                            canAct = ui.canAct,
                            hasLog = ui.hasLog
                        )
                    )
                }
            )
        }
    }
}