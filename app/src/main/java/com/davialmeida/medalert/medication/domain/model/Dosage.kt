package com.davialmeida.medalert.medication.domain.model

import java.math.BigDecimal

data class Dosage(
    val amount: BigDecimal,
    val unit: DosageUnit
)