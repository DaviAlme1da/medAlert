package com.davialmeida.medalert.compose.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.component.DoseCard
import com.davialmeida.medalert.compose.component.DoseStatusUi
import com.davialmeida.medalert.compose.component.EmptyState
import com.davialmeida.medalert.compose.component.MedButton
import com.davialmeida.medalert.compose.component.MedButtonStyle
import com.davialmeida.medalert.compose.component.MedFilterChip
import com.davialmeida.medalert.compose.component.MedTextField
import com.davialmeida.medalert.compose.component.MedTimeField
import com.davialmeida.medalert.compose.component.MedicationCard
import com.davialmeida.medalert.compose.component.StatusBadge
import com.davialmeida.medalert.compose.component.WeekdayChip
import com.davialmeida.medalert.compose.theme.MedAlertTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComponentCatalogScreen(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ---------------------------------------------------------
            // BOTÕES
            // ---------------------------------------------------------

            CatalogSection(
                title = stringResource(R.string.catalog_buttons)
            ) {
                MedButton(
                    text = stringResource(R.string.catalog_primary),
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )

                MedButton(
                    text = stringResource(R.string.catalog_positive),
                    onClick = {},
                    style = MedButtonStyle.Positive,
                    modifier = Modifier.fillMaxWidth()
                )

                MedButton(
                    text = stringResource(R.string.catalog_outlined),
                    onClick = {},
                    style = MedButtonStyle.Outlined,
                    modifier = Modifier.fillMaxWidth()
                )

                MedButton(
                    text = stringResource(R.string.catalog_text),
                    onClick = {},
                    style = MedButtonStyle.Text,
                    modifier = Modifier.fillMaxWidth()
                )

                MedButton(
                    text = stringResource(R.string.catalog_loading),
                    onClick = {},
                    loading = true,
                    modifier = Modifier.fillMaxWidth()
                )

                MedButton(
                    text = stringResource(R.string.catalog_disabled),
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                MedButton(
                    text = stringResource(R.string.catalog_compact),
                    onClick = {},
                    style = MedButtonStyle.Positive,
                    compact = true
                )
            }

            // ---------------------------------------------------------
            // CAMPO DE TEXTO
            // ---------------------------------------------------------

            CatalogSection(
                title = stringResource(R.string.catalog_text_field)
            ) {
                MedTextField(
                    value = stringResource(R.string.catalog_example_medication),
                    onValueChange = {},
                    label = stringResource(R.string.catalog_name),
                    modifier = Modifier.fillMaxWidth()
                )

                MedTextField(
                    value = "",
                    onValueChange = {},
                    label = stringResource(R.string.catalog_name),
                    error = stringResource(R.string.catalog_required_field),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ---------------------------------------------------------
            // STATUS BADGE
            // ---------------------------------------------------------

            CatalogSection(
                title = stringResource(R.string.catalog_status_badges)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(status = DoseStatusUi.Pending)
                    StatusBadge(status = DoseStatusUi.Taken)
                    StatusBadge(status = DoseStatusUi.Skipped)
                    StatusBadge(status = DoseStatusUi.Late)
                }
            }

            // ---------------------------------------------------------
            // DOSE CARD
            // ---------------------------------------------------------

            CatalogSection(
                title = stringResource(R.string.catalog_dose_cards)
            ) {
                DoseCard(
                    name = stringResource(R.string.catalog_paracetamol),
                    dosage = stringResource(R.string.catalog_dosage_500_mg),
                    status = DoseStatusUi.Pending,
                    dotColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    onTakeClick = {}
                )

                DoseCard(
                    name = stringResource(R.string.catalog_dipyrone),
                    dosage = stringResource(R.string.catalog_dosage_1_g),
                    status = DoseStatusUi.Taken,
                    dotColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth()
                )

                DoseCard(
                    name = stringResource(R.string.catalog_ibuprofen),
                    dosage = stringResource(R.string.catalog_dosage_600_mg),
                    status = DoseStatusUi.Skipped,
                    dotColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth()
                )

                DoseCard(
                    name = stringResource(R.string.catalog_amoxicillin),
                    dosage = stringResource(R.string.catalog_dosage_500_mg),
                    status = DoseStatusUi.Late,
                    dotColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ---------------------------------------------------------
            // MEDICATION CARD
            // ---------------------------------------------------------

            CatalogSection(
                title = stringResource(R.string.catalog_medication_cards)
            ) {
                MedicationCard(
                    name = stringResource(R.string.catalog_paracetamol),
                    dosage = stringResource(R.string.catalog_dosage_500_mg),
                    frequencySummary = stringResource(R.string.catalog_every_8_hours),
                    nextDose = stringResource(R.string.catalog_time_1400),
                    dotColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )

                MedicationCard(
                    name = stringResource(R.string.catalog_dipyrone),
                    dosage = stringResource(R.string.catalog_dosage_1_g),
                    frequencySummary = stringResource(R.string.catalog_twice_daily),
                    nextDose = null,
                    dotColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ---------------------------------------------------------
            // CHIPS
            // ---------------------------------------------------------

            CatalogSection(
                title = stringResource(R.string.catalog_chips)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MedFilterChip(
                        label = stringResource(R.string.catalog_all),
                        selected = true,
                        onClick = {}
                    )

                    MedFilterChip(
                        label = stringResource(R.string.catalog_pending),
                        selected = false,
                        onClick = {}
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WeekdayChip(
                        label = stringResource(R.string.catalog_monday_letter),
                        contentDescription = stringResource(R.string.catalog_monday),
                        selected = true,
                        onClick = {}
                    )

                    WeekdayChip(
                        label = stringResource(R.string.catalog_tuesday_letter),
                        contentDescription = stringResource(R.string.catalog_tuesday),
                        selected = false,
                        onClick = {}
                    )

                    WeekdayChip(
                        label = stringResource(R.string.catalog_wednesday_letter),
                        contentDescription = stringResource(R.string.catalog_wednesday),
                        selected = false,
                        onClick = {}
                    )

                    WeekdayChip(
                        label = stringResource(R.string.catalog_thursday_letter),
                        contentDescription = stringResource(R.string.catalog_thursday),
                        selected = true,
                        onClick = {}
                    )
                }
            }

            // ---------------------------------------------------------
            // CAMPO DE HORÁRIO
            // ---------------------------------------------------------

            CatalogSection(
                title = stringResource(R.string.catalog_time_field)
            ) {
                MedTimeField(
                    value = stringResource(R.string.catalog_time_0800),
                    label = stringResource(R.string.time),
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )

                MedTimeField(
                    value = "",
                    label = stringResource(R.string.time),
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ---------------------------------------------------------
            // EMPTY STATE
            // ---------------------------------------------------------

            CatalogSection(
                title = stringResource(R.string.catalog_empty_state)
            ) {
                EmptyState(
                    title = stringResource(R.string.empty_medications_title),
                    message = stringResource(R.string.empty_medications_message),
                    actionText = stringResource(R.string.add_medication),
                    onActionClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun CatalogSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun ComponentCatalogScreenPreview() {
    MedAlertTheme {
        ComponentCatalogScreen()
    }
}