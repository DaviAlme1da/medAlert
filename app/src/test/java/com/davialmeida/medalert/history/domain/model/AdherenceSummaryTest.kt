package com.davialmeida.medalert.history.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdherenceSummaryTest {

    @Test
    fun `no resolved doses means no percentage`() {
        assertNull(AdherenceSummary(taken = 0, skipped = 0, late = 0).percentage)
    }

    @Test
    fun `all taken is one hundred percent`() {
        assertEquals(100, AdherenceSummary(taken = 4, skipped = 0, late = 0).percentage)
    }

    @Test
    fun `skipped and late doses lower the percentage`() {
        assertEquals(0, AdherenceSummary(taken = 0, skipped = 1, late = 1).percentage)
        assertEquals(33, AdherenceSummary(taken = 1, skipped = 0, late = 2).percentage)
    }

    @Test
    fun `percentage is rounded to the nearest integer`() {
        assertEquals(67, AdherenceSummary(taken = 2, skipped = 1, late = 0).percentage)
    }

    @Test
    fun `resolved is the sum of every outcome`() {
        assertEquals(6, AdherenceSummary(taken = 3, skipped = 2, late = 1).resolved)
    }
}