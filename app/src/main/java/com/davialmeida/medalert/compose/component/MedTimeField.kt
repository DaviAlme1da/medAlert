package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MedTimeField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val currentOnClick by rememberUpdatedState(onClick)

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            if (interaction is PressInteraction.Release) {
                currentOnClick()
            }
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = modifier,
        label = {
            Text(text = label)
        },
        readOnly = true,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        interactionSource = interactionSource,
        trailingIcon = {
            IconButton(
                onClick = onClick
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_clock
                    ),
                    contentDescription = stringResource(
                        R.string.select_time
                    )
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun MedTimeFieldPreview() {
    MedAlertTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            MedTimeField(
                value = "08:00",
                label = stringResource(R.string.time),
                onClick = {}
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            MedTimeField(
                value = "",
                label = stringResource(R.string.time),
                onClick = {}
            )
        }
    }
}