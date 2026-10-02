package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.compose.theme.MedAlertTheme

@Composable
fun MedFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(text = label)
        },
        modifier = modifier,
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun WeekdayChip(
    label: String,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .semantics {
                this.contentDescription = contentDescription
            }
            .toggleable(
                value = selected,
                onValueChange = {
                    onClick()
                },
                role = Role.Checkbox
            ),
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        border = if (!selected) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            null
        }
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.clearAndSetSemantics {},
                text = label,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChipsPreview() {
    MedAlertTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedFilterChip(
                    label = "Todos",
                    selected = true,
                    onClick = {}
                )

                MedFilterChip(
                    label = "Pendentes",
                    selected = false,
                    onClick = {}
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeekdayChip(
                    label = "S",
                    contentDescription = "Segunda-feira",
                    selected = true,
                    onClick = {}
                )

                WeekdayChip(
                    label = "T",
                    contentDescription = "Terça-feira",
                    selected = false,
                    onClick = {}
                )

                WeekdayChip(
                    label = "Q",
                    contentDescription = "Quarta-feira",
                    selected = false,
                    onClick = {}
                )

                WeekdayChip(
                    label = "Q",
                    contentDescription = "Quinta-feira",
                    selected = true,
                    onClick = {}
                )
            }
        }
    }
}