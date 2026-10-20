package com.davialmeida.medalert.medication.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.MedScreenScaffold
import com.davialmeida.medalert.medication.presentation.viewmodel.CreateMedicationViewModel

@Composable
fun CreateMedicationScreen(
    onBackClick: () -> Unit,
    onMedicationCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateMedicationViewModel = hiltViewModel()
) {
    val state by viewModel.form.collectAsStateWithLifecycle()
    val actions = rememberMedicationFormActions(viewModel)

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onMedicationCreated()
        }
    }

    MedScreenScaffold(
        title = stringResource(R.string.medication_create_title),
        onBackClick = onBackClick,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            MedicationFormContent(
                state = state,
                actions = actions,
                saveLabel = stringResource(R.string.medication_save_create)
            )
        }
    }
}
