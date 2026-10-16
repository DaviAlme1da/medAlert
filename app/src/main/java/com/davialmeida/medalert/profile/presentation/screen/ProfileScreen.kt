package com.davialmeida.medalert.profile.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.EmptyState
import com.davialmeida.medalert.compose.component.MedAlertDialog
import com.davialmeida.medalert.compose.component.MedButton
import com.davialmeida.medalert.compose.component.MedScreenScaffold
import com.davialmeida.medalert.compose.component.ProfileAvatar
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.presentation.extension.toComposeColor
import com.davialmeida.medalert.profile.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onAddProfileClick: () -> Unit,
    onEditProfileClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var profileToDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val profileToDelete = uiState.profiles.firstOrNull { it.id == profileToDeleteId }

    MedScreenScaffold(
        title = stringResource(R.string.profile_screen_title),
        onBackClick = onBackClick,
        modifier = modifier
    ) { padding ->
        ProfileListContent(
            profiles = uiState.profiles,
            activeProfileId = uiState.activeProfile?.id,
            isLoading = uiState.isLoading,
            errorMessage = uiState.error?.messageRes,
            onProfileClick = viewModel::setActiveProfile,
            onEditProfileClick = onEditProfileClick,
            onDeleteProfileClick = { profile -> profileToDeleteId = profile.id },
            onAddProfileClick = onAddProfileClick,
            onErrorDismiss = viewModel::clearError,
            contentPadding = padding
        )
    }

    profileToDelete?.let { profile ->
        MedAlertDialog(
            title = stringResource(R.string.profile_delete_title),
            message = stringResource(R.string.profile_delete_message, profile.name),
            confirmText = stringResource(R.string.profile_delete_confirm),
            dismissText = stringResource(R.string.profile_delete_cancel),
            destructive = true,
            onConfirm = {
                viewModel.deleteProfile(profile.id)
                profileToDeleteId = null
            },
            onDismiss = { profileToDeleteId = null }
        )
    }
}

@Composable
private fun ProfileListContent(
    profiles: List<Profile>,
    activeProfileId: Long?,
    isLoading: Boolean,
    errorMessage: Int?,
    onProfileClick: (Long) -> Unit,
    onEditProfileClick: (Long) -> Unit,
    onDeleteProfileClick: (Profile) -> Unit,
    onAddProfileClick: () -> Unit,
    onErrorDismiss: () -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
    ) {
        errorMessage?.let { messageRes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(messageRes),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )

                TextButton(onClick = onErrorDismiss) {
                    Text(text = stringResource(R.string.profile_error_dismiss))
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()

                profiles.isEmpty() -> EmptyState(
                    title = stringResource(R.string.profile_empty_title),
                    message = stringResource(R.string.profile_empty_message)
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = profiles,
                        key = { profile -> profile.id }
                    ) { profile ->
                        ProfileCard(
                            profile = profile,
                            isActive = profile.id == activeProfileId,
                            onClick = { onProfileClick(profile.id) },
                            onEditClick = { onEditProfileClick(profile.id) },
                            onDeleteClick = { onDeleteProfileClick(profile) }
                        )
                    }
                }
            }
        }

        MedButton(
            text = stringResource(R.string.profile_add_button),
            onClick = onAddProfileClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
    }
}

@Composable
private fun ProfileCard(
    profile: Profile,
    isActive: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val editDescription = stringResource(R.string.profile_edit_description, profile.name)
    val deleteDescription = stringResource(R.string.profile_delete_description, profile.name)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (isActive) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileAvatar(
                    name = profile.name,
                    color = profile.color.toComposeColor()
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isActive) {
                        Text(
                            text = stringResource(R.string.profile_active),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onEditClick,
                    modifier = Modifier.semantics {
                        contentDescription = editDescription
                    }
                ) {
                    Text(text = stringResource(R.string.profile_edit_button))
                }

                TextButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.semantics {
                        contentDescription = deleteDescription
                    }
                ) {
                    Text(
                        text = stringResource(R.string.profile_delete_button),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}