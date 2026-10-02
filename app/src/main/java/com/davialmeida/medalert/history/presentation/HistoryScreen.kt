package com.davialmeida.medalert.history.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import com.davialmeida.medalert.R

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    Text(text = stringResource(R.string.nav_history))
}

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    MedAlertTheme {
        HistoryScreen()
    }
}