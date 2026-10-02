package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun MedicationDot(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .size(12.dp)
            .background(
                color = color,
                shape = CircleShape
            )
    )
}