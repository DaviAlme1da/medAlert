package com.davialmeida.medalert.today.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.MedAlertTheme

@Composable
fun TodayScreen(modifier: Modifier = Modifier) {
    Text(text = stringResource(R.string.nav_today))
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenPreview() {
    MedAlertTheme {
        TodayScreen()
    }
}