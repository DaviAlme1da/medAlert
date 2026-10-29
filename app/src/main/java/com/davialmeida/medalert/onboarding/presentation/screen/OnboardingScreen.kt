package com.davialmeida.medalert.onboarding.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
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
import com.davialmeida.medalert.compose.component.MedCard
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import com.davialmeida.medalert.compose.theme.statusColors
import com.davialmeida.medalert.reminder.presentation.component.ReminderPermissionRequester
import com.davialmeida.medalert.reminder.presentation.viewmodel.ReminderPermissionViewModel

private data class OnboardingPage(
    @DrawableRes val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)

private val onboardingPages = listOf(
    OnboardingPage(
        imageRes = R.drawable.img_onboarding_welcome,
        titleRes = R.string.onboarding_welcome_title,
        descriptionRes = R.string.onboarding_welcome_description
    ),
    OnboardingPage(
        imageRes = R.drawable.img_onboarding_routine,
        titleRes = R.string.onboarding_how_title,
        descriptionRes = R.string.onboarding_how_description
    ),
    OnboardingPage(
        imageRes = R.drawable.img_onboarding_permissions,
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

    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    val page = onboardingPages[currentPage]
    val isLastPage = currentPage == onboardingPages.lastIndex

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permissionViewModel.refresh()
    }

    // Sem o botão "Voltar" do layout, o gesto/botão de voltar do sistema
    // volta uma página em vez de fechar o app.
    BackHandler(enabled = currentPage > 0) {
        currentPage--
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnboardingHeader(onSkip = onComplete)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImageCard(imageRes = page.imageRes)

            Text(
                text = stringResource(page.titleRes),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = stringResource(page.descriptionRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            if (isLastPage) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PermissionItem(
                        iconRes = R.drawable.ic_notification,
                        titleRes = R.string.settings_notifications,
                        descriptionRes = R.string.onboarding_permission_notifications_desc,
                        allowed = permissionStatus.notificationsAllowed
                    )

                    PermissionItem(
                        iconRes = R.drawable.ic_med_alarm,
                        titleRes = R.string.settings_exact_alarms,
                        descriptionRes = R.string.onboarding_permission_exact_desc,
                        allowed = permissionStatus.exactAlarmsAllowed
                    )
                }
            }
        }

        PageIndicator(
            pageCount = onboardingPages.size,
            currentPage = currentPage,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (isLastPage) {
            ReminderPermissionRequester(
                status = permissionStatus,
                onPermissionResult = permissionViewModel::refresh
            ) { requestPermission ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (permissionStatus.isComplete) {
                        MedButton(
                            text = stringResource(R.string.onboarding_start),
                            onClick = onComplete,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        MedButton(
                            text = stringResource(R.string.onboarding_open_settings),
                            onClick = requestPermission,
                            icon = painterResource(R.drawable.ic_notification),
                            modifier = Modifier.fillMaxWidth()
                        )

                        MedButton(
                            text = stringResource(R.string.onboarding_not_now),
                            onClick = onComplete,
                            style = MedButtonStyle.Text,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            MedButton(
                text = stringResource(R.string.onboarding_next),
                onClick = { currentPage++ },
                icon = painterResource(R.drawable.ic_med_chevron_right),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun OnboardingHeader(onSkip: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_notification),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        MedButton(
            text = stringResource(R.string.onboarding_skip),
            onClick = onSkip,
            style = MedButtonStyle.Text,
            compact = true
        )
    }
}

/** Quadro grande com a ilustração de cada página. */
@Composable
private fun ImageCard(@DrawableRes imageRes: Int) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Box(
        modifier = Modifier
            .fillMaxWidth(0.86f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (isDark) Color(0xFFD6DEE2) else MaterialTheme.colorScheme.surface
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        )
    }
}

@Composable
private fun PermissionItem(
    @DrawableRes iconRes: Int,
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    allowed: Boolean
) {
    MedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (allowed) {
                Icon(
                    painter = painterResource(R.drawable.ic_med_check),
                    contentDescription = null,
                    tint = MaterialTheme.statusColors.success,
                    modifier = Modifier.size(20.dp)
                )
            }
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
            val active = index == currentPage

            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (active) 24.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) {
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