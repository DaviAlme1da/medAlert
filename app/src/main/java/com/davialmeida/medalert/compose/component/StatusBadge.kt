package com.davialmeida.medalert.compose.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.MedAlertTheme
import com.davialmeida.medalert.compose.theme.statusColors

enum class DoseStatusUi {
    Pending,
    Taken,
    Skipped,
    Late
}

private data class StatusBadgeConfig(
    val containerColor: Color,
    val contentColor: Color,
    val labelRes: Int,
    val iconRes: Int?
)

@Composable
private fun getStatusBadgeConfig(
    status: DoseStatusUi
): StatusBadgeConfig {
    return when (status) {
        DoseStatusUi.Pending -> StatusBadgeConfig(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            labelRes = R.string.status_pending,
            iconRes = null
        )

        DoseStatusUi.Taken -> StatusBadgeConfig(
            containerColor = MaterialTheme.statusColors.successContainer,
            contentColor = MaterialTheme.statusColors.onSuccessContainer,
            labelRes = R.string.status_taken,
            iconRes = R.drawable.ic_check
        )

        DoseStatusUi.Skipped -> StatusBadgeConfig(
            containerColor = MaterialTheme.statusColors.warningContainer,
            contentColor = MaterialTheme.statusColors.onWarningContainer,
            labelRes = R.string.status_skipped,
            iconRes = R.drawable.ic_close
        )

        DoseStatusUi.Late -> StatusBadgeConfig(
            containerColor = MaterialTheme.statusColors.dangerContainer,
            contentColor = MaterialTheme.statusColors.onDangerContainer,
            labelRes = R.string.status_late,
            iconRes = R.drawable.ic_warning
        )
    }
}

@Composable
fun StatusBadge(
    status: DoseStatusUi,
    modifier: Modifier = Modifier,
) {
    val config = getStatusBadgeConfig(status)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = config.containerColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            config.iconRes?.let { iconRes ->
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = config.contentColor,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )
            }

            Text(
                text = stringResource(config.labelRes),
                color = config.contentColor,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusBadgePreview() {
    MedAlertTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusBadge(DoseStatusUi.Pending)
            StatusBadge(DoseStatusUi.Taken)
            StatusBadge(DoseStatusUi.Skipped)
            StatusBadge(DoseStatusUi.Late)
        }
    }
}