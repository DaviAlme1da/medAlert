package com.davialmeida.medalert.today.presentation.screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.DoseActionSheet
import com.davialmeida.medalert.compose.component.DoseCard
import com.davialmeida.medalert.compose.component.DoseStatusUi
import com.davialmeida.medalert.compose.component.ProfileAvatar
import com.davialmeida.medalert.compose.component.ProgressCard
import com.davialmeida.medalert.compose.component.WeekStrip
import com.davialmeida.medalert.medication.presentation.extension.toComposeColor
import com.davialmeida.medalert.medication.presentation.extension.toDisplayText
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.presentation.extension.toComposeColor
import com.davialmeida.medalert.reminder.presentation.ReminderPermissionBanner
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
    viewModel: TodayViewModel = hiltViewModel(),
    permissionViewModel: ReminderPermissionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionStatus by permissionViewModel.status.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var selectedDoseKey by rememberSaveable { mutableStateOf<String?>(null) }
    var notificationRequested by rememberSaveable { mutableStateOf(false) }

    val selectedDose = selectedDoseKey?.let { key ->
        state.items.firstOrNull { it.key == key }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permissionViewModel.refresh()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {

        permissionViewModel.refresh()
    }

    fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        runCatching { context.startActivity(intent) }
    }

    fun handleReminderPermissionAction() {
        when {
            !permissionStatus.notificationsAllowed -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    val blocked = notificationRequested &&
                            activity != null &&
                            !ActivityCompat.shouldShowRequestPermissionRationale(
                                activity,
                                Manifest.permission.POST_NOTIFICATIONS,
                            )
                    if (blocked) {
                        openNotificationSettings()
                    } else {
                        notificationRequested = true
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS,
                        )
                    }
                } else {
                    openNotificationSettings()
                }
            }

            !permissionStatus.exactAlarmsAllowed &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val intent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:${context.packageName}"),
                )
                runCatching { context.startActivity(intent) }
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        if (!permissionStatus.isComplete) {
            item {
                ReminderPermissionBanner(
                    status = permissionStatus,
                    onActionClick = { handleReminderPermissionAction() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.today_screen_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                ProfileSwitcher(
                    profiles = state.profiles,
                    activeProfile = state.activeProfile,
                    onSelectProfile = viewModel::selectProfile,
                    onManageProfiles = onOpenProfiles,
                )
            }
        }

        item {
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

                    TextButton(onClick = viewModel::clearActionError) {
                        Text(stringResource(R.string.today_error_dismiss))
                    }
                }
            }
        }

        when {
            state.isLoading -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.hasLoadError -> item {
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

            state.activeProfile == null -> item {
                Text(
                    text = stringResource(R.string.today_no_profile_message),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            else -> {
                item {
                    ProgressCard(
                        taken = state.progress.taken,
                        total = state.progress.total,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (state.items.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.today_empty_day),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    item {
                        Text(
                            text = if (state.selectedDate == state.today) {
                                stringResource(R.string.today_doses_today)
                            } else {
                                stringResource(
                                    R.string.today_doses_of_date,
                                    state.selectedDate.format(dateFormatter),
                                )
                            },
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }

                    items(
                        items = state.items,
                        key = { it.key },
                    ) { dose ->
                        DoseCard(
                            name = dose.name,
                            dosage = dose.dosage.toDisplayText(),
                            time = dose.time.format(timeFormatter),
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

    selectedDose?.let { dose ->
        DoseActionSheet(
            medicationName = dose.name,
            time = dose.time.format(timeFormatter),
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
private fun ProfileSwitcher(
    profiles: List<Profile>,
    activeProfile: Profile?,
    onSelectProfile: (Long) -> Unit,
    onManageProfiles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(onClick = { expanded = true }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (activeProfile != null) {
                    ProfileAvatar(
                        name = activeProfile.name,
                        color = activeProfile.color.toComposeColor(),
                        size = 32.dp,
                    )
                }

                Text(
                    text = activeProfile?.name
                        ?: stringResource(R.string.today_no_profile_selected),
                )
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
                text = { Text(stringResource(R.string.today_manage_profiles)) },
                onClick = {
                    expanded = false
                    onManageProfiles()
                },
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}