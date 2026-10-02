package com.davialmeida.medalert.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MedApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.MAIN.route,
        modifier = modifier
    ) {
        composable(Screen.MAIN.route) {
            MainScreen()
        }
    }
}