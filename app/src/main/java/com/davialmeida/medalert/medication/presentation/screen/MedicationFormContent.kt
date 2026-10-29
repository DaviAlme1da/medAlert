package com.davialmeida.medalert.medication.presentation.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.ColorOption
import com.davialmeida.medalert.compose.component.MedButton
import com.davialmeida.medalert.compose.component.MedButtonStyle
import com.davialmeida.medalert.compose.component.MedFilterChip
import com.davialmeida.medalert.compose.component.MedTextField
import com.davialmeida.medalert.compose.component.WeekdayChip
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.model.MedicationColor
import com.davialmeida.medalert.medication.domain.model.MedicationForm
import com.davialmeida.medalert.medication.presentation.extension.label
import com.davialmeida.medalert.medication.presentation.extension.toComposeColor
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

/**
 * Agrupa as ações do formulário para não passar 17 lambdas soltas.
 */
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

@OptIn(ExperimentalLayoutApi::class)
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
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        MedTextField(
            value = state.name,
            onValueChange = actions.onNameChange,
            label = stringResource(R.string.medication_field_name),
            modifier = Modifier.fillMaxWidth(),
            error = state.fieldErrors[MedicationField.NAME]
                ?.toMessageRes()?.let { stringResource(it) }
        )

        FormSection(title = stringResource(R.string.medication_field_form)) {
            FlowRow(
                modifier = Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedicationForm.entries.forEach { form ->
                    MedFilterChip(
                        label = stringResource(form.toLabelRes()),
                        selected = state.form == form,
                        onClick = { actions.onFormChange(form) }
                    )
                }
            }
        }

        FormSection(title = stringResource(R.string.medication_detail_dosage)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                MedTextField(
                    value = state.dosageAmount,
                    onValueChange = actions.onDosageAmountChange,
                    label = stringResource(R.string.medication_field_dosage_amount),
                    modifier = Modifier.weight(1f),
                    error = state.fieldErrors[MedicationField.DOSAGE]
                        ?.toMessageRes()?.let { stringResource(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                SimpleDropdown(
                    label = stringResource(R.string.medication_field_dosage_unit),
                    selectedLabel = state.dosageUnit.label(),
                    options = DosageUnit.entries.toList(),
                    optionLabel = { it.label() },
                    onSelected = actions.onDosageUnitChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp)
                )
            }

            Text(
                text = stringResource(R.string.medication_dosage_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FormSection(title = stringResource(R.string.medication_frequency_section)) {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FrequencyType.entries.forEach { type ->
                    FrequencyOption(
                        label = stringResource(type.toLabelRes()),
                        selected = state.frequencyType == type,
                        onClick = { actions.onFrequencyTypeChange(type) }
                    )
                }
            }

            when (state.frequencyType) {
                FrequencyType.DAILY,
                FrequencyType.SPECIFIC_DAYS -> {
                    if (state.frequencyType == FrequencyType.SPECIFIC_DAYS) {
                        DaysSelector(
                            selectedDays = state.selectedDays,
                            onToggleDay = actions.onToggleDay
                        )

                        FieldErrorText(
                            state.fieldErrors[MedicationField.DAYS]?.toMessageRes()
                        )
                    }

                    TimesEditor(
                        times = state.times,
                        onAddTime = actions.onAddTime,
                        onRemoveTime = actions.onRemoveTime,
                        onUpdateTimes = actions.onUpdateTimes
                    )

                    FieldErrorText(
                        state.fieldErrors[MedicationField.TIMES]?.toMessageRes()
                    )
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
                                R.plurals.medication_interval_hours,
                                it,
                                it
                            )
                        },
                        onSelected = actions.onIntervalHoursChange,
                        modifier = Modifier.fillMaxWidth()
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
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        MedButton(
                            text = stringResource(R.string.medication_pick_time),
                            onClick = {
                                showTimePicker(context, state.firstTime) {
                                    actions.onFirstTimeChange(it)
                                }
                            },
                            style = MedButtonStyle.Outlined,
                            compact = true
                        )
                    }

                    FieldErrorText(
                        state.fieldErrors[MedicationField.FREQUENCY]?.toMessageRes()
                    )
                }
            }
        }

        FormSection(title = stringResource(R.string.medication_period_section)) {
            MedButton(
                text = stringResource(
                    R.string.medication_start_date,
                    state.startDate.format(dateFormatter)
                ),
                onClick = {
                    showDatePicker(context, state.startDate) {
                        actions.onStartDateChange(it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                style = MedButtonStyle.Outlined,
                enabled = startDateEditable
            )

            MedButton(
                text = state.endDate?.let {
                    stringResource(
                        R.string.medication_end_date,
                        it.format(dateFormatter)
                    )
                } ?: stringResource(R.string.medication_set_end_date),
                onClick = {
                    showDatePicker(context, state.endDate ?: state.startDate) {
                        actions.onEndDateChange(it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                style = MedButtonStyle.Outlined
            )

            if (state.endDate != null) {
                MedButton(
                    text = stringResource(R.string.medication_remove_end_date),
                    onClick = { actions.onEndDateChange(null) },
                    modifier = Modifier.fillMaxWidth(),
                    style = MedButtonStyle.Text
                )
            }

            FieldErrorText(state.fieldErrors[MedicationField.DATES]?.toMessageRes())
        }

        FormSection(title = stringResource(R.string.medication_field_color)) {
            Row(
                modifier = Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedicationColor.entries.forEach { color ->
                    ColorOption(
                        color = color.toComposeColor(),
                        name = stringResource(color.toLabelRes()),
                        selected = state.color == color,
                        onClick = { actions.onColorChange(color) }
                    )
                }
            }
        }

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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.medication_field_reminders),
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = stringResource(R.string.medication_reminders_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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
            loading = state.isSaving,
            icon = painterResource(R.drawable.ic_med_check)
        )
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        content()
    }
}

@Composable
private fun FrequencyOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        shape = shape,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(selected = selected, onClick = null)

            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DaysSelector(
    selectedDays: Set<DayOfWeek>,
    onToggleDay: (DayOfWeek) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.medication_field_days),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            DayOfWeek.entries.forEach { day ->
                val name = stringResource(day.toLabelRes())

                WeekdayChip(
                    label = name.first().uppercase(),
                    contentDescription = name,
                    selected = day in selectedDays,
                    onClick = { onToggleDay(day) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            times.forEach { time ->
                TimeChip(
                    text = time.format(timeFormatter),
                    onEdit = {
                        showTimePicker(context, time) { picked ->
                            onUpdateTimes(times - time + picked)
                        }
                    },
                    onRemove = { onRemoveTime(time) }
                )
            }

            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onAddTime),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_med_add),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = stringResource(R.string.medication_add_time),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeChip(
    text: String,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    val editLabel = stringResource(R.string.medication_change_time)
    val removeLabel = stringResource(R.string.medication_remove_time)

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClickLabel = editLabel, onClick = onEdit)
                    .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClickLabel = removeLabel, onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = removeLabel,
                    modifier = Modifier.size(16.dp)
                )
            }
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
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = selectedLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.ic_med_chevron_down),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
    context: Context,
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
    context: Context,
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