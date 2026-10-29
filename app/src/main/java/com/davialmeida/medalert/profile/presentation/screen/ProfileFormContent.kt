package com.davialmeida.medalert.profile.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.ColorOption
import com.davialmeida.medalert.compose.component.MedButton
import com.davialmeida.medalert.compose.component.MedTextField
import com.davialmeida.medalert.compose.component.ProfileAvatar
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import com.davialmeida.medalert.profile.presentation.extension.labelRes
import com.davialmeida.medalert.profile.presentation.extension.toComposeColor
import com.davialmeida.medalert.profile.presentation.state.ProfileFormUiState

@Composable
internal fun ProfileFormContent(
    uiState: ProfileFormUiState,
    buttonText: String,
    onNameChange: (String) -> Unit,
    onColorChange: (ProfileColor) -> Unit,
    onSaveClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val nameError = uiState.error?.takeIf { it.isNameError }
    val generalError = uiState.error?.takeIf { !it.isNameError }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileAvatar(
            name = uiState.name,
            color = uiState.selectedColor.toComposeColor(),
            size = 96.dp
        )

        MedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = stringResource(R.string.profile_name_label),
            error = nameError?.let { stringResource(it.messageRes) },
            helper = stringResource(R.string.profile_name_helper),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_color_label),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileColor.entries.forEach { color ->
                    ColorOption(
                        color = color.toComposeColor(),
                        name = stringResource(color.labelRes()),
                        selected = uiState.selectedColor == color,
                        onClick = { onColorChange(color) }
                    )
                }
            }
        }

        generalError?.let { error ->
            Text(
                text = stringResource(error.messageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        MedButton(
            text = buttonText,
            onClick = onSaveClick,
            loading = uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
            icon = painterResource(R.drawable.ic_med_check)
        )
    }
}