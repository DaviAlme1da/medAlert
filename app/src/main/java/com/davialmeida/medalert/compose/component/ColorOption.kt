package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import com.davialmeida.medalert.compose.theme.ProfileBlue
import com.davialmeida.medalert.compose.theme.ProfileGreen
import com.davialmeida.medalert.compose.theme.ProfilePurple

@Composable
fun ColorOption(
    color: Color,
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedBorder = if (selected) {
        Modifier.border(
            width = 3.dp,
            color = MaterialTheme.colorScheme.onSurface,
            shape = CircleShape
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(color)
            .then(selectedBorder)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .semantics {
                contentDescription = name
            },
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorOptionPreview() {
    MedAlertTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ColorOption(color = ProfileBlue, name = "Azul", selected = true, onClick = {})
            ColorOption(color = ProfileGreen, name = "Verde", selected = false, onClick = {})
            ColorOption(color = ProfilePurple, name = "Roxo", selected = false, onClick = {})
        }
    }
}