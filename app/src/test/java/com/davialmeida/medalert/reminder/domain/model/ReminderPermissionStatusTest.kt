package com.davialmeida.medalert.reminder.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderPermissionStatusTest {

    @Test
    fun `is complete only when both permissions are allowed`() {
        assertTrue(ReminderPermissionStatus(true, true).isComplete)
        assertFalse(ReminderPermissionStatus(true, false).isComplete)
        assertFalse(ReminderPermissionStatus(false, true).isComplete)
        assertFalse(ReminderPermissionStatus(false, false).isComplete)
    }
}