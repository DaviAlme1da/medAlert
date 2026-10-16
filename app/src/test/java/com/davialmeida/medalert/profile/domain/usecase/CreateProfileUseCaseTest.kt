package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.core.domain.UnexpectedAppError
import com.davialmeida.medalert.profile.domain.error.ProfileError
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateProfileUseCaseTest {

    private lateinit var repository: FakeProfileRepository
    private lateinit var useCase: CreateProfileUseCase

    @Before
    fun setup() {
        repository = FakeProfileRepository()
        useCase = CreateProfileUseCase(repository)
    }

    @Test
    fun `creates profile with valid data`() = runTest {
        val result = useCase(name = "Davi", color = ProfileColor.BLUE)

        assertTrue(result is AppResult.Success)

        val profiles = repository.observeAll().first()

        assertEquals(1, profiles.size)
        assertEquals("Davi", profiles.first().name)
        assertEquals(ProfileColor.BLUE, profiles.first().color)
    }

    @Test
    fun `blank name returns EmptyName and saves nothing`() = runTest {
        val result = useCase(name = "", color = ProfileColor.BLUE)

        assertEquals(AppResult.Error(ProfileError.EmptyName), result)
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun `short name returns NameTooShort and saves nothing`() = runTest {
        val result = useCase(name = "Ab", color = ProfileColor.BLUE)

        assertEquals(AppResult.Error(ProfileError.NameTooShort), result)
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun `long name returns NameTooLong and saves nothing`() = runTest {
        val result = useCase(name = "a".repeat(31), color = ProfileColor.BLUE)

        assertEquals(AppResult.Error(ProfileError.NameTooLong), result)
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun `trims the name before saving`() = runTest {
        useCase(name = "  Davi  ", color = ProfileColor.BLUE)

        assertEquals("Davi", repository.observeAll().first().first().name)
    }

    @Test
    fun `does not change the active profile`() = runTest {
        val firstId = repository.createIfEmpty(
            Profile(id = 0, name = "First", color = ProfileColor.BLUE)
        )

        useCase(name = "Second", color = ProfileColor.GREEN)

        assertEquals(firstId, repository.storedActiveProfileId())
    }

    @Test
    fun `returns UnexpectedAppError when the repository fails`() = runTest {
        repository.failOnCreate = true

        val result = useCase(name = "Davi", color = ProfileColor.BLUE)

        assertEquals(AppResult.Error(UnexpectedAppError), result)
    }
}