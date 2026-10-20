package com.davialmeida.medalert.medication.presentation.state

import com.davialmeida.medalert.core.domain.AppError
import com.davialmeida.medalert.medication.domain.error.MedicationError
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.medication.domain.model.MedicationColor
import com.davialmeida.medalert.medication.domain.model.MedicationForm
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

enum class FrequencyType {
    DAILY,
    SPECIFIC_DAYS,
    EVERY_X_HOURS
}

enum class MedicationField {
    NAME,
    DOSAGE,
    FREQUENCY,
    DAYS,
    TIMES,
    DATES,
    GENERAL
}

val DefaultDoseTime: LocalTime = LocalTime.of(8, 0)

/** Estado do formulário, compartilhado entre criar e editar. */
data class MedicationFormUiState(
    val startDate: LocalDate,
    val name: String = "",
    val dosageAmount: String = "",
    val dosageUnit: DosageUnit = DosageUnit.MG,
    val form: MedicationForm = MedicationForm.TABLET,
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val selectedDays: Set<DayOfWeek> = emptySet(),
    val times: List<LocalTime> = listOf(DefaultDoseTime),
    val intervalHours: Int = 8,
    val firstTime: LocalTime = DefaultDoseTime,
    val endDate: LocalDate? = null,
    val color: MedicationColor = MedicationColor.BLUE,
    val notes: String = "",
    val remindersEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val fieldErrors: Map<MedicationField, MedicationError> = emptyMap(),
    val generalError: AppError? = null
)
