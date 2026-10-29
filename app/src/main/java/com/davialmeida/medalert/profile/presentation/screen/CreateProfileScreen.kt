package com.davialmeida.medalert.profile.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.MedScreenScaffold
import com.davialmeida.medalert.profile.presentation.viewmodel.ProfileFormViewModel

@Composable
fun CreateProfileScreen(
    onBackClick: () -> Unit,
    onProfileCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.clearSaved()
            onProfileCreated()
        }
    }

    MedScreenScaffold(
        title = stringResource(R.string.profile_create_title),
        subtitle = stringResource(R.string.profile_form_subtitle),
        onBackClick = onBackClick,
        modifier = modifier
    ) { padding ->
        ProfileFormContent(
            uiState = uiState,
            buttonText = stringResource(R.string.profile_create_button),
            onNameChange = viewModel::onNameChange,
            onColorChange = viewModel::onColorChange,
            onSaveClick = viewModel::save,
            contentPadding = padding
        )
    }
}