package com.davialmeida.medalert.history.domain.model

import java.time.LocalDate

data class HistoryResult(
    val days: List<HistoryDay>,
    val summary: AdherenceSummary,
    val today: LocalDate
)