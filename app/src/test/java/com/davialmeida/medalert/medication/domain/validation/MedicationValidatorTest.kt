
package com.davialmeida.medalert.medication.domain.validation

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.medication.domain.error.MedicationError
import com.davialmeida.medalert.medication.domain.model.Dosage
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.medication.domain.model.Frequency
import com.davialmeida.medalert.medication.domain.model.Medication
import com.davialmeida.medalert.medication.domain.model.MedicationColor
import com.davialmeida.medalert.medication.domain.model.MedicationForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class MedicationValidatorTest {

    private val today = LocalDate.of(2026, 10, 16)

    private fun validMedication(): Medication = Medication(
        id = 1L,
        seriesId = 1L,
        profileId = 1L,
        name = "Paracetamol",
        dosage = Dosage(
            amount = BigDecimal("500"),
            unit = DosageUnit.MG
        ),
        form = MedicationForm.TABLET,
        frequency = Frequency.Daily(
            times = listOf(LocalTime.of(8, 0))
        ),
        startDate = today,
        endDate = null,
        color = MedicationColor.BLUE,
        notes = "",
        remindersEnabled = true
    )

    @Test
    fun `valid medication returns no errors`() {
        val errors = MedicationValidator.validateAll(validMedication())

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `blank name returns EmptyName`() {
        val medication = validMedication().copy(name = "   ")

        val errors = MedicationValidator.validateAll(medication)

        assertTrue(MedicationError.EmptyName in errors)
    }

    @Test
    fun `name exceeding maximum length returns NameTooLong`() {
        val medication = validMedication().copy(
            name = "A".repeat(MEDICATION_NAME_MAX_LENGTH + 1)
        )

        val errors = MedicationValidator.validateAll(medication)

        assertTrue(MedicationError.NameTooLong in errors)
    }

    @Test
    fun `name at maximum length is valid`() {
        val medication = validMedication().copy(
            name = "A".repeat(MEDICATION_NAME_MAX_LENGTH)
        )

        val errors = MedicationValidator.validateAll(medication)

        assertFalse(MedicationError.NameTooLong in errors)
    }

    @Test
    fun `zero dosage returns InvalidDosageAmount`() {
        val medication = validMedication().copy(
            dosage = validMedication().dosage.copy(
                amount = BigDecimal.ZERO
            )
        )

        val errors = MedicationValidator.validateAll(medication)

        assertTrue(MedicationError.InvalidDosageAmount in errors)
    }

    @Test
    fun `negative dosage returns InvalidDosageAmount`() {
        val medication = validMedication().copy(
            dosage = validMedication().dosage.copy(
                amount = BigDecimal("-1")
            )
        )

        val errors = MedicationValidator.validateAll(medication)

        assertTrue(MedicationError.InvalidDosageAmount in errors)
    }

    @Test
    fun `end date before start date returns EndDateBeforeStartDate`() {
        val medication = validMedication().copy(
            endDate = today.minusDays(1)
        )

        val errors = MedicationValidator.validateAll(medication)

        assertTrue(MedicationError.EndDateBeforeStartDate in errors)
    }

    @Test
    fun `end date equal to start date is valid`() {
        val medication = validMedication().copy(
            endDate = today
        )

        val errors = MedicationValidator.validateAll(medication)

        assertFalse(MedicationError.EndDateBeforeStartDate in errors)
    }

    @Test
    fun `daily frequency without times returns NoTimes`() {
        val medication = validMedication().copy(
            frequency = Frequency.Daily(times = emptyList())
        )

        val errors = MedicationValidator.validateAll(medication)

        assertTrue(MedicationError.NoTimes in errors)
    }

    @Test
    fun `daily frequency with duplicate times returns DuplicateTimes`() {
        val time = LocalTime.of(8, 0)
        val medication = validMedication().copy(
            frequency = Frequency.Daily(times = listOf(time, time))
        )

        val errors = MedicationValidator.validateAll(medication)

        assertTrue(MedicationError.DuplicateTimes in errors)
    }

    @Test
    fun `specific days without times returns NoTimes`() {
        val medication = validMedication().copy(
            frequency = Frequency.SpecificDays(
                daysOfWeek = setOf(DayOfWeek.MONDAY),
                times = emptyList()
            )
        )

        val errors = MedicationValidator.validateAll(medication)

        assertTrue(MedicationError.NoTimes in errors)
    }

    @Test
    fun `specific days with duplicate times returns DuplicateTimes`() {
        val time = LocalTime.of(8, 0)
        val medication = validMedication().copy(
            frequency = Frequency.SpecificDays(
                daysOfWeek = setOf(DayOfWeek.MONDAY),
                times = listOf(time, time)
            )
        )

        val errors = MedicationValidator.validateAll(medication)

        assertTrue(MedicationError.DuplicateTimes in errors)
    }

    @Test
    fun `specific days without selected days returns NoDaysSelected`() {
        val medication = validMedication().copy(
            frequency = Frequency.SpecificDays(
                daysOfWeek = emptySet(),
                times = listOf(LocalTime.of(8, 0))
            )
        )

        val errors = MedicationValidator.validateAll(medication)

        assertTrue(MedicationError.NoDaysSelected in errors)
    }

    @Test
    fun `valid specific days frequency returns no errors`() {
        val medication = validMedication().copy(
            frequency = Frequency.SpecificDays(
                daysOfWeek = setOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.WEDNESDAY
                ),
                times = listOf(LocalTime.of(8, 0))
            )
        )

        val errors = MedicationValidator.validateAll(medication)

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `invalid interval returns InvalidInterval`() {
        val medication = validMedication().copy(
            frequency = Frequency.EveryXHours(
                intervalHours = -1,
                firstTime = LocalTime.of(8, 0)
            )
        )

        val errors = MedicationValidator.validateAll(medication)

        assertTrue(MedicationError.InvalidInterval in errors)
    }

    @Test
    fun `valid interval returns no errors`() {
        val medication = validMedication().copy(
            frequency = Frequency.EveryXHours(
                intervalHours = 8,
                firstTime = LocalTime.of(8, 0)
            )
        )

        val errors = MedicationValidator.validateAll(medication)

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validate returns first error`() {
        val medication = validMedication().copy(
            name = "",
            dosage = validMedication().dosage.copy(
                amount = BigDecimal.ZERO
            )
        )

        val result = MedicationValidator.validate(medication)

        assertEquals(
            AppResult.Error(MedicationError.EmptyName),
            result
        )
    }

    @Test
    fun `validate returns success when medication is valid`() {
        val result = MedicationValidator.validate(validMedication())

        assertEquals(AppResult.Success(Unit), result)
    }

    @Test
    fun `validateAll returns multiple errors together`() {
        val medication = validMedication().copy(
            name = "",
            dosage = validMedication().dosage.copy(
                amount = BigDecimal.ZERO
            ),
            endDate = today.minusDays(1)
        )

        val errors = MedicationValidator.validateAll(medication)

        assertTrue(MedicationError.EmptyName in errors)
        assertTrue(MedicationError.InvalidDosageAmount in errors)
        assertTrue(MedicationError.EndDateBeforeStartDate in errors)
    }

    @Test
    fun `validateAll does not return duplicate errors`() {
        val time = LocalTime.of(8, 0)
        val medication = validMedication().copy(
            frequency = Frequency.Daily(times = listOf(time, time))
        )

        val errors = MedicationValidator.validateAll(medication)

        assertEquals(errors.size, errors.distinct().size)
    }
}
