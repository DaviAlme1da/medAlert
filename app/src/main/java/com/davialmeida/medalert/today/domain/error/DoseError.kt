package com.davialmeida.medalert.today.domain.error

import com.davialmeida.medalert.core.domain.AppError

sealed interface DoseError : AppError {

    data object FutureDose : DoseError

    data object DoseNotFound : DoseError

    data object MedicationNotFound : DoseError
}
