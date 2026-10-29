package com.davialmeida.medalert.profile.presentation.screen

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.EmptyState
import com.davialmeida.medalert.compose.component.MedAlertDialog
import com.davialmeida.medalert.compose.component.MedButton
import com.davialmeida.medalert.compose.component.MedCard
import com.davialmeida.medalert.compose.component.MedScreenScaffold
import com.davialmeida.medalert.compose.component.MedStatusChip
import com.davialmeida.medalert.compose.component.MedStatusKind
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
        subtitle = stringResource(R.string.profile_screen_subtitle),
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
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        errorMessage?.let { messageRes ->
            item {
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
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.profile_list_headline),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = stringResource(R.string.profile_list_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            MedButton(
                text = stringResource(R.string.profile_add_button),
                onClick = onAddProfileClick,
                modifier = Modifier.fillMaxWidth(),
                icon = painterResource(R.drawable.ic_med_add)
            )
        }

        if (profiles.isEmpty()) {
            item {
                EmptyState(
                    title = stringResource(R.string.profile_empty_title),
                    message = stringResource(R.string.profile_empty_message),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.profile_list_section),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = pluralStringResource(
                            R.plurals.profile_count,
                            profiles.size,
                            profiles.size
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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

    MedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = if (isActive) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        borderColor = if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 8.dp,
            end = 4.dp,
            bottom = 8.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileAvatar(
                name = profile.name,
                color = profile.color.toComposeColor(),
                size = 48.dp
            )

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = profile.name,
                    modifier = Modifier.weight(1f, fill = false),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isActive) {
                    MedStatusChip(
                        text = stringResource(R.string.profile_active_short),
                        kind = MedStatusKind.Success
                    )
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_med_edit),
                    contentDescription = editDescription,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_med_delete),
                    contentDescription = deleteDescription,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}