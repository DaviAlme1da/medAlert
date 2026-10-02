package com.davialmeida.medalert.settings.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import com.davialmeida.medalert.R

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.nav_settings))
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MedAlertTheme {
        SettingsScreen()
    }
}