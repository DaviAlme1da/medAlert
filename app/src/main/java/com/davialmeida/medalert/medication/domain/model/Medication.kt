package com.davialmeida.medalert.medication.domain.model

import java.time.LocalDate

data class Medication(
    val id: Long,
    val profileId: Long,
    val name: String,
    val dosage: Dosage,
    val form: MedicationForm,
    val frequency: Frequency,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val color: MedicationColor,
    val notes: String,
    val remindersEnabled: Boolean
) {

    fun isActiveOn(date: LocalDate): Boolean {
        val hasStarted = date >= startDate
        val hasNotEnded = endDate == null || date <= endDate

        return hasStarted && hasNotEnded
    }
}