package com.davialmeida.medalert.today.domain.model

import com.davialmeida.medalert.medication.domain.model.Medication

data class DaySchedule(
    val medications: List<Medication>,
    val logs: List<DoseLog>
)
