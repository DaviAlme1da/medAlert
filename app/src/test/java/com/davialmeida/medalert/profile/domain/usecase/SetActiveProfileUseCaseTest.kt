package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.profile.domain.error.ProfileError
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SetActiveProfileUseCaseTest {

    private lateinit var repository: FakeProfileRepository
    private lateinit var useCase: SetActiveProfileUseCase

    @Before
    fun setup() {
        repository = FakeProfileRepository()
        useCase = SetActiveProfileUseCase(repository)
    }

    private suspend fun createProfile(name: String): Long {
        return repository.create(
            Profile(id = 0, name = name, color = ProfileColor.BLUE)
        )
    }

    @Test
    fun `sets an existing profile as active`() = runTest {
        createProfile("Davi")
        val secondId = createProfile("Joao")

        val result = useCase(secondId)

        assertTrue(result is AppResult.Success)
        assertEquals(secondId, repository.observeActiveProfile().first()?.id)
    }

    @Test
    fun `returns ProfileNotFound for an unknown profile and keeps nothing active`() = runTest {
        val result = useCase(999)

        assertEquals(AppResult.Error(ProfileError.ProfileNotFound), result)
        assertNull(repository.observeActiveProfile().first())
    }

    @Test
    fun `can switch the active profile`() = runTest {
        val firstId = createProfile("Davi")
        val secondId = createProfile("Joao")

        useCase(firstId)
        assertEquals(firstId, repository.observeActiveProfile().first()?.id)

        useCase(secondId)
        assertEquals(secondId, repository.observeActiveProfile().first()?.id)
    }

    @Test
    fun `returns UnexpectedAppError when saving the active profile fails`() = runTest {
        val firstId = createProfile("Davi")
        val secondId = createProfile("Joao")
        useCase(firstId)

        repository.failOnSetActiveProfile = true

        val result = useCase(secondId)

        assertEquals(AppResult.Error(UnexpectedAppError), result)
        assertEquals(firstId, repository.storedActiveProfileId())
    }
}