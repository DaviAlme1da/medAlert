package com.davialmeida.medalert.core.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.MedButton
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import com.davialmeida.medalert.medication.presentation.screen.CreateMedicationScreen
import com.davialmeida.medalert.medication.presentation.screen.EditMedicationScreen
import com.davialmeida.medalert.medication.presentation.screen.MedicationDetailScreen
import com.davialmeida.medalert.onboarding.presentation.screen.OnboardingScreen
import com.davialmeida.medalert.profile.presentation.screen.CreateProfileScreen
import com.davialmeida.medalert.profile.presentation.screen.EditProfileScreen
import com.davialmeida.medalert.profile.presentation.screen.ProfileScreen
import com.davialmeida.medalert.settings.domain.model.ThemePreference
import com.davialmeida.medalert.settings.presentation.viewmodel.ThemeViewModel

@Composable
fun MedApp(
    modifier: Modifier = Modifier,
    viewModel: StartupViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by themeViewModel.settings.collectAsStateWithLifecycle()

    val darkTheme = when (settings.theme) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    MedAlertTheme(darkTheme = darkTheme) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (uiState) {
                StartupUiState.Loading -> LoadingContent()

                StartupUiState.Onboarding -> {
                    OnboardingScreen(
                        onComplete = viewModel::completeOnboarding
                    )
                }

                StartupUiState.Error -> {
                    ErrorContent(onRetryClick = viewModel::retry)
                }

                StartupUiState.Ready -> AppNavHost()
            }
        }
    }
}

@Composable
private fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.MAIN.route
    ) {
        composable(Screen.MAIN.route) {
            MainScreen(
                onOpenProfiles = {
                    navController.navigate(Screen.PROFILES.route)
                },
                onAddMedication = {
                    navController.navigate(Screen.CREATE_MEDICATION.route)
                },
                onOpenMedication = { seriesId ->
                    navController.navigate(medicationDetailRoute(seriesId))
                }
            )
        }
        composable(
            route = Screen.MEDICATION_DETAIL.route,
            arguments = listOf(
                navArgument(MEDICATION_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) {
            MedicationDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onEditClick = { seriesId ->
                    navController.navigate(editMedicationRoute(seriesId))
                },
                onDeleted = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EDIT_MEDICATION.route,
            arguments = listOf(
                navArgument(MEDICATION_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) {
            EditMedicationScreen(
                onBackClick = { navController.popBackStack() },
                onMedicationUpdated = { navController.popBackStack() }
            )
        }

        composable(Screen.CREATE_MEDICATION.route) {
            CreateMedicationScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onMedicationCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PROFILES.route) {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAddProfileClick = {
                    navController.navigate(Screen.CREATE_PROFILE.route)
                },
                onEditProfileClick = { profileId ->
                    navController.navigate(editProfileRoute(profileId))
                }
            )
        }

        composable(Screen.CREATE_PROFILE.route) {
            CreateProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onProfileCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EDIT_PROFILE.route,
            arguments = listOf(
                navArgument(PROFILE_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) {
            EditProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onProfileUpdated = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(
            16.dp,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.startup_error),
            style = MaterialTheme.typography.bodyLarge
        )

        MedButton(
            text = stringResource(R.string.retry),
            onClick = onRetryClick
        )
    }
}
