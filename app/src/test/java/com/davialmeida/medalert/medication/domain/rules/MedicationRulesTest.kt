package com.davialmeida.medalert.medication.domain.rules

import com.davialmeida.medalert.medication.domain.model.Dosage
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.medication.domain.model.Frequency
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalTime

class MedicationScheduleComparisonTest {

    private val eight = LocalTime.of(8, 0)
    private val twenty = LocalTime.of(20, 0)

    @Test
    fun `dosage with same value and different scale is the same`() {
        val a = Dosage(BigDecimal("1.0"), DosageUnit.MG)
        val b = Dosage(BigDecimal("1.00"), DosageUnit.MG)

        assertTrue(a.hasSameValueAs(b))
    }

    @Test
    fun `dosage with different unit is different`() {
        val a = Dosage(BigDecimal("1"), DosageUnit.MG)
        val b = Dosage(BigDecimal("1"), DosageUnit.G)

        assertFalse(a.hasSameValueAs(b))
    }

    @Test
    fun `daily frequency ignores time order`() {
        val a = Frequency.Daily(listOf(eight, twenty))
        val b = Frequency.Daily(listOf(twenty, eight))

        assertTrue(a.hasSameScheduleAs(b))
    }

    @Test
    fun `daily and specific days are different`() {
        val a = Frequency.Daily(listOf(eight))
        val b = Frequency.SpecificDays(setOf(DayOfWeek.MONDAY), listOf(eight))

        assertFalse(a.hasSameScheduleAs(b))
    }

    @Test
    fun `specific days with different days are different`() {
        val a = Frequency.SpecificDays(setOf(DayOfWeek.MONDAY), listOf(eight))
        val b = Frequency.SpecificDays(setOf(DayOfWeek.TUESDAY), listOf(eight))

        assertFalse(a.hasSameScheduleAs(b))
    }

    @Test
    fun `every x hours with different interval is different`() {
        val a = Frequency.EveryXHours(8, eight)
        val b = Frequency.EveryXHours(6, eight)

        assertFalse(a.hasSameScheduleAs(b))
    }
}
