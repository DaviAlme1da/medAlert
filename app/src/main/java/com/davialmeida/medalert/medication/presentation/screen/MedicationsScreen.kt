package com.davialmeida.medalert.medication.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.EmptyState
import com.davialmeida.medalert.compose.component.EmptyStateIllustration
import com.davialmeida.medalert.compose.component.MedSegmentedControl
import com.davialmeida.medalert.compose.component.MedTabHeader
import com.davialmeida.medalert.compose.component.MedicationCard
import com.davialmeida.medalert.medication.presentation.extension.toComposeColor
import com.davialmeida.medalert.medication.presentation.extension.toDisplayText
import com.davialmeida.medalert.medication.presentation.extension.toSummary
import com.davialmeida.medalert.medication.presentation.state.MedicationListItem
import com.davialmeida.medalert.medication.presentation.viewmodel.MedicationsViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private enum class MedicationTab {
    ACTIVE,
    PAUSED
}

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm")

private val dayTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)

@Composable
fun MedicationsScreen(
    onAddMedication: () -> Unit,
    onOpenMedication: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MedicationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(MedicationTab.ACTIVE) }

    val activeCount = uiState.activeMedications.size
    val showsList = !uiState.isLoading && !uiState.hasError

    val subtitle = if (showsList && activeCount > 0) {
        pluralStringResource(R.plurals.medications_active_count, activeCount, activeCount)
    } else {
        stringResource(R.string.medications_subtitle_empty)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            MedTabHeader(
                title = stringResource(R.string.nav_medications),
                subtitle = subtitle
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.hasError -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.medication_list_load_error),
                                color = MaterialTheme.colorScheme.error
                            )

                            TextButton(onClick = viewModel::retry) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }

                else -> {
                    val items = when (selectedTab) {
                        MedicationTab.ACTIVE -> uiState.activeMedications
                        MedicationTab.PAUSED -> uiState.pausedMedications
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MedSegmentedControl(
                            options = listOf(
                                stringResource(R.string.medications_tab_active),
                                stringResource(R.string.medications_tab_paused)
                            ),
                            selectedIndex = selectedTab.ordinal,
                            onSelect = { selectedTab = MedicationTab.entries[it] }
                        )

                        if (items.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedTab == MedicationTab.ACTIVE) {
                                    EmptyState(
                                        title = stringResource(
                                            R.string.medications_empty_active_title
                                        ),
                                        message = stringResource(
                                            R.string.medications_empty_active_message
                                        ),
                                        illustration = {
                                            EmptyStateIllustration(
                                                iconRes = R.drawable.ic_nav_medications
                                            )
                                        },
                                        actionText = stringResource(R.string.add_medication),
                                        onActionClick = onAddMedication
                                    )
                                } else {
                                    EmptyState(
                                        title = stringResource(R.string.medications_empty_paused),
                                        message = ""
                                    )
                                }
                            }
                        } else {
                            MedicationList(
                                items = items,
                                onOpenMedication = onOpenMedication
                            )
                        }
                    }
                }
            }
        }

        if (showsList) {
            val addDescription =
                stringResource(R.string.medication_add_content_description)

            FloatingActionButton(
                onClick = onAddMedication,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .semantics {
                        contentDescription = addDescription
                    },
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_med_add),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun MedicationList(
    items: List<MedicationListItem>,
    onOpenMedication: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        items(
            items = items,
            key = { item -> item.medication.seriesId }
        ) { item ->
            val medication = item.medication

            MedicationCard(
                name = medication.name,
                dosage = medication.dosage.toDisplayText(),
                frequencySummary = medication.frequency.toSummary(),
                nextDose = item.nextDose?.let { next ->
                    if (item.nextDoseIsToday) {
                        stringResource(
                            R.string.medication_next_today,
                            next.format(timeFormatter)
                        )
                    } else {
                        next.format(dayTimeFormatter)
                    }
                },
                dotColor = medication.color.toComposeColor(),
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpenMedication(medication.seriesId) }
            )
        }
    }
}