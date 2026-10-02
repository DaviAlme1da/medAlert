package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.compose.theme.MedAlertTheme

enum class MedButtonStyle {
    Primary,
    Positive,
    Outlined,
    Text
}

@Composable
fun MedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: MedButtonStyle = MedButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    compact: Boolean = false,
) {
    val isEnabled = enabled && !loading

    val commonModifier = modifier.heightIn(
        min = if (compact) 40.dp else 48.dp
    )

    val shape = MaterialTheme.shapes.medium

    val content: @Composable () -> Unit = {
        ButtonContent(
            text = text,
            loading = loading,
            bold = style == MedButtonStyle.Positive
        )
    }

    when (style) {
        MedButtonStyle.Primary -> {
            Button(
                onClick = onClick,
                modifier = commonModifier,
                enabled = isEnabled,
                shape = shape,
                content = { content() }
            )
        }

        MedButtonStyle.Positive -> {
            Button(
                onClick = onClick,
                modifier = commonModifier,
                enabled = isEnabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                content = { content() }
            )
        }

        MedButtonStyle.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = commonModifier,
                enabled = isEnabled,
                shape = shape,
                content = { content() }
            )
        }

        MedButtonStyle.Text -> {
            TextButton(
                onClick = onClick,
                modifier = commonModifier,
                enabled = isEnabled,
                shape = shape,
                content = { content() }
            )
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    bold: Boolean,
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = LocalContentColor.current,
            strokeWidth = 2.dp
        )
    } else {
        Text(
            text = text,
            fontWeight = if (bold) FontWeight.Bold else null
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MedButtonPreview() {
    MedAlertTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MedButton(
                text = "Continuar",
                onClick = {}
            )

            MedButton(
                text = "Tomei",
                onClick = {},
                style = MedButtonStyle.Positive
            )

            MedButton(
                text = "Tomei",
                onClick = {},
                style = MedButtonStyle.Positive,
                compact = true
            )

            MedButton(
                text = "Cancelar",
                onClick = {},
                style = MedButtonStyle.Outlined
            )

            MedButton(
                text = "Saiba mais",
                onClick = {},
                style = MedButtonStyle.Text
            )

            MedButton(
                text = "Carregando",
                onClick = {},
                loading = true
            )

            MedButton(
                text = "Desabilitado",
                onClick = {},
                enabled = false
            )
        }
    }
}