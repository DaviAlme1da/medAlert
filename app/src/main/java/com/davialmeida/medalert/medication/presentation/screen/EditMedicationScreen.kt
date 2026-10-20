package com.davialmeida.medalert.medication.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.MedScreenScaffold
import com.davialmeida.medalert.medication.presentation.extension.toMessageRes
import com.davialmeida.medalert.medication.presentation.state.EditMedicationLoadState
import com.davialmeida.medalert.medication.presentation.viewmodel.EditMedicationViewModel

@Composable
fun EditMedicationScreen(
    onBackClick: () -> Unit,
    onMedicationUpdated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditMedicationViewModel = hiltViewModel()
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val loadState by viewModel.loadState.collectAsStateWithLifecycle()
    val actions = rememberMedicationFormActions(viewModel)

    LaunchedEffect(form.isSaved) {
        if (form.isSaved) {
            onMedicationUpdated()
        }
    }

    MedScreenScaffold(
        title = stringResource(R.string.medication_edit_title),
        onBackClick = onBackClick,
        modifier = modifier
    ) { innerPadding ->
        when (val current = loadState) {
            EditMedicationLoadState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is EditMedicationLoadState.Failed -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(current.error.toMessageRes()),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    TextButton(onClick = viewModel::retry) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }

            EditMedicationLoadState.Ready -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    MedicationFormContent(
                        state = form,
                        actions = actions,
                        saveLabel = stringResource(R.string.medication_save_edit),
                        startDateEditable = false
                    )
                }
            }
        }
    }
}
