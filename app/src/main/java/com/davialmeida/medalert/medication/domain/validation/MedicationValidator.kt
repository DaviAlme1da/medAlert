package com.davialmeida.medalert.medication.domain.validation

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.medication.domain.error.MedicationError
import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.model.Medication
import java.math.BigDecimal.ZERO
import java.time.LocalTime

const val MEDICATION_NAME_MAX_LENGTH = 60

object MedicationValidator {

    fun validateAll(
        medication: Medication
    ): List<MedicationError> {
        val errors = mutableListOf<MedicationError>()

        if (medication.name.isBlank()) {
            errors.add(MedicationError.EmptyName)
        }

        if (medication.name.length > MEDICATION_NAME_MAX_LENGTH) {
            errors.add(MedicationError.NameTooLong)
        }

        if (medication.dosage.amount <= ZERO) {
            errors.add(MedicationError.InvalidDosageAmount)
        }

        if (
            medication.endDate != null &&
            medication.endDate < medication.startDate
        ) {
            errors.add(MedicationError.EndDateBeforeStartDate)
        }

        when (val frequency = medication.frequency) {
            is Frequency.Daily -> {
                validateTimes(frequency.times, errors)
            }

            is Frequency.SpecificDays -> {
                validateTimes(frequency.times, errors)

                if (frequency.daysOfWeek.isEmpty()) {
                    errors.add(MedicationError.NoDaysSelected)
                }
            }

            is Frequency.EveryXHours -> {
                if (
                    frequency.intervalHours !in
                    Frequency.VALID_EVERY_X_HOURS_INTERVALS
                ) {
                    errors.add(MedicationError.InvalidInterval)
                }
            }
        }

        return errors.distinct()
    }

    fun validate(
        medication: Medication
    ): AppResult<Unit> {
        val firstError = validateAll(medication).firstOrNull()
            ?: return AppResult.Success(Unit)

        return AppResult.Error(firstError)
    }

    private fun validateTimes(
        times: List<LocalTime>,
        errors: MutableList<MedicationError>
    ) {
        if (times.isEmpty()) {
            errors.add(MedicationError.NoTimes)
        }

        if (times.size != times.distinct().size) {
            errors.add(MedicationError.DuplicateTimes)
        }
    }
}