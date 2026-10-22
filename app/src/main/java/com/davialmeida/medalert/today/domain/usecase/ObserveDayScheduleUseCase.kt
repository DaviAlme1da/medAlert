package com.davialmeida.medalert.today.domain.usecase

import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.today.domain.model.DaySchedule
import com.davialmeida.medalert.today.domain.repository.DoseLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class ObserveDayScheduleUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository
) {

    operator fun invoke(
        profileId: Long,
        date: LocalDate
    ): Flow<DaySchedule> {
        return combine(
            medicationRepository.observeActiveOn(profileId, date),
            doseLogRepository.observeByProfileAndDate(profileId, date)
        ) { medications, logs ->
            DaySchedule(
                medications = medications,
                logs = logs
            )
        }
    }
}