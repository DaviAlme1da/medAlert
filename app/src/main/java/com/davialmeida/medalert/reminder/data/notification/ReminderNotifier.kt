package com.davialmeida.medalert.reminder.data.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.davialmeida.medalert.R
import com.davialmeida.medalert.medication.domain.model.Dosage
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.reminder.data.receiver.DoseActionContract
import com.davialmeida.medalert.reminder.data.receiver.DoseActionReceiver
import com.davialmeida.medalert.reminder.domain.model.DueReminder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val manager = NotificationManagerCompat.from(context)

    @SuppressLint("MissingPermission")
    fun show(reminder: DueReminder) {
        if (!canNotify()) return

        val id = notificationId(reminder)
        val text = context.getString(
            R.string.reminder_notification_text,
            formatDosage(reminder.dosage),
            reminder.profileName,
        )

        val notification =
            NotificationCompat.Builder(context, ReminderNotificationChannel.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(reminder.medicationName)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(openAppIntent(id))
                .addAction(
                    0,
                    context.getString(R.string.reminder_action_taken),
                    actionIntent(reminder, id, DoseActionContract.ACTION_TAKEN)
                )
                .addAction(
                    0,
                    context.getString(R.string.reminder_action_skipped),
                    actionIntent(reminder, id, DoseActionContract.ACTION_SKIPPED)
                )
                .addAction(
                    0,
                    context.getString(R.string.reminder_action_snooze),
                    actionIntent(reminder, id, DoseActionContract.ACTION_SNOOZE)
                )
                .build()

        manager.notify(id, notification)
    }

    fun cancel(notificationId: Int) = manager.cancel(notificationId)

    private fun formatDosage(dosage: Dosage): String {
        val amountText = dosage.amount
            .stripTrailingZeros()
            .toPlainString()
            .replace('.', ',')

        val unitText = when (dosage.unit) {
            DosageUnit.MG -> "mg"
            DosageUnit.G -> "g"
            DosageUnit.ML -> "ml"
            DosageUnit.TABLETS -> {
                val quantity = if (dosage.amount.compareTo(BigDecimal.ONE) == 0) 1 else 2
                context.resources.getQuantityString(
                    R.plurals.medication_unit_tablets,
                    quantity,
                )
            }
        }

        return "$amountText $unitText"
    }

    private fun canNotify(): Boolean {
        if (!manager.areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openAppIntent(id: Int): PendingIntent? {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
            ?: return null
        return PendingIntent.getActivity(
            context, id, launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun actionIntent(reminder: DueReminder, id: Int, action: String): PendingIntent {
        val intent = Intent(context, DoseActionReceiver::class.java).apply {
            data =
                Uri.parse("medalert://dose/${reminder.medicationId}/${reminder.scheduledDateTime}/$action")
            putExtra(DoseActionContract.EXTRA_MEDICATION_ID, reminder.medicationId)
            putExtra(
                DoseActionContract.EXTRA_SCHEDULED_DATE_TIME,
                reminder.scheduledDateTime.toString()
            )
            putExtra(DoseActionContract.EXTRA_ACTION, action)
            putExtra(DoseActionContract.EXTRA_NOTIFICATION_ID, id)
        }
        return PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}


fun notificationId(medicationId: Long, scheduledDateTime: LocalDateTime): Int =
    "$medicationId|$scheduledDateTime".hashCode()

fun notificationId(reminder: DueReminder): Int =
    notificationId(reminder.medicationId, reminder.scheduledDateTime)