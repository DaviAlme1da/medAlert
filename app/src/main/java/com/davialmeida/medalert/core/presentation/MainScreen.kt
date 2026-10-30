package com.davialmeida.medalert.core.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.davialmeida.medalert.R
import com.davialmeida.medalert.history.presentation.screen.HistoryScreen
import com.davialmeida.medalert.medication.presentation.screen.MedicationsScreen
import com.davialmeida.medalert.settings.presentation.screen.SettingsScreen
import com.davialmeida.medalert.today.presentation.screen.TodayScreen

private val bottomBarItems = listOf(
    BottomBarItem(
        screen = Screen.TODAY,
        labelRes = R.string.nav_today,
        iconRes = R.drawable.ic_nav_today,
    ),
    BottomBarItem(
        screen = Screen.MEDICATIONS,
        labelRes = R.string.nav_medications,
        iconRes = R.drawable.ic_nav_medications,
    ),
    BottomBarItem(
        screen = Screen.HISTORY,
        labelRes = R.string.nav_history,
        iconRes = R.drawable.ic_nav_history,
    ),
    BottomBarItem(
        screen = Screen.SETTINGS,
        labelRes = R.string.nav_settings,
        iconRes = R.drawable.ic_nav_settings,
    ),
)

@Composable
fun MainScreen(
    onOpenProfiles: () -> Unit,
    onAddMedication: () -> Unit = {},
    onOpenMedication: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                bottomBarItems.forEach { item ->
                    val selected = currentDestination
                        ?.hierarchy
                        ?.any { destination ->
                            destination.route == item.screen.route
                        } == true

                    NavigationBarItem(
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            navController.navigate(item.screen.route) {
                                launchSingleTop = true
                                popUpTo(
                                    navController.graph.findStartDestination().id
                                ) {
                                    saveState = true
                                }
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(item.iconRes),
                                contentDescription = null,
                            )
                        },
                        label = {
                            Text(text = stringResource(item.labelRes))
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(

            navController = navController,
            startDestination = Screen.TODAY.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.TODAY.route) {
                TodayScreen(
                    onOpenProfiles = onOpenProfiles,
                    onAddMedication = onAddMedication,
                )
            }

            composable(Screen.MEDICATIONS.route) {
                MedicationsScreen(
                    onAddMedication = onAddMedication,
                    onOpenMedication = onOpenMedication,
                )
            }

            composable(Screen.HISTORY.route) {
                HistoryScreen()
            }

            composable(Screen.SETTINGS.route) {
                SettingsScreen(
                    onProfilesClick = onOpenProfiles
                )
            }
        }
    }
}

private data class BottomBarItem(
    val screen: Screen,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
)