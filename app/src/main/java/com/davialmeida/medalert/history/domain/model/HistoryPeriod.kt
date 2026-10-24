package com.davialmeida.medalert.history.domain.model

enum class HistoryPeriod(val days: Long) {
    LAST_7_DAYS(7),
    LAST_30_DAYS(30)
}