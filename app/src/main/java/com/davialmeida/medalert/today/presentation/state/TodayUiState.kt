package com.davialmeida.medalert.today.presentation.state

import com.davialmeida.medalert.core.domain.AppError
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.today.domain.model.DayProgress
import com.davialmeida.medalert.today.presentation.model.DoseItemUi
import java.time.LocalDate

data class TodayUiState(
    val selectedDate: LocalDate,
    val today: LocalDate,
    val weekDays: List<LocalDate>,
    val isLoading: Boolean = true,
    val hasLoadError: Boolean = false,
    val actionError: AppError? = null,
    val profiles: List<Profile> = emptyList(),
    val activeProfile: Profile? = null,
    val items: List<DoseItemUi> = emptyList(),
    val progress: DayProgress = DayProgress(taken = 0, total = 0),
)