package com.davialmeida.medalert.core.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.davialmeida.medalert.compose.screen.ComponentCatalogScreen
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MedAlertTheme {
                ComponentCatalogScreen()
            }
        }
    }
}