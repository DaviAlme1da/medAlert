package com.davialmeida.medalert.medication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.medication.domain.error.MedicationError
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.model.MedicationColor
import com.davialmeida.medalert.medication.domain.model.MedicationForm
import com.davialmeida.medalert.medication.domain.validation.MedicationValidator
import com.davialmeida.medalert.medication.presentation.mapper.toField
import com.davialmeida.medalert.medication.presentation.mapper.toMedication
import com.davialmeida.medalert.medication.presentation.state.DefaultDoseTime
import com.davialmeida.medalert.medication.presentation.state.FrequencyType
import com.davialmeida.medalert.medication.presentation.state.MedicationField
import com.davialmeida.medalert.medication.presentation.state.MedicationFormUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Lógica comum de criar e editar. Quem herda só diz:
 *  - qual é o "molde" do medicamento (medicationTemplate)
 *  - como salvar (persist)
 */
abstract class MedicationFormViewModel(
    initialForm: MedicationFormUiState
) : ViewModel() {

    protected val mutableForm = MutableStateFlow(initialForm)
    val form: StateFlow<MedicationFormUiState> = mutableForm.asStateFlow()

    protected abstract fun medicationTemplate(): Medication?

    protected abstract suspend fun persist(medication: Medication): AppResult<*>

    fun updateName(value: String) =
        change(MedicationField.NAME) { copy(name = value) }

    fun updateDosageAmount(value: String) =
        change(MedicationField.DOSAGE) { copy(dosageAmount = value) }

    fun updateDosageUnit(value: DosageUnit) =
        change { copy(dosageUnit = value) }

    fun updateForm(value: MedicationForm) =
        change { copy(form = value) }

    fun updateFrequencyType(value: FrequencyType) = change(
        MedicationField.FREQUENCY,
        MedicationField.DAYS,
        MedicationField.TIMES
    ) { copy(frequencyType = value) }

    fun toggleDay(day: DayOfWeek) = change(MedicationField.DAYS) {
        copy(
            selectedDays = if (day in selectedDays) {
                selectedDays - day
            } else {
                selectedDays + day
            }
        )
    }

    fun addTime() = change(MedicationField.TIMES) {
        val next = (times.maxOrNull() ?: DefaultDoseTime).plusHours(1)
        copy(times = (times + next).distinct().sorted())
    }

    fun removeTime(time: LocalTime) =
        change(MedicationField.TIMES) { copy(times = times - time) }

    fun updateTimes(value: List<LocalTime>) = change(MedicationField.TIMES) {
        copy(times = value.distinct().sorted())
    }

    fun updateIntervalHours(value: Int) =
        change(MedicationField.FREQUENCY) { copy(intervalHours = value) }

    fun updateFirstTime(value: LocalTime) =
        change { copy(firstTime = value) }

    fun updateStartDate(value: LocalDate) =
        change(MedicationField.DATES) { copy(startDate = value) }

    fun updateEndDate(value: LocalDate?) =
        change(MedicationField.DATES) { copy(endDate = value) }

    fun updateColor(value: MedicationColor) =
        change { copy(color = value) }

    fun updateNotes(value: String) =
        change { copy(notes = value) }

    fun updateRemindersEnabled(value: Boolean) =
        change { copy(remindersEnabled = value) }

    fun saveMedication() {
        val current = mutableForm.value

        if (current.isSaving || current.isSaved) return

        val template = medicationTemplate() ?: return
        val medication = current.toMedication(template)
        val errors = MedicationValidator.validateAll(medication)

        if (errors.isNotEmpty()) {
            mutableForm.update {
                it.copy(
                    fieldErrors = errors.associateBy { error -> error.toField() },
                    generalError = null
                )
            }
            return
        }

        viewModelScope.launch {
            mutableForm.update {
                it.copy(
                    isSaving = true,
                    fieldErrors = emptyMap(),
                    generalError = null
                )
            }

            when (val result = persist(medication)) {
                is AppResult.Success -> mutableForm.update {
                    it.copy(isSaving = false, isSaved = true)
                }

                is AppResult.Error -> {
                    val error = result.error
                    val field = (error as? MedicationError)?.toField()

                    mutableForm.update {
                        if (field != null && field != MedicationField.GENERAL) {
                            it.copy(
                                isSaving = false,
                                fieldErrors = mapOf(
                                    field to error as MedicationError
                                )
                            )
                        } else {
                            it.copy(isSaving = false, generalError = error)
                        }
                    }
                }
            }
        }
    }

    private fun change(
        vararg clearing: MedicationField,
        transform: MedicationFormUiState.() -> MedicationFormUiState
    ) {
        mutableForm.update { current ->
            val changed = current.transform()

            changed.copy(
                fieldErrors = changed.fieldErrors - clearing.toSet(),
                generalError = null
            )
        }
    }
}
