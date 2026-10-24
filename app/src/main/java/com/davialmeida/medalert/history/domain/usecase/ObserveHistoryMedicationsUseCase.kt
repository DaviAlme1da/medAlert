package com.davialmeida.medalert.history.domain.usecase

import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.medication.domain.rules.latestPerSeries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveHistoryMedicationsUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository
) {

    operator fun invoke(profileId: Long): Flow<List<Medication>> {
        return medicationRepository
            .observeByProfile(profileId)
            .map { latestPerSeries(it) }
    }
}