package com.davialmeida.medalert.medication.domain.usecase

import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class ObserveActiveMedicationsUseCase @Inject constructor(
    private val repository: MedicationRepository
) {

    operator fun invoke(
        profileId: Long,
        date: LocalDate
    ): Flow<List<Medication>> {
        return repository.observeActiveOn(
            profileId = profileId,
            date = date
        )
    }
}
