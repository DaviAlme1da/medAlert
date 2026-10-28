package com.davialmeida.medalert.reminder.presentation.viewmodel

import com.davialmeida.medalert.medication.domain.usecase.FakeMedicationRepository
import com.davialmeida.medalert.medication.domain.usecase.sampleMedication
import com.davialmeida.medalert.reminder.domain.model.ReminderPermissionStatus
import com.davialmeida.medalert.reminder.domain.usecase.FakeReminderScheduler
import com.davialmeida.medalert.reminder.domain.usecase.RescheduleRemindersUseCase
import com.davialmeida.medalert.testutil.FakeReminderPermissionChecker
import com.davialmeida.medalert.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ReminderPermissionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Antes das 08:00, para existir um próximo lembrete.
    private val clock = Clock.fixed(Instant.parse("2026-10-16T07:00:00Z"), ZoneOffset.UTC)

    private val checker = FakeReminderPermissionChecker()
    private val scheduler = FakeReminderScheduler()
    private val medicationRepository = FakeMedicationRepository().apply {
        data.add(sampleMedication())
    }

    private fun viewModel() = ReminderPermissionViewModel(
        permissionChecker = checker,
        rescheduleReminders = RescheduleRemindersUseCase(medicationRepository, scheduler, clock)
    )

    @Test
    fun `starts with the current permission status`() = runTest {
        checker.current = ReminderPermissionStatus(true, false)

        assertEquals(ReminderPermissionStatus(true, false), viewModel().status.value)
    }

    @Test
    fun `refresh reads the new status`() = runTest {
        val viewModel = viewModel()

        checker.current = ReminderPermissionStatus(true, true)
        viewModel.refresh()

        assertTrue(viewModel.status.value.isComplete)
    }

    @Test
    fun `reschedules when exact alarms become allowed`() = runTest {
        checker.current = ReminderPermissionStatus(true, false)
        val viewModel = viewModel()

        checker.current = ReminderPermissionStatus(true, true)
        viewModel.refresh()

        assertEquals(1, scheduler.scheduled.size)
    }

    @Test
    fun `does not reschedule when exact alarms were already allowed`() = runTest {
        checker.current = ReminderPermissionStatus(true, true)
        val viewModel = viewModel()

        viewModel.refresh()

        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun `does not reschedule when only notifications change`() = runTest {
        checker.current = ReminderPermissionStatus(false, false)
        val viewModel = viewModel()

        checker.current = ReminderPermissionStatus(true, false)
        viewModel.refresh()

        assertTrue(scheduler.scheduled.isEmpty())
    }
}