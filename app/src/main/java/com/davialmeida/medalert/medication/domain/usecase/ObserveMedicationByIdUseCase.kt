package com.davialmeida.medalert.medication.domain.usecase

import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMedicationByIdUseCase @Inject constructor(
    private val repository: MedicationRepository
) {

    operator fun invoke(
        medicationId: Long
    ): Flow<Medication?> {
        return repository.observeById(medicationId)
    }
}
