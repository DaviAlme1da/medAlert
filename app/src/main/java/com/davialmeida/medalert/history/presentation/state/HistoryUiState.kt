package com.davialmeida.medalert.history.presentation.state

import com.davialmeida.medalert.core.domain.AppError
import com.davialmeida.medalert.history.domain.model.AdherenceSummary
import com.davialmeida.medalert.history.domain.model.HistoryDay
import com.davialmeida.medalert.history.domain.model.HistoryPeriod
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.profile.domain.model.Profile
import java.time.LocalDate

data class HistoryUiState(
    val period: HistoryPeriod = HistoryPeriod.LAST_7_DAYS,
    val selectedSeriesId: Long? = null,
    val medications: List<Medication> = emptyList(),
    val activeProfile: Profile? = null,
    val days: List<HistoryDay> = emptyList(),
    val summary: AdherenceSummary = AdherenceSummary(
        taken = 0,
        skipped = 0,
        late = 0
    ),
    val today: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true,
    val hasLoadError: Boolean = false,
    val actionError: AppError? = null
)