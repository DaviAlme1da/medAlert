package com.davialmeida.medalert.settings.presentation.viewmodel

import com.davialmeida.medalert.settings.domain.model.AppSettings
import com.davialmeida.medalert.settings.domain.model.ThemePreference
import com.davialmeida.medalert.settings.domain.usecase.ObserveSettingsUseCase
import com.davialmeida.medalert.settings.domain.usecase.SetSnoozeMinutesUseCase
import com.davialmeida.medalert.settings.domain.usecase.SetThemePreferenceUseCase
import com.davialmeida.medalert.testutil.FakeSettingsRepository
import com.davialmeida.medalert.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSettingsRepository()

    private fun viewModel() = SettingsViewModel(
        observeSettingsUseCase = ObserveSettingsUseCase(repository),
        setThemePreferenceUseCase = SetThemePreferenceUseCase(repository),
        setSnoozeMinutesUseCase = SetSnoozeMinutesUseCase(repository)
    )

    /** O uiState usa WhileSubscribed: precisa de um coletor ativo. */
    private fun TestScope.collect(viewModel: SettingsViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
    }

    @Test
    fun `starts with the stored settings`() = runTest {
        val viewModel = viewModel().also { collect(it) }

        assertEquals(AppSettings(), viewModel.uiState.value.settings)
        assertFalse(viewModel.uiState.value.hasSaveError)
    }

    @Test
    fun `selecting a theme updates the state`() = runTest {
        val viewModel = viewModel().also { collect(it) }

        viewModel.onThemeSelected(ThemePreference.DARK)

        assertEquals(ThemePreference.DARK, viewModel.uiState.value.settings.theme)
    }

    @Test
    fun `selecting a snooze duration updates the state`() = runTest {
        val viewModel = viewModel().also { collect(it) }

        viewModel.onSnoozeMinutesSelected(15)

        assertEquals(15, viewModel.uiState.value.settings.snoozeMinutes)
    }

    @Test
    fun `an invalid snooze duration shows the save error`() = runTest {
        val viewModel = viewModel().also { collect(it) }

        viewModel.onSnoozeMinutesSelected(7)

        assertTrue(viewModel.uiState.value.hasSaveError)
        assertEquals(10, viewModel.uiState.value.settings.snoozeMinutes)
    }

    @Test
    fun `a write failure shows the save error and it can be cleared`() = runTest {
        val viewModel = viewModel().also { collect(it) }
        repository.failOnWrite = true

        viewModel.onThemeSelected(ThemePreference.DARK)
        assertTrue(viewModel.uiState.value.hasSaveError)

        viewModel.clearSaveError()
        assertFalse(viewModel.uiState.value.hasSaveError)
    }

    @Test
    fun `a successful save clears a previous error`() = runTest {
        val viewModel = viewModel().also { collect(it) }
        viewModel.onSnoozeMinutesSelected(7)
        assertTrue(viewModel.uiState.value.hasSaveError)

        viewModel.onSnoozeMinutesSelected(5)

        assertFalse(viewModel.uiState.value.hasSaveError)
    }
}