package com.davialmeida.medalert.medication.presentation.extension

import androidx.compose.runtime.Composable
import com.davialmeida.medalert.medication.domain.model.Dosage

@Composable
fun Dosage.toDisplayText(): String {
    val amountText = amount
        .stripTrailingZeros()
        .toPlainString()
        .replace('.', ',')

    return "$amountText ${unit.label(amount)}"
}
