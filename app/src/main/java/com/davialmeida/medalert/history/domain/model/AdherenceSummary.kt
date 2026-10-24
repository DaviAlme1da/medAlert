package com.davialmeida.medalert.history.domain.model

import kotlin.math.roundToInt

data class AdherenceSummary(
    val taken: Int,
    val skipped: Int,
    val late: Int,
) {
    val resolved: Int
        get() = taken + skipped + late

    val percentage: Int?
        get() = if (resolved == 0) {
            null
        } else {
            (taken * 100.0 / resolved).roundToInt()
        }
}