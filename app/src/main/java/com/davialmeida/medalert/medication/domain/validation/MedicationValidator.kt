package com.davialmeida.medalert.medication.domain.validation

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.medication.domain.error.MedicationError
import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.model.Medication
import java.math.BigDecimal.ZERO
import java.time.LocalTime

object MedicationValidator {

    fun validate(
        medication: Medication
    ): AppResult<Unit> {

        if (medication.name.isBlank()) {
            return AppResult.Error(
                MedicationError.EmptyName
            )
        }

        if (medication.dosage.amount <= ZERO) {
            return AppResult.Error(
                MedicationError.InvalidDosageAmount
            )
        }

        if (
            medication.endDate != null &&
            medication.endDate < medication.startDate
        ) {
            return AppResult.Error(
                MedicationError.EndDateBeforeStartDate
            )
        }

        when (val frequency = medication.frequency) {

            is Frequency.Daily -> {
                validateTimes(frequency.times)?.let {
                    return AppResult.Error(it)
                }
            }

            is Frequency.SpecificDays -> {

                validateTimes(frequency.times)?.let {
                    return AppResult.Error(it)
                }

                if (frequency.daysOfWeek.isEmpty()) {
                    return AppResult.Error(
                        MedicationError.NoDaysSelected
                    )
                }
            }

            is Frequency.EveryXHours -> {

                if (
                    frequency.intervalHours !in
                    Frequency.VALID_EVERY_X_HOURS_INTERVALS
                ) {
                    return AppResult.Error(
                        MedicationError.InvalidInterval
                    )
                }
            }
        }

        return AppResult.Success(Unit)
    }

    private fun validateTimes(
        times: List<LocalTime>
    ): MedicationError? {

        if (times.isEmpty()) {
            return MedicationError.NoTimes
        }

        if (times.size != times.distinct().size) {
            return MedicationError.DuplicateTimes
        }

        return null
    }
}