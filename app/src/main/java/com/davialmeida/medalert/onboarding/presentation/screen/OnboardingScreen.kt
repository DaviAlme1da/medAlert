package com.davialmeida.medalert.onboarding.presentation.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.MedButton
import com.davialmeida.medalert.compose.component.MedButtonStyle
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import com.davialmeida.medalert.reminder.presentation.component.ReminderPermissionRequester
import com.davialmeida.medalert.reminder.presentation.viewmodel.ReminderPermissionViewModel

private data class OnboardingPage(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)

private val onboardingPages = listOf(
    OnboardingPage(
        iconRes = R.drawable.ic_nav_today,
        titleRes = R.string.onboarding_welcome_title,
        descriptionRes = R.string.onboarding_welcome_description
    ),
    OnboardingPage(
        iconRes = R.drawable.ic_nav_history,
        titleRes = R.string.onboarding_how_title,
        descriptionRes = R.string.onboarding_how_description
    ),
    OnboardingPage(
        iconRes = R.drawable.ic_notification,
        titleRes = R.string.onboarding_permissions_title,
        descriptionRes = R.string.onboarding_permissions_description
    )
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    permissionViewModel: ReminderPermissionViewModel = hiltViewModel()
) {
    val permissionStatus by permissionViewModel.status.collectAsStateWithLifecycle()

    var currentPage by rememberSaveable { mutableStateOf(0) }
    val page = onboardingPages[currentPage]
    val isLastPage = currentPage == onboardingPages.lastIndex

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permissionViewModel.refresh()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PageIndicator(
                pageCount = onboardingPages.size,
                currentPage = currentPage
            )

            if (!isLastPage) {
                TextButton(onClick = onComplete) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(page.iconRes),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(page.titleRes),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(page.descriptionRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (isLastPage) {
                ReminderPermissionRequester(
                    status = permissionStatus,
                    onPermissionResult = permissionViewModel::refresh
                ) { requestPermission ->
                    if (permissionStatus.isComplete) {
                        Text(
                            text = stringResource(R.string.onboarding_permissions_done),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        MedButton(
                            text = stringResource(
                                if (!permissionStatus.notificationsAllowed) {
                                    R.string.reminder_permission_action_notifications
                                } else {
                                    R.string.reminder_permission_action_exact
                                }
                            ),
                            onClick = requestPermission,
                            style = MedButtonStyle.Outlined,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (currentPage > 0) {
                MedButton(
                    text = stringResource(R.string.onboarding_back),
                    onClick = { currentPage-- },
                    style = MedButtonStyle.Outlined,
                    modifier = Modifier.weight(1f)
                )
            }

            MedButton(
                text = stringResource(
                    if (isLastPage) R.string.onboarding_start else R.string.onboarding_next
                ),
                onClick = {
                    if (isLastPage) onComplete() else currentPage++
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    MedAlertTheme {
        OnboardingScreen(onComplete = {})
    }
}