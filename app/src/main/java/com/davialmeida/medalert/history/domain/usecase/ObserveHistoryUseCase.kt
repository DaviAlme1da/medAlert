package com.davialmeida.medalert.history.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.history.domain.model.AdherenceSummary
import com.davialmeida.medalert.history.domain.model.HistoryPeriod
import com.davialmeida.medalert.history.domain.model.HistoryResult
import com.davialmeida.medalert.history.domain.rules.buildHistory
import com.davialmeida.medalert.history.domain.rules.calculateAdherence
import com.davialmeida.medalert.history.domain.rules.filterBySeries
import com.davialmeida.medalert.medication.domain.repository.MedicationRepository
import com.davialmeida.medalert.profile.domain.repository.ProfileRepository
import com.davialmeida.medalert.today.domain.repository.DoseLogRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class ObserveHistoryUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val clock: Clock
) {

    operator fun invoke(
        period: HistoryPeriod = HistoryPeriod.LAST_7_DAYS,
        seriesId: Long? = null
    ): Flow<AppResult<HistoryResult>> =
        profileRepository.observeActiveProfile()
            .flatMapLatest { profile ->
                if (profile == null) {
                    flowOf(success(emptyHistory()))
                } else {
                    val rangeEnd = LocalDate.now(clock)
                    val rangeStart = rangeEnd.minusDays(period.days - 1)

                    combine(
                        medicationRepository.observeByProfile(profile.id),
                        doseLogRepository.observeByProfileInRange(
                            profileId = profile.id,
                            startDate = rangeStart,
                            endDate = rangeEnd
                        )
                    ) { medications, logs ->
                        val now = LocalDateTime.now(clock)
                        val today = now.toLocalDate()

                        val days = buildHistory(
                            medications = filterBySeries(medications, seriesId),
                            logs = logs,
                            period = period,
                            today = today,
                            now = now
                        )

                        success(
                            HistoryResult(
                                days = days,
                                summary = calculateAdherence(days),
                                today = today
                            )
                        )
                    }
                }
            }
            .catch { exception ->
                if (exception is CancellationException) throw exception

                emit(AppResult.Error(UnexpectedAppError))
            }

    private fun success(result: HistoryResult): AppResult<HistoryResult> =
        AppResult.Success(result)

    private fun emptyHistory() = HistoryResult(
        days = emptyList(),
        summary = AdherenceSummary(taken = 0, skipped = 0, late = 0),
        today = LocalDate.now(clock)
    )
}