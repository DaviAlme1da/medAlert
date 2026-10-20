package com.davialmeida.medalert.medication.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.MedAlertDialog
import com.davialmeida.medalert.compose.component.MedButton
import com.davialmeida.medalert.compose.component.MedScreenScaffold
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.presentation.extension.toComposeColor
import com.davialmeida.medalert.medication.presentation.extension.toDisplayText
import com.davialmeida.medalert.medication.presentation.extension.toLabelRes
import com.davialmeida.medalert.medication.presentation.extension.toMessageRes
import com.davialmeida.medalert.medication.presentation.extension.toSummary
import com.davialmeida.medalert.medication.presentation.state.MedicationDetailUiState
import com.davialmeida.medalert.medication.presentation.viewmodel.MedicationDetailViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

@Composable
fun MedicationDetailScreen(
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MedicationDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPauseDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // Ao voltar da edição, recarrega os dados.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onDeleted()
        }
    }

    MedScreenScaffold(
        title = stringResource(R.string.medication_detail_title),
        onBackClick = onBackClick,
        modifier = modifier
    ) { innerPadding ->
        val medication = state.medication

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            medication == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(
                            state.loadError?.toMessageRes()
                                ?: R.string.medication_detail_load_error
                        ),
                        color = MaterialTheme.colorScheme.error
                    )

                    TextButton(onClick = viewModel::retry) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }

            else -> {
                MedicationDetailContent(
                    medication = medication,
                    state = state,
                    onEditClick = { onEditClick(medication.seriesId) },
                    onPauseClick = { showPauseDialog = true },
                    onResumeClick = viewModel::resume,
                    onDeleteClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                )
            }
        }
    }

    if (showPauseDialog) {
        MedAlertDialog(
            title = stringResource(R.string.medication_pause_dialog_title),
            message = stringResource(R.string.medication_pause_dialog_message),
            confirmText = stringResource(R.string.medication_pause_dialog_confirm),
            dismissText = stringResource(R.string.medication_dialog_cancel),
            onConfirm = {
                showPauseDialog = false
                viewModel.pause()
            },
            onDismiss = { showPauseDialog = false }
        )
    }

    if (showDeleteDialog) {
        MedAlertDialog(
            title = stringResource(R.string.medication_delete_dialog_title),
            message = stringResource(R.string.medication_delete_dialog_message),
            confirmText = stringResource(R.string.medication_delete_dialog_confirm),
            dismissText = stringResource(R.string.medication_dialog_cancel),
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete()
            },
            onDismiss = { showDeleteDialog = false },
            destructive = true
        )
    }
}

@Composable
private fun MedicationDetailContent(
    medication: Medication,
    state: MedicationDetailUiState,
    onEditClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = medication.name,
            style = MaterialTheme.typography.headlineSmall,
            color = medication.color.toComposeColor()
        )

        HorizontalDivider()

        DetailRow(
            label = stringResource(R.string.medication_detail_status),
            value = stringResource(state.status.toLabelRes())
        )

        DetailRow(
            label = stringResource(R.string.medication_detail_dosage),
            value = medication.dosage.toDisplayText()
        )

        DetailRow(
            label = stringResource(R.string.medication_detail_form),
            value = stringResource(medication.form.toLabelRes())
        )

        DetailRow(
            label = stringResource(R.string.medication_detail_frequency),
            value = medication.frequency.toSummary()
        )

        DetailRow(
            label = stringResource(R.string.medication_detail_start_date),
            value = medication.startDate.format(dateFormatter)
        )

        DetailRow(
            label = stringResource(R.string.medication_detail_end_date),
            value = medication.endDate?.format(dateFormatter)
                ?: stringResource(R.string.medication_detail_no_end_date)
        )

        DetailRow(
            label = stringResource(R.string.medication_detail_reminders),
            value = stringResource(
                if (medication.remindersEnabled) {
                    R.string.common_enabled
                } else {
                    R.string.common_disabled
                }
            )
        )

        if (medication.notes.isNotBlank()) {
            DetailRow(
                label = stringResource(R.string.medication_detail_notes),
                value = medication.notes
            )
        }

        state.actionError?.let { error ->
            Text(
                text = stringResource(error.toMessageRes()),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (state.canEdit) {
            MedButton(
                text = stringResource(R.string.medication_detail_edit),
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isBusy
            )
        }

        if (state.canPause) {
            OutlinedButton(
                onClick = onPauseClick,
                enabled = !state.isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.medication_detail_pause))
            }
        }

        if (state.canResume) {
            MedButton(
                text = stringResource(R.string.medication_detail_resume),
                onClick = onResumeClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isBusy,
                loading = state.isBusy
            )
        }

        TextButton(
            onClick = onDeleteClick,
            enabled = !state.isBusy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.medication_detail_delete),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
