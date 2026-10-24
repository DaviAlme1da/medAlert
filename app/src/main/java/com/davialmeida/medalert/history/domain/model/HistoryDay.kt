package com.davialmeida.medalert.history.domain.model

import com.davialmeida.medalert.today.domain.model.DoseItem
import java.time.LocalDate

data class HistoryDay(
    val date: LocalDate,
    val items: List<DoseItem>
)