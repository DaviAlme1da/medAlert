package com.davialmeida.medalert.today.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.DoseActionSheet
import com.davialmeida.medalert.compose.component.DoseCard
import com.davialmeida.medalert.compose.component.DoseStatusUi
import com.davialmeida.medalert.compose.component.MedButton
import com.davialmeida.medalert.compose.component.MedCard
import com.davialmeida.medalert.compose.component.ProfileAvatar
import com.davialmeida.medalert.compose.component.ProgressCard
import com.davialmeida.medalert.compose.component.WeekStrip
import com.davialmeida.medalert.medication.presentation.extension.toComposeColor
import com.davialmeida.medalert.medication.presentation.extension.toDisplayText
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.presentation.extension.toComposeColor
import com.davialmeida.medalert.reminder.presentation.viewmodel.ReminderPermissionViewModel
import com.davialmeida.medalert.today.presentation.extension.toTodayMessageRes
import com.davialmeida.medalert.today.presentation.viewmodel.TodayViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm")

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

@Composable
fun TodayScreen(
    onOpenProfiles: () -> Unit,
    modifier: Modifier = Modifier,
    onAddMedication: (() -> Unit)? = null,
    viewModel: TodayViewModel = hiltViewModel(),
    permissionViewModel: ReminderPermissionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedDoseKey by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val selectedDose = selectedDoseKey?.let { key ->
        state.items.firstOrNull { it.key == key }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permissionViewModel.refresh()
    }

    Column(modifier = modifier.fillMaxSize()) {

        // Faixa branca: perfil + semana
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProfileSwitcher(
                    profiles = state.profiles,
                    activeProfile = state.activeProfile,
                    onSelectProfile = viewModel::selectProfile,
                    onManageProfiles = onOpenProfiles,
                )

                WeekStrip(
                    days = state.weekDays,
                    selected = state.selectedDate,
                    today = state.today,
                    onSelect = viewModel::selectDate,
                    onPreviousWeek = viewModel::previousWeek,
                    onNextWeek = viewModel::nextWeek,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {


            item {
                Text(
                    text = if (state.selectedDate == state.today) {
                        stringResource(R.string.today_screen_title)
                    } else {
                        stringResource(
                            R.string.today_doses_of_date,
                            state.selectedDate.format(dateFormatter),
                        )
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            state.actionError?.let { error ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(error.toTodayMessageRes()),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        TextButton(
                            onClick = viewModel::clearActionError,
                        ) {
                            Text(stringResource(R.string.today_error_dismiss))
                        }
                    }
                }
            }

            when {
                state.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                state.hasLoadError -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.today_load_error),
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.error,
                            )

                            TextButton(onClick = viewModel::retry) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }

                state.activeProfile == null -> {
                    item {
                        Text(
                            text = stringResource(
                                R.string.today_no_profile_message
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                state.items.isEmpty() -> {
                    item {
                        EmptyDayCard(
                            onAddMedication = onAddMedication,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                else -> {
                    item {
                        ProgressCard(
                            taken = state.progress.taken,
                            total = state.progress.total,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    val groups = state.items.groupBy { it.time }

                    groups.forEach { (time, doses) ->
                        item(key = "header-$time") {
                            Text(
                                text = time.format(timeFormatter),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }

                        items(
                            items = doses,
                            key = { it.key },
                        ) { dose ->
                            DoseCard(
                                name = dose.name,
                                dosage = dose.dosage.toDisplayText(),
                                time = dose.time.format(timeFormatter),
                                showTime = false,
                                status = dose.status,
                                dotColor = dose.color.toComposeColor(),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    selectedDoseKey = dose.key
                                },
                                onTakeClick = if (
                                    dose.status == DoseStatusUi.Pending &&
                                    dose.canAct
                                ) {
                                    {
                                        viewModel.markTaken(
                                            dose.medicationId,
                                            dose.scheduledDateTime,
                                        )
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    selectedDose?.let { dose ->
        DoseActionSheet(
            medicationName = dose.name,
            time = dose.time.format(timeFormatter),
            subtitle = dose.dosage.toDisplayText(),
            canAct = dose.canAct,
            hasLog = dose.hasLog,
            onMarkTaken = {
                viewModel.markTaken(
                    dose.medicationId,
                    dose.scheduledDateTime,
                )
                selectedDoseKey = null
            },
            onSkip = {
                viewModel.markSkipped(
                    dose.medicationId,
                    dose.scheduledDateTime,
                )
                selectedDoseKey = null
            },
            onUndo = {
                viewModel.undo(
                    dose.medicationId,
                    dose.scheduledDateTime,
                )
                selectedDoseKey = null
            },
            onDismiss = {
                selectedDoseKey = null
            },
        )
    }
}

@Composable
private fun EmptyDayCard(
    onAddMedication: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    MedCard(
        modifier = modifier,
        contentPadding = PaddingValues(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.today_empty_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(R.string.today_empty_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            if (onAddMedication != null) {
                MedButton(
                    text = stringResource(R.string.add_medication),
                    onClick = onAddMedication,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    icon = painterResource(R.drawable.ic_med_add),
                )
            }
        }
    }
}

@Composable
private fun ProfileSwitcher(
    profiles: List<Profile>,
    activeProfile: Profile?,
    onSelectProfile: (Long) -> Unit,
    onManageProfiles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { expanded = true }
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (activeProfile != null) {
                ProfileAvatar(
                    name = activeProfile.name,
                    color = activeProfile.color.toComposeColor(),
                    size = 40.dp,
                )
            }

            Column {
                Text(
                    text = stringResource(R.string.today_greeting),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = activeProfile?.name
                            ?: stringResource(
                                R.string.today_no_profile_selected
                            ),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_med_chevron_down),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            profiles.forEach { profile ->
                DropdownMenuItem(
                    text = { Text(profile.name) },
                    leadingIcon = {
                        ProfileAvatar(
                            name = profile.name,
                            color = profile.color.toComposeColor(),
                            size = 32.dp,
                        )
                    },
                    onClick = {
                        onSelectProfile(profile.id)
                        expanded = false
                    },
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.today_manage_profiles))
                },
                onClick = {
                    expanded = false
                    onManageProfiles()
                },
            )
        }
    }
}