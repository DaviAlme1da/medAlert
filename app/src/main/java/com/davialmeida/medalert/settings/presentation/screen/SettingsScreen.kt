package com.davialmeida.medalert.settings.presentation.screen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.OptionDialog
import com.davialmeida.medalert.compose.component.SettingsRow
import com.davialmeida.medalert.compose.component.SettingsSection
import com.davialmeida.medalert.reminder.presentation.component.ReminderPermissionRequester
import com.davialmeida.medalert.reminder.presentation.viewmodel.ReminderPermissionViewModel
import com.davialmeida.medalert.settings.domain.model.SNOOZE_OPTIONS
import com.davialmeida.medalert.settings.domain.model.ThemePreference
import com.davialmeida.medalert.settings.presentation.viewmodel.SettingsViewModel

private enum class SettingsDialog { THEME, SNOOZE }

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

    var openDialog by rememberSaveable { mutableStateOf<SettingsDialog?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permissionViewModel.refresh()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_settings),
            style = MaterialTheme.typography.headlineMedium
        )

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

        SettingsSection(title = stringResource(R.string.settings_section_profiles)) {
            SettingsRow(
                title = stringResource(R.string.settings_profiles_manage),
                onClick = onProfilesClick
            )
        }

        SettingsSection(title = stringResource(R.string.settings_section_reminders)) {
            ReminderPermissionRequester(
                status = permissionStatus,
                onPermissionResult = permissionViewModel::refresh
            ) { requestPermission ->
                SettingsRow(
                    title = stringResource(R.string.settings_notifications),
                    subtitle = permissionSubtitle(permissionStatus.notificationsAllowed),
                    onClick = if (permissionStatus.notificationsAllowed) null else requestPermission
                )

                SettingsRow(
                    title = stringResource(R.string.settings_exact_alarms),
                    subtitle = permissionSubtitle(permissionStatus.exactAlarmsAllowed),
                    // Só pede o alarme exato depois das notificações.
                    onClick = if (
                        permissionStatus.exactAlarmsAllowed ||
                        !permissionStatus.notificationsAllowed
                    ) {
                        null
                    } else {
                        requestPermission
                    }
                )
            }

            SettingsRow(
                title = stringResource(R.string.settings_snooze_title),
                subtitle = pluralStringResource(
                    R.plurals.settings_snooze_minutes,
                    settings.snoozeMinutes,
                    settings.snoozeMinutes
                ),
                onClick = { openDialog = SettingsDialog.SNOOZE }
            )
        }

        SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
            SettingsRow(
                title = stringResource(R.string.settings_theme_title),
                subtitle = themeLabel(settings.theme),
                onClick = { openDialog = SettingsDialog.THEME }
            )
        }

        SettingsSection(title = stringResource(R.string.settings_section_about)) {
            SettingsRow(
                title = stringResource(R.string.settings_version),
                subtitle = versionName
            )
        }
    }

    when (openDialog) {
        SettingsDialog.THEME -> OptionDialog(
            title = stringResource(R.string.settings_theme_title),
            options = ThemePreference.entries,
            selected = settings.theme,
            label = { themeLabel(it) },
            onSelect = {
                viewModel.onThemeSelected(it)
                openDialog = null
            },
            onDismiss = { openDialog = null }
        )

        SettingsDialog.SNOOZE -> OptionDialog(
            title = stringResource(R.string.settings_snooze_title),
            options = SNOOZE_OPTIONS,
            selected = settings.snoozeMinutes,
            label = {
                pluralStringResource(R.plurals.settings_snooze_minutes, it, it)
            },
            onSelect = {
                viewModel.onSnoozeMinutesSelected(it)
                openDialog = null
            },
            onDismiss = { openDialog = null }
        )

        null -> Unit
    }
}

@Composable
private fun permissionSubtitle(allowed: Boolean): String = stringResource(
    if (allowed) R.string.settings_permission_allowed else R.string.settings_permission_denied
)

@Composable
private fun themeLabel(theme: ThemePreference): String = stringResource(
    when (theme) {
        ThemePreference.SYSTEM -> R.string.settings_theme_system
        ThemePreference.LIGHT -> R.string.settings_theme_light
        ThemePreference.DARK -> R.string.settings_theme_dark
    }
)

@Suppress("DEPRECATION")
private fun Context.appVersionName(): String =
    runCatching { packageManager.getPackageInfo(packageName, 0).versionName }
        .getOrNull()
        .orEmpty()