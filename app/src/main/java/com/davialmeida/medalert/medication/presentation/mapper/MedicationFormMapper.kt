package com.davialmeida.medalert.medication.presentation.mapper

import com.davialmeida.medalert.medication.domain.error.MedicationError
import com.davialmeida.medalert.medication.domain.model.Dosage
import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.presentation.state.FrequencyType
import com.davialmeida.medalert.medication.presentation.state.MedicationField
import com.davialmeida.medalert.medication.presentation.state.MedicationFormUiState
import java.math.BigDecimal

fun parseDosageAmount(text: String): BigDecimal? {
    return text.trim().replace(',', '.').toBigDecimalOrNull()
}

fun MedicationFormUiState.toFrequency(): Frequency {
    return when (frequencyType) {
        FrequencyType.DAILY -> Frequency.Daily(times)

        FrequencyType.SPECIFIC_DAYS -> Frequency.SpecificDays(
            daysOfWeek = selectedDays,
            times = times
        )

        FrequencyType.EVERY_X_HOURS -> Frequency.EveryXHours(
            intervalHours = intervalHours,
            firstTime = firstTime
        )
    }
}

/**
 * Aplica o formulário sobre um "molde" (id, seriesId e profileId vêm dele).
 * Quantidade ilegível vira zero, para o validador reportá-la junto
 * com os demais erros.
 */
fun MedicationFormUiState.toMedication(template: Medication): Medication {
    val amount = parseDosageAmount(dosageAmount) ?: BigDecimal.ZERO

    return template.copy(
        name = name.trim(),
        dosage = Dosage(amount, dosageUnit),
        form = form,
        frequency = toFrequency(),
        startDate = startDate,
        endDate = endDate,
        color = color,
        notes = notes.trim(),
        remindersEnabled = remindersEnabled
    )
}

fun Medication.toFormState(): MedicationFormUiState {
    val base = MedicationFormUiState(
        startDate = startDate,
        name = name,
        dosageAmount = dosage.amount.stripTrailingZeros().toPlainString(),
        dosageUnit = dosage.unit,
        form = form,
        endDate = endDate,
        color = color,
        notes = notes,
        remindersEnabled = remindersEnabled
    )

    return when (val value = frequency) {
        is Frequency.Daily -> base.copy(
            frequencyType = FrequencyType.DAILY,
            times = value.times.sorted()
        )

        is Frequency.SpecificDays -> base.copy(
            frequencyType = FrequencyType.SPECIFIC_DAYS,
            selectedDays = value.daysOfWeek,
            times = value.times.sorted()
        )

        is Frequency.EveryXHours -> base.copy(
            frequencyType = FrequencyType.EVERY_X_HOURS,
            intervalHours = value.intervalHours,
            firstTime = value.firstTime
        )
    }
}

fun MedicationError.toField(): MedicationField = when (this) {
    MedicationError.EmptyName,
    MedicationError.NameTooLong -> MedicationField.NAME

    MedicationError.InvalidDosageAmount -> MedicationField.DOSAGE

    MedicationError.NoTimes,
    MedicationError.DuplicateTimes -> MedicationField.TIMES

    MedicationError.NoDaysSelected -> MedicationField.DAYS
    MedicationError.InvalidInterval -> MedicationField.FREQUENCY

    MedicationError.EndDateBeforeStartDate,
    MedicationError.EndDateInPast -> MedicationField.DATES

    MedicationError.NoActiveProfile,
    MedicationError.NotFound,
    MedicationError.CannotEdit,
    MedicationError.CannotPause,
    MedicationError.CannotResume -> MedicationField.GENERAL
}
