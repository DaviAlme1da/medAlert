package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.MedAlertTheme

enum class MedButtonStyle {
    Primary,
    Positive,
    Outlined,
    Text
}

private val ButtonShape = RoundedCornerShape(14.dp)

@Composable
fun MedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: MedButtonStyle = MedButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    compact: Boolean = false,
    icon: Painter? = null,
) {
    val isEnabled = enabled && !loading

    val commonModifier = modifier.heightIn(
        min = if (compact) 40.dp else 52.dp
    )

    val content: @Composable () -> Unit = {
        ButtonContent(
            text = text,
            loading = loading,
            icon = icon
        )
    }

    when (style) {
        MedButtonStyle.Primary -> {
            Button(
                onClick = onClick,
                modifier = commonModifier,
                enabled = isEnabled,
                shape = ButtonShape,
                content = { content() }
            )
        }

        MedButtonStyle.Positive -> {
            Button(
                onClick = onClick,
                modifier = commonModifier,
                enabled = isEnabled,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                content = { content() }
            )
        }

        MedButtonStyle.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = commonModifier,
                enabled = isEnabled,
                shape = ButtonShape,
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (isEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                ),
                content = { content() }
            )
        }

        MedButtonStyle.Text -> {
            TextButton(
                onClick = onClick,
                modifier = commonModifier,
                enabled = isEnabled,
                shape = ButtonShape,
                content = { content() }
            )
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    icon: Painter?,
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = LocalContentColor.current,
            strokeWidth = 2.dp
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
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
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                icon = painterResource(R.drawable.ic_notification)
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
                text = "Agora não",
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