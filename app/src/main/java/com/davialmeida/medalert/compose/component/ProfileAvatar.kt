package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import com.davialmeida.medalert.compose.theme.ProfileGreen

private fun initialsOf(name: String): String =
    name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .mapNotNull { it.firstOrNull() }
        .joinToString("")
        .uppercase()

@Composable
fun ProfileAvatar(
    name: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initialsOf(name),
            style = if (size >= 40.dp) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.titleSmall
            },
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileAvatarPreview() {
    MedAlertTheme {
        ProfileAvatar(
            name = "Ana Souza",
            color = ProfileGreen,
            modifier = Modifier.padding(16.dp)
        )
    }
}