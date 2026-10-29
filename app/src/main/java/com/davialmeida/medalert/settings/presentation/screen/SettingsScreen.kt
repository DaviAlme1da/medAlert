package com.davialmeida.medalert.settings.presentation.screen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.MedSegmentedControl
import com.davialmeida.medalert.compose.component.MedTabHeader
import com.davialmeida.medalert.compose.component.OptionDialog
import com.davialmeida.medalert.compose.component.SettingsRow
import com.davialmeida.medalert.compose.component.SettingsSection
import com.davialmeida.medalert.compose.theme.statusColors
import com.davialmeida.medalert.reminder.presentation.component.ReminderPermissionRequester
import com.davialmeida.medalert.reminder.presentation.viewmodel.ReminderPermissionViewModel
import com.davialmeida.medalert.settings.domain.model.SNOOZE_OPTIONS
import com.davialmeida.medalert.settings.domain.model.ThemePreference
import com.davialmeida.medalert.settings.presentation.viewmodel.SettingsViewModel

// Ordem do Figma: Claro, Escuro, Sistema.
private val themeOptions = listOf(
    ThemePreference.LIGHT,
    ThemePreference.DARK,
    ThemePreference.SYSTEM
)

@Composable
fun SettingsScreen(
    onProfilesClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    permissionViewModel: ReminderPermissionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionStatus by permissionViewModel.status.collectAsStateWithLifecycle()
    val settings = state.settings

    val context = LocalContext.current
    val versionName = remember { context.appVersionName() }

    var showSnoozeDialog by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permissionViewModel.refresh()
    }

    Column(modifier = modifier.fillMaxSize()) {
        MedTabHeader(
            title = stringResource(R.string.nav_settings),
            subtitle = stringResource(R.string.settings_subtitle)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (state.hasSaveError) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_save_error),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    TextButton(onClick = viewModel::clearSaveError) {
                        Text(stringResource(R.string.today_error_dismiss))
                    }
                }
            }

            SettingsSection(
                title = stringResource(R.string.settings_section_permissions)
            ) {
                ReminderPermissionRequester(
                    status = permissionStatus,
                    onPermissionResult = permissionViewModel::refresh
                ) { requestPermission ->
                    val canAskNotifications = !permissionStatus.notificationsAllowed

                    // Só pede o alarme exato depois das notificações.
                    val canAskExact = !permissionStatus.exactAlarmsAllowed &&
                            permissionStatus.notificationsAllowed

                    SettingsRow(
                        title = stringResource(R.string.settings_notifications),
                        subtitle = stringResource(R.string.settings_notifications_desc),
                        leadingIcon = R.drawable.ic_notification,
                        onClick = if (canAskNotifications) requestPermission else null,
                        trailing = {
                            PermissionStatus(
                                allowed = permissionStatus.notificationsAllowed,
                                canConfigure = canAskNotifications
                            )
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    SettingsRow(
                        title = stringResource(R.string.settings_exact_alarms),
                        subtitle = stringResource(R.string.settings_exact_alarms_desc),
                        leadingIcon = R.drawable.ic_med_alarm,
                        onClick = if (canAskExact) requestPermission else null,
                        trailing = {
                            PermissionStatus(
                                allowed = permissionStatus.exactAlarmsAllowed,
                                canConfigure = canAskExact
                            )
                        }
                    )
                }
            }

            SettingsSection(
                title = stringResource(R.string.settings_section_reminders)
            ) {
                SettingsRow(
                    title = stringResource(R.string.settings_snooze_title),
                    onClick = { showSnoozeDialog = true },
                    trailing = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = pluralStringResource(
                                    R.plurals.settings_snooze_minutes,
                                    settings.snoozeMinutes,
                                    settings.snoozeMinutes
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Icon(
                                painter = painterResource(R.drawable.ic_med_chevron_right),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }

            SettingsSection(
                title = stringResource(R.string.settings_section_appearance),
                card = false
            ) {
                MedSegmentedControl(
                    options = themeOptions.map { themeShortLabel(it) },
                    selectedIndex = themeOptions.indexOf(settings.theme),
                    onSelect = { viewModel.onThemeSelected(themeOptions[it]) }
                )
            }

            SettingsSection(
                title = stringResource(R.string.settings_section_profiles)
            ) {
                SettingsRow(
                    title = stringResource(R.string.settings_profiles_manage),
                    onClick = onProfilesClick
                )
            }

            SettingsSection(
                title = stringResource(R.string.settings_section_about)
            ) {
                SettingsRow(
                    title = stringResource(R.string.app_name),
                    subtitle = stringResource(R.string.settings_version) +
                            " " + versionName
                )
            }
        }
    }

    if (showSnoozeDialog) {
        OptionDialog(
            title = stringResource(R.string.settings_snooze_title),
            options = SNOOZE_OPTIONS,
            selected = settings.snoozeMinutes,
            label = {
                pluralStringResource(R.plurals.settings_snooze_minutes, it, it)
            },
            onSelect = {
                viewModel.onSnoozeMinutesSelected(it)
                showSnoozeDialog = false
            },
            onDismiss = { showSnoozeDialog = false }
        )
    }
}

@Composable
private fun PermissionStatus(
    allowed: Boolean,
    canConfigure: Boolean
) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = stringResource(
                if (allowed) {
                    R.string.settings_status_active
                } else {
                    R.string.settings_status_inactive
                }
            ),
            style = MaterialTheme.typography.labelLarge,
            color = if (allowed) {
                MaterialTheme.statusColors.onSuccessContainer
            } else {
                MaterialTheme.statusColors.onDangerContainer
            }
        )

        if (canConfigure) {
            Text(
                text = stringResource(R.string.settings_configure),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun themeShortLabel(theme: ThemePreference): String = stringResource(
    when (theme) {
        ThemePreference.SYSTEM -> R.string.settings_theme_system_short
        ThemePreference.LIGHT -> R.string.settings_theme_light
        ThemePreference.DARK -> R.string.settings_theme_dark
    }
)

@Suppress("DEPRECATION")
private fun Context.appVersionName(): String =
    runCatching { packageManager.getPackageInfo(packageName, 0).versionName }
        .getOrNull()
        .orEmpty()