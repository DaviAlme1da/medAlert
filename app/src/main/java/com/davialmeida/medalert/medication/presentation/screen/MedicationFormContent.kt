package com.davialmeida.medalert.medication.presentation.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.MedButton
import com.davialmeida.medalert.compose.component.MedTextField
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.model.MedicationColor
import com.davialmeida.medalert.medication.domain.model.MedicationForm
import com.davialmeida.medalert.medication.presentation.extension.label
import com.davialmeida.medalert.medication.presentation.extension.toLabelRes
import com.davialmeida.medalert.medication.presentation.extension.toMessageRes
import com.davialmeida.medalert.medication.presentation.state.FrequencyType
import com.davialmeida.medalert.medication.presentation.state.MedicationField
import com.davialmeida.medalert.medication.presentation.state.MedicationFormUiState
import com.davialmeida.medalert.medication.presentation.viewmodel.MedicationFormViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm")

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

/** Agrupa as ações do formulário para não passar 18 lambdas soltas. */
class MedicationFormActions(
    val onNameChange: (String) -> Unit,
    val onDosageAmountChange: (String) -> Unit,
    val onDosageUnitChange: (DosageUnit) -> Unit,
    val onFormChange: (MedicationForm) -> Unit,
    val onFrequencyTypeChange: (FrequencyType) -> Unit,
    val onToggleDay: (DayOfWeek) -> Unit,
    val onAddTime: () -> Unit,
    val onRemoveTime: (LocalTime) -> Unit,
    val onUpdateTimes: (List<LocalTime>) -> Unit,
    val onIntervalHoursChange: (Int) -> Unit,
    val onFirstTimeChange: (LocalTime) -> Unit,
    val onStartDateChange: (LocalDate) -> Unit,
    val onEndDateChange: (LocalDate?) -> Unit,
    val onColorChange: (MedicationColor) -> Unit,
    val onNotesChange: (String) -> Unit,
    val onRemindersChange: (Boolean) -> Unit,
    val onSave: () -> Unit
)

@Composable
fun rememberMedicationFormActions(
    viewModel: MedicationFormViewModel
): MedicationFormActions {
    return remember(viewModel) {
        MedicationFormActions(
            onNameChange = viewModel::updateName,
            onDosageAmountChange = viewModel::updateDosageAmount,
            onDosageUnitChange = viewModel::updateDosageUnit,
            onFormChange = viewModel::updateForm,
            onFrequencyTypeChange = viewModel::updateFrequencyType,
            onToggleDay = viewModel::toggleDay,
            onAddTime = viewModel::addTime,
            onRemoveTime = viewModel::removeTime,
            onUpdateTimes = viewModel::updateTimes,
            onIntervalHoursChange = viewModel::updateIntervalHours,
            onFirstTimeChange = viewModel::updateFirstTime,
            onStartDateChange = viewModel::updateStartDate,
            onEndDateChange = viewModel::updateEndDate,
            onColorChange = viewModel::updateColor,
            onNotesChange = viewModel::updateNotes,
            onRemindersChange = viewModel::updateRemindersEnabled,
            onSave = viewModel::saveMedication
        )
    }
}

@Composable
fun MedicationFormContent(
    state: MedicationFormUiState,
    actions: MedicationFormActions,
    saveLabel: String,
    modifier: Modifier = Modifier,
    startDateEditable: Boolean = true
) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MedTextField(
            value = state.name,
            onValueChange = actions.onNameChange,
            label = stringResource(R.string.medication_field_name),
            modifier = Modifier.fillMaxWidth(),
            error = state.fieldErrors[MedicationField.NAME]
                ?.toMessageRes()?.let { stringResource(it) }
        )

        MedTextField(
            value = state.dosageAmount,
            onValueChange = actions.onDosageAmountChange,
            label = stringResource(R.string.medication_field_dosage_amount),
            modifier = Modifier.fillMaxWidth(),
            error = state.fieldErrors[MedicationField.DOSAGE]
                ?.toMessageRes()?.let { stringResource(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        SimpleDropdown(
            label = stringResource(R.string.medication_field_dosage_unit),
            selectedLabel = state.dosageUnit.label(),
            options = DosageUnit.entries.toList(),
            optionLabel = { it.label() },
            onSelected = actions.onDosageUnitChange
        )

        SimpleDropdown(
            label = stringResource(R.string.medication_field_form),
            selectedLabel = stringResource(state.form.toLabelRes()),
            options = MedicationForm.entries.toList(),
            optionLabel = { stringResource(it.toLabelRes()) },
            onSelected = actions.onFormChange
        )

        HorizontalDivider()

        Text(
            text = stringResource(R.string.medication_frequency_section),
            style = MaterialTheme.typography.titleMedium
        )

        SimpleDropdown(
            label = stringResource(R.string.medication_field_frequency_type),
            selectedLabel = stringResource(state.frequencyType.toLabelRes()),
            options = FrequencyType.entries.toList(),
            optionLabel = { stringResource(it.toLabelRes()) },
            onSelected = actions.onFrequencyTypeChange
        )

        when (state.frequencyType) {
            FrequencyType.DAILY,
            FrequencyType.SPECIFIC_DAYS -> {
                if (state.frequencyType == FrequencyType.SPECIFIC_DAYS) {
                    DaysSelector(
                        selectedDays = state.selectedDays,
                        onToggleDay = actions.onToggleDay
                    )
                    FieldErrorText(state.fieldErrors[MedicationField.DAYS]?.toMessageRes())
                }

                TimesEditor(
                    times = state.times,
                    onAddTime = actions.onAddTime,
                    onRemoveTime = actions.onRemoveTime,
                    onUpdateTimes = actions.onUpdateTimes
                )
                FieldErrorText(state.fieldErrors[MedicationField.TIMES]?.toMessageRes())
            }

            FrequencyType.EVERY_X_HOURS -> {
                val intervals = Frequency.VALID_EVERY_X_HOURS_INTERVALS.sorted()

                SimpleDropdown(
                    label = stringResource(R.string.medication_field_interval),
                    selectedLabel = pluralStringResource(
                        R.plurals.medication_interval_hours,
                        state.intervalHours,
                        state.intervalHours
                    ),
                    options = intervals,
                    optionLabel = {
                        pluralStringResource(
                            R.plurals.medication_interval_hours, it, it
                        )
                    },
                    onSelected = actions.onIntervalHoursChange
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(
                            R.string.medication_field_first_dose,
                            state.firstTime.format(timeFormatter)
                        )
                    )

                    OutlinedButton(
                        onClick = {
                            showTimePicker(context, state.firstTime) {
                                actions.onFirstTimeChange(it)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.medication_pick_time))
                    }
                }

                FieldErrorText(state.fieldErrors[MedicationField.FREQUENCY]?.toMessageRes())
            }
        }

        HorizontalDivider()

        Text(
            text = stringResource(R.string.medication_period_section),
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedButton(
            enabled = startDateEditable,
            onClick = {
                showDatePicker(context, state.startDate) {
                    actions.onStartDateChange(it)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    R.string.medication_start_date,
                    state.startDate.format(dateFormatter)
                )
            )
        }

        OutlinedButton(
            onClick = {
                showDatePicker(context, state.endDate ?: state.startDate) {
                    actions.onEndDateChange(it)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = state.endDate?.let {
                    stringResource(
                        R.string.medication_end_date,
                        it.format(dateFormatter)
                    )
                } ?: stringResource(R.string.medication_set_end_date)
            )
        }

        if (state.endDate != null) {
            OutlinedButton(
                onClick = { actions.onEndDateChange(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.medication_remove_end_date))
            }
        }

        FieldErrorText(state.fieldErrors[MedicationField.DATES]?.toMessageRes())

        HorizontalDivider()

        SimpleDropdown(
            label = stringResource(R.string.medication_field_color),
            selectedLabel = stringResource(state.color.toLabelRes()),
            options = MedicationColor.entries.toList(),
            optionLabel = { stringResource(it.toLabelRes()) },
            onSelected = actions.onColorChange
        )

        MedTextField(
            value = state.notes,
            onValueChange = actions.onNotesChange,
            label = stringResource(R.string.medication_field_notes),
            modifier = Modifier.fillMaxWidth(),
            singleLine = false
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.medication_field_reminders))

            Switch(
                checked = state.remindersEnabled,
                onCheckedChange = actions.onRemindersChange
            )
        }

        FieldErrorText(state.generalError?.toMessageRes())

        MedButton(
            text = saveLabel,
            onClick = actions.onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
            loading = state.isSaving
        )
    }
}

@Composable
private fun DaysSelector(
    selectedDays: Set<DayOfWeek>,
    onToggleDay: (DayOfWeek) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.medication_field_days),
            style = MaterialTheme.typography.labelLarge
        )

        DayOfWeek.entries.forEach { day ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = day in selectedDays,
                    onCheckedChange = { onToggleDay(day) }
                )
                Text(stringResource(day.toLabelRes()))
            }
        }
    }
}

@Composable
private fun TimesEditor(
    times: List<LocalTime>,
    onAddTime: () -> Unit,
    onRemoveTime: (LocalTime) -> Unit,
    onUpdateTimes: (List<LocalTime>) -> Unit
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.medication_field_times),
            style = MaterialTheme.typography.labelLarge
        )

        times.forEach { time ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = time.format(timeFormatter),
                    style = MaterialTheme.typography.bodyLarge
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            showTimePicker(context, time) { picked ->
                                onUpdateTimes(times - time + picked)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.medication_change_time))
                    }

                    OutlinedButton(onClick = { onRemoveTime(time) }) {
                        Text(stringResource(R.string.medication_remove_time))
                    }
                }
            }
        }

        OutlinedButton(onClick = onAddTime) {
            Text(stringResource(R.string.medication_add_time))
        }
    }
}

@Composable
private fun FieldErrorText(messageRes: Int?) {
    if (messageRes == null) return

    Text(
        text = stringResource(messageRes),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun <T> SimpleDropdown(
    label: String,
    selectedLabel: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)

        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedLabel)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun showTimePicker(
    context: android.content.Context,
    initial: LocalTime,
    onPicked: (LocalTime) -> Unit
) {
    TimePickerDialog(
        context,
        { _, hour, minute -> onPicked(LocalTime.of(hour, minute)) },
        initial.hour,
        initial.minute,
        true
    ).show()
}

private fun showDatePicker(
    context: android.content.Context,
    initial: LocalDate,
    onPicked: (LocalDate) -> Unit
) {
    DatePickerDialog(
        context,
        { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day)) },
        initial.year,
        initial.monthValue - 1,
        initial.dayOfMonth
    ).show()
}
