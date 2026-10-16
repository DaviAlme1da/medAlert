package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.profile.domain.error.ProfileError
import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteProfileUseCaseTest {

    private lateinit var repository: FakeProfileRepository
    private lateinit var useCase: DeleteProfileUseCase

    @Before
    fun setup() {
        repository = FakeProfileRepository()
        useCase = DeleteProfileUseCase(repository)
    }

    private suspend fun createProfile(name: String): Long {
        return repository.create(
            Profile(id = 0, name = name, color = ProfileColor.BLUE)
        )
    }

    @Test
    fun `cannot delete the last profile`() = runTest {
        val id = createProfile("Davi")

        val result = useCase(id)

        assertEquals(AppResult.Error(ProfileError.CannotDeleteLastProfile), result)
        assertNotNull(repository.observeById(id).first())
    }

    @Test
    fun `deletes a non active profile and keeps the active one`() = runTest {
        val firstId = createProfile("Davi")
        val secondId = createProfile("Joao")
        repository.setActiveProfileId(firstId)

        val result = useCase(secondId)

        assertTrue(result is AppResult.Success)
        assertNull(repository.observeById(secondId).first())
        assertNotNull(repository.observeById(firstId).first())
        assertEquals(firstId, repository.storedActiveProfileId())
    }

    @Test
    fun `returns ProfileNotFound for an unknown profile`() = runTest {
        val result = useCase(999)

        assertEquals(AppResult.Error(ProfileError.ProfileNotFound), result)
    }

    @Test
    fun `deleting the active profile switches to another existing profile`() = runTest {
        val firstId = createProfile("Davi")
        createProfile("Joao")
        createProfile("Maria")
        repository.setActiveProfileId(firstId)

        val result = useCase(firstId)

        assertTrue(result is AppResult.Success)
        assertNull(repository.observeById(firstId).first())

        val activeId = repository.storedActiveProfileId()
        assertNotEquals(firstId, activeId)
        assertTrue(repository.observeAll().first().any { it.id == activeId })
    }

    @Test
    fun `failing delete returns DeleteFailed and keeps a valid state`() = runTest {
        val firstId = createProfile("Davi")
        createProfile("Joao")
        repository.setActiveProfileId(firstId)
        repository.failOnDelete = true

        val result = useCase(firstId)

        assertEquals(AppResult.Error(ProfileError.DeleteFailed), result)
        assertNotNull(repository.observeById(firstId).first())

        val activeId = repository.storedActiveProfileId()
        assertTrue(repository.observeAll().first().any { it.id == activeId })
    }
}