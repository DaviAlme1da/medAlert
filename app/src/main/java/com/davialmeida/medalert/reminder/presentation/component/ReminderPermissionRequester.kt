package com.davialmeida.medalert.reminder.presentation.component

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.davialmeida.medalert.reminder.domain.model.ReminderPermissionStatus

@Composable
fun ReminderPermissionRequester(
    status: ReminderPermissionStatus,
    onPermissionResult: () -> Unit,
    content: @Composable (onRequestPermission: () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var notificationRequested by rememberSaveable {
        mutableStateOf(false)
    }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) {
            onPermissionResult()
        }

    fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

        runCatching {
            context.startActivity(intent)
        }
    }

    fun requestPermission() {
        when {
            !status.notificationsAllowed -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val blocked = notificationRequested &&
                            activity != null &&
                            !ActivityCompat.shouldShowRequestPermissionRationale(
                                activity,
                                Manifest.permission.POST_NOTIFICATIONS
                            )

                    if (blocked) {
                        openNotificationSettings()
                    } else {
                        notificationRequested = true
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                } else {
                    openNotificationSettings()
                }
            }

            !status.exactAlarmsAllowed &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val intent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:${context.packageName}")
                )

                runCatching {
                    context.startActivity(intent)
                }
            }
        }
    }

    content(::requestPermission)
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
