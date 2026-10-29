package com.davialmeida.medalert.history.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.AdherenceSummaryCard
import com.davialmeida.medalert.compose.component.DoseActionSheet
import com.davialmeida.medalert.compose.component.HistoryDaySection
import com.davialmeida.medalert.compose.component.HistoryDoseSelection
import com.davialmeida.medalert.compose.component.HistoryPeriodSelector
import com.davialmeida.medalert.compose.component.MedFilterChip
import com.davialmeida.medalert.compose.component.MedTabHeader
import com.davialmeida.medalert.history.presentation.viewmodel.HistoryViewModel
import com.davialmeida.medalert.today.presentation.extension.toTodayMessageRes
import java.time.format.DateTimeFormatter

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedDose by remember {
        mutableStateOf<HistoryDoseSelection?>(null)
    }

    Column(modifier = modifier.fillMaxSize()) {
        MedTabHeader(
            title = stringResource(R.string.nav_history),
            subtitle = stringResource(R.string.history_subtitle)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HistoryPeriodSelector(
                    selectedPeriod = state.period,
                    onPeriodSelected = viewModel::selectPeriod,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.medications.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = stringResource(R.string.history_filter_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                MedFilterChip(
                                    label = stringResource(R.string.history_filter_all),
                                    selected = state.selectedSeriesId == null,
                                    onClick = { viewModel.selectSeries(null) }
                                )
                            }

                            items(
                                items = state.medications,
                                key = { it.seriesId }
                            ) { medication ->
                                MedFilterChip(
                                    label = medication.name,
                                    selected = state.selectedSeriesId == medication.seriesId,
                                    onClick = {
                                        viewModel.selectSeries(medication.seriesId)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            state.actionError?.let { error ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(error.toTodayMessageRes()),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        TextButton(onClick = viewModel::clearActionError) {
                            Text(stringResource(R.string.today_error_dismiss))
                        }
                    }
                }
            }

            when {
                state.isLoading -> item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.hasLoadError -> item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.today_load_error),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.error
                        )

                        TextButton(onClick = viewModel::retry) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }

                state.activeProfile == null -> item {
                    Text(
                        text = stringResource(R.string.today_no_profile_message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                state.days.isEmpty() -> item {
                    Text(
                        text = stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    item {
                        AdherenceSummaryCard(
                            percentage = state.summary.percentage,
                            taken = state.summary.taken,
                            skipped = state.summary.skipped,
                            late = state.summary.late,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    items(
                        items = state.days,
                        key = { it.date.toString() }
                    ) { day ->
                        HistoryDaySection(
                            day = day,
                            today = state.today,
                            onDoseClick = { selectedDose = it }
                        )
                    }
                }
            }
        }
    }

    selectedDose?.let { dose ->
        DoseActionSheet(
            medicationName = dose.medicationName,
            time = dose.scheduledDateTime.toLocalTime().format(timeFormatter),
            canAct = dose.canAct,
            hasLog = dose.hasLog,
            onMarkTaken = {
                viewModel.markTaken(dose.medicationId, dose.scheduledDateTime)
                selectedDose = null
            },
            onSkip = {
                viewModel.markSkipped(dose.medicationId, dose.scheduledDateTime)
                selectedDose = null
            },
            onUndo = {
                viewModel.undo(dose.medicationId, dose.scheduledDateTime)
                selectedDose = null
            },
            onDismiss = { selectedDose = null }
        )
    }
}