package com.davialmeida.medalert.reminder.data.notification

import com.davialmeida.medalert.medication.domain.model.Dosage
import com.davialmeida.medalert.medication.domain.model.DosageUnit
import com.davialmeida.medalert.reminder.domain.model.DueReminder
import org.junit.Assert
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class NotificationIdTest {

    private val base = LocalDateTime.of(2026, 10, 16, 8, 0)

    private fun reminder(medicationId: Long, at: LocalDateTime) = DueReminder(
        medicationId = medicationId,
        medicationName = "Med",
        dosage = Dosage(BigDecimal("1"), DosageUnit.TABLETS),
        profileName = "Pessoal",
        scheduledDateTime = at
    )

    @Test
    fun `same dose always produces the same id`() {
        Assert.assertEquals(
            notificationId(reminder(1, base)),
            notificationId(reminder(1, base))
        )
    }

    @Test
    fun `different medications produce different ids`() {
        Assert.assertNotEquals(
            notificationId(reminder(1, base)),
            notificationId(reminder(2, base))
        )
    }

    @Test
    fun `different times produce different ids`() {
        Assert.assertNotEquals(
            notificationId(reminder(1, base)),
            notificationId(reminder(1, base.plusHours(6)))
        )
    }
}