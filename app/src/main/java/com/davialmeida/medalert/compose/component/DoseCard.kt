package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.MedAlertTheme

@Composable
fun DoseCard(
    name: String,
    dosage: String,
    status: DoseStatusUi,
    dotColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onTakeClick: (() -> Unit)? = null,
) {
    val cardShape = MaterialTheme.shapes.large

    val cardModifier = if (status == DoseStatusUi.Late) {
        modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.error,
            shape = cardShape
        )
    } else {
        modifier
    }

    Card(
        onClick = onClick,
        modifier = cardModifier,
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MedicationDot(color = dotColor)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = dosage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (status == DoseStatusUi.Pending && onTakeClick != null) {
                MedButton(
                    text = stringResource(R.string.action_taken),
                    onClick = onTakeClick,
                    style = MedButtonStyle.Positive,
                    compact = true
                )
            } else {
                StatusBadge(status = status)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DoseCardPreview() {
    MedAlertTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DoseCard(
                name = "Paracetamol",
                dosage = "500 mg",
                status = DoseStatusUi.Pending,
                dotColor = MaterialTheme.colorScheme.primary,
                onTakeClick = {}
            )

            DoseCard(
                name = "Losartana",
                dosage = "50 mg",
                status = DoseStatusUi.Taken,
                dotColor = MaterialTheme.colorScheme.secondary
            )

            DoseCard(
                name = "Omeprazol",
                dosage = "20 mg",
                status = DoseStatusUi.Skipped,
                dotColor = MaterialTheme.colorScheme.tertiary
            )

            DoseCard(
                name = "Amoxicilina",
                dosage = "500 mg",
                status = DoseStatusUi.Late,
                dotColor = MaterialTheme.colorScheme.error
            )
        }
    }
}