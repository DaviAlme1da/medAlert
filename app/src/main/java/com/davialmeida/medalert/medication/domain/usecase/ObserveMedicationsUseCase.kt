package com.davialmeida.medalert.medication.domain.usecase

import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.medication.domain.rules.latestPerSeries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveMedicationsUseCase @Inject constructor(
    private val repository: MedicationRepository
) {

    operator fun invoke(profileId: Long): Flow<List<Medication>> {
        return repository.observeByProfile(profileId)
            .map { medications -> latestPerSeries(medications) }
    }
}
