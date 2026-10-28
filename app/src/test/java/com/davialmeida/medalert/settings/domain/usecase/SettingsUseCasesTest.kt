package com.davialmeida.medalert.settings.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.settings.domain.model.SNOOZE_OPTIONS
import com.davialmeida.medalert.settings.domain.model.ThemePreference
import com.davialmeida.medalert.testutil.FakeSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsUseCasesTest {

    private val repository = FakeSettingsRepository()

    @Test
    fun `every snooze option is accepted`() = runTest {
        val useCase = SetSnoozeMinutesUseCase(repository)

        SNOOZE_OPTIONS.forEach { minutes ->
            assertEquals(AppResult.Success(Unit), useCase(minutes))
            assertEquals(minutes, repository.settings.snoozeMinutes)
        }
    }

    @Test
    fun `a value outside the options is rejected and nothing is saved`() = runTest {
        val result = SetSnoozeMinutesUseCase(repository)(7)

        assertEquals(AppResult.Error(UnexpectedAppError), result)
        assertEquals(10, repository.settings.snoozeMinutes)
    }

    @Test
    fun `a write failure on snooze becomes an error`() = runTest {
        repository.failOnWrite = true

        val result = SetSnoozeMinutesUseCase(repository)(5)

        assertEquals(AppResult.Error(UnexpectedAppError), result)
    }

    @Test
    fun `theme preference is saved`() = runTest {
        val result = SetThemePreferenceUseCase(repository)(ThemePreference.DARK)

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(ThemePreference.DARK, repository.settings.theme)
    }

    @Test
    fun `a write failure on theme becomes an error`() = runTest {
        repository.failOnWrite = true

        val result = SetThemePreferenceUseCase(repository)(ThemePreference.LIGHT)

        assertEquals(AppResult.Error(UnexpectedAppError), result)
    }

    @Test
    fun `completing the onboarding is persisted`() = runTest {
        assertFalse(repository.settings.onboardingCompleted)

        val result = CompleteOnboardingUseCase(repository)()

        assertEquals(AppResult.Success(Unit), result)
        assertTrue(repository.settings.onboardingCompleted)
    }

    @Test
    fun `a write failure on onboarding becomes an error`() = runTest {
        repository.failOnWrite = true

        assertEquals(
            AppResult.Error(UnexpectedAppError),
            CompleteOnboardingUseCase(repository)()
        )
    }

    @Test
    fun `observing settings emits the current values`() = runTest {
        repository.setTheme(ThemePreference.LIGHT)

        val settings = ObserveSettingsUseCase(repository)().first()

        assertEquals(ThemePreference.LIGHT, settings.theme)
    }
}