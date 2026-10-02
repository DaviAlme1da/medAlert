package com.davialmeida.medalert.core.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import com.davialmeida.medalert.history.presentation.HistoryScreen
import com.davialmeida.medalert.medication.presentation.MedicationsScreen
import com.davialmeida.medalert.settings.presentation.SettingsScreen
import com.davialmeida.medalert.today.presentation.TodayScreen

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
                        onClick = {
                            navController.navigate(item.screen.route) {
                                launchSingleTop = true

                                popUpTo(navController.graph.findStartDestination().id) {
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
                            Text(
                                text = stringResource(item.labelRes),
                            )
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
                TodayScreen()
            }

            composable(Screen.MEDICATIONS.route) {
                MedicationsScreen()
            }

            composable(Screen.HISTORY.route) {
                HistoryScreen()
            }

            composable(Screen.SETTINGS.route) {
                SettingsScreen()
            }
        }
    }
}

private data class BottomBarItem(
    val screen: Screen,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
)

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MedAlertTheme {
        MainScreen()
    }
}