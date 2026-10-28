package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import com.davialmeida.medalert.compose.theme.statusColors

enum class MedStatusKind {
    Success,
    Warning,
    Danger,
    Neutral
}

@Composable
fun MedStatusChip(
    text: String,
    kind: MedStatusKind,
    modifier: Modifier = Modifier,
) {
    val status = MaterialTheme.statusColors
    val scheme = MaterialTheme.colorScheme

    val container = when (kind) {
        MedStatusKind.Success -> status.successContainer
        MedStatusKind.Warning -> status.warningContainer
        MedStatusKind.Danger -> status.dangerContainer
        MedStatusKind.Neutral -> scheme.primaryContainer
    }

    val content = when (kind) {
        MedStatusKind.Success -> status.onSuccessContainer
        MedStatusKind.Warning -> status.onWarningContainer
        MedStatusKind.Danger -> status.onDangerContainer
        MedStatusKind.Neutral -> scheme.onPrimaryContainer
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = container,
        contentColor = content
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MedStatusChipPreview() {
    MedAlertTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MedStatusChip("Tomada", MedStatusKind.Success)
            MedStatusChip("Pulada", MedStatusKind.Warning)
            MedStatusChip("Atrasada", MedStatusKind.Danger)
            MedStatusChip("Próxima", MedStatusKind.Neutral)
        }
    }
}