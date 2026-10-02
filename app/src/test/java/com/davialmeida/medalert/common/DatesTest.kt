package com.davialmeida.medalert.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DatesTest {

    @Test
    fun `toDisplayString and toLocalTimeOrNull should be round trip`() {
        val original = LocalTime.of(14, 30)

        val text = original.toDisplayString()
        val result = text.toLocalTimeOrNull()

        assertEquals(original, result)
    }

    @Test
    fun `toLocalTimeOrNull should return null for invalid time`() {
        assertNull("25:00".toLocalTimeOrNull())
        assertNull("".toLocalTimeOrNull())
        assertNull("abc".toLocalTimeOrNull())
    }

    @Test
    fun `startOfDay should represent different instants for different zones`() {
        val date = LocalDate.of(2026, 10, 2)

        val saoPaulo = date.startOfDay(
            ZoneId.of("America/Sao_Paulo")
        )

        val utc = date.startOfDay(
            ZoneId.of("UTC")
        )

        assertEquals(
            false,
            saoPaulo == utc
        )
    }

    @Test
    fun `toLocalDate should depend on zone near midnighMainScreent`() {
        val instant = Instant.parse("2026-10-02T02:30:00Z")

        val saoPaulo = instant.toLocalDate(
            ZoneId.of("America/Sao_Paulo")
        )

        val utc = instant.toLocalDate(
            ZoneId.of("UTC")
        )

        assertEquals(
            LocalDate.of(2026, 10, 1),
            saoPaulo
        )

        assertEquals(
            LocalDate.of(2026, 10, 2),
            utc
        )
    }
}