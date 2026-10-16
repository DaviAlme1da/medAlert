package com.davialmeida.medalert.profile.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.MedScreenScaffold
import com.davialmeida.medalert.profile.presentation.viewmodel.ProfileFormViewModel

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onProfileUpdated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.clearSaved()
            onProfileUpdated()
        }
    }

    MedScreenScaffold(
        title = stringResource(R.string.profile_edit_title),
        onBackClick = onBackClick,
        modifier = modifier
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.profileNotFound -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.profile_not_found),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                ProfileFormContent(
                    uiState = uiState,
                    buttonText = stringResource(R.string.profile_save_button),
                    onNameChange = viewModel::onNameChange,
                    onColorChange = viewModel::onColorChange,
                    onSaveClick = viewModel::save,
                    contentPadding = padding
                )
            }
        }
    }
}