package com.davialmeida.medalert.medication.presentation.extension

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.ProfileBlue
import com.davialmeida.medalert.compose.theme.ProfileGreen
import com.davialmeida.medalert.compose.theme.ProfilePurple
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.medication.domain.model.MedicationColor
import com.davialmeida.medalert.medication.domain.model.MedicationForm
import com.davialmeida.medalert.medication.presentation.state.FrequencyType
import com.davialmeida.medalert.medication.domain.rules.MedicationStatus
import androidx.compose.ui.graphics.Color
import java.math.BigDecimal
import java.time.DayOfWeek

@Composable
fun DosageUnit.label(amount: BigDecimal? = null): String {
    return when (this) {
        DosageUnit.MG -> stringResource(R.string.medication_unit_mg)
        DosageUnit.G -> stringResource(R.string.medication_unit_g)
        DosageUnit.ML -> stringResource(R.string.medication_unit_ml)
        DosageUnit.TABLETS -> {
            val isSingular = amount != null && amount.compareTo(BigDecimal.ONE) == 0

            pluralStringResource(
                id = R.plurals.medication_unit_tablets,
                count = if (isSingular) 1 else 2
            )
        }
    }
}

@StringRes
fun MedicationForm.toLabelRes(): Int = when (this) {
    MedicationForm.TABLET -> R.string.medication_form_tablet
    MedicationForm.CAPSULE -> R.string.medication_form_capsule
    MedicationForm.DROPS -> R.string.medication_form_drops
    MedicationForm.LIQUID -> R.string.medication_form_liquid
    MedicationForm.INJECTION -> R.string.medication_form_injection
    MedicationForm.OINTMENT -> R.string.medication_form_ointment
    MedicationForm.OTHER -> R.string.medication_form_other
}

@StringRes
fun FrequencyType.toLabelRes(): Int = when (this) {
    FrequencyType.DAILY -> R.string.medication_frequency_type_daily
    FrequencyType.SPECIFIC_DAYS -> R.string.medication_frequency_type_specific_days
    FrequencyType.EVERY_X_HOURS -> R.string.medication_frequency_type_every_x_hours
}

@StringRes
fun MedicationColor.toLabelRes(): Int = when (this) {
    MedicationColor.BLUE -> R.string.medication_color_blue
    MedicationColor.GREEN -> R.string.medication_color_green
    MedicationColor.PURPLE -> R.string.medication_color_purple
}

@StringRes
fun DayOfWeek.toLabelRes(): Int = when (this) {
    DayOfWeek.MONDAY -> R.string.medication_weekday_monday
    DayOfWeek.TUESDAY -> R.string.medication_weekday_tuesday
    DayOfWeek.WEDNESDAY -> R.string.medication_weekday_wednesday
    DayOfWeek.THURSDAY -> R.string.medication_weekday_thursday
    DayOfWeek.FRIDAY -> R.string.medication_weekday_friday
    DayOfWeek.SATURDAY -> R.string.medication_weekday_saturday
    DayOfWeek.SUNDAY -> R.string.medication_weekday_sunday
}

@StringRes
fun MedicationStatus.toLabelRes(): Int = when (this) {
    MedicationStatus.ACTIVE -> R.string.medication_status_active
    MedicationStatus.SCHEDULED -> R.string.medication_status_scheduled
    MedicationStatus.ENDED -> R.string.medication_status_paused
}

fun MedicationColor.toComposeColor(): Color = when (this) {
    MedicationColor.BLUE -> ProfileBlue
    MedicationColor.GREEN -> ProfileGreen
    MedicationColor.PURPLE -> ProfilePurple
}
