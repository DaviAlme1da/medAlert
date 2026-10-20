package com.davialmeida.medalert.medication.presentation.viewmodel

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.medication.domain.model.Dosage
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.model.MedicationColor
import com.davialmeida.medalert.medication.domain.model.MedicationForm
import com.davialmeida.medalert.medication.domain.usecase.CreateMedicationUseCase
import com.davialmeida.medalert.medication.presentation.state.MedicationFormUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CreateMedicationViewModel @Inject constructor(
    private val createMedicationUseCase: CreateMedicationUseCase,
    clock: Clock
) : MedicationFormViewModel(
    MedicationFormUiState(startDate = LocalDate.now(clock))
) {

    // Todos os campos são sobrescritos pelo formulário; id/profile
    // zerados são trocados no use case (perfil ativo).
    private val blankMedication = Medication(
        id = 0L,
        seriesId = 0L,
        profileId = 0L,
        name = "",
        dosage = Dosage(BigDecimal.ZERO, DosageUnit.MG),
        form = MedicationForm.TABLET,
        frequency = Frequency.Daily(emptyList()),
        startDate = LocalDate.EPOCH,
        endDate = null,
        color = MedicationColor.BLUE,
        notes = "",
        remindersEnabled = true
    )

    override fun medicationTemplate(): Medication = blankMedication

    override suspend fun persist(medication: Medication): AppResult<*> {
        return createMedicationUseCase(medication)
    }
}
