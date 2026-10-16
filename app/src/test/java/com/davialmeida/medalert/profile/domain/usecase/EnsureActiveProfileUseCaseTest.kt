package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class EnsureActiveProfileUseCaseTest {

    private lateinit var repository: FakeProfileRepository
    private lateinit var useCase: EnsureActiveProfileUseCase

    @Before
    fun setup() {
        repository = FakeProfileRepository()
        useCase = EnsureActiveProfileUseCase(repository)
    }

    private suspend fun createProfile(name: String): Long {
        return repository.create(
            Profile(id = 0, name = name, color = ProfileColor.BLUE)
        )
    }

    @Test
    fun `keeps a valid active profile`() = runTest {
        createProfile("Davi")
        val secondId = createProfile("Joao")
        repository.setActiveProfileId(secondId)

        useCase()

        assertEquals(secondId, repository.storedActiveProfileId())
    }

    @Test
    fun `selects the first profile when none is active`() = runTest {
        val firstId = createProfile("Davi")
        createProfile("Joao")

        useCase()

        assertEquals(firstId, repository.storedActiveProfileId())
    }

    @Test
    fun `replaces the id of a deleted profile`() = runTest {
        val firstId = createProfile("Davi")
        val secondId = createProfile("Joao")
        repository.setActiveProfileId(firstId)

        val firstProfile = repository.observeById(firstId).first()!!
        repository.delete(firstProfile)

        useCase()

        assertEquals(secondId, repository.storedActiveProfileId())
    }

    @Test
    fun `writes nothing when there are no profiles`() = runTest {
        useCase()

        assertNull(repository.storedActiveProfileId())
    }

    @Test
    fun `calling twice gives the same result`() = runTest {
        val firstId = createProfile("Davi")
        createProfile("Joao")

        useCase()
        useCase()

        assertEquals(firstId, repository.storedActiveProfileId())
    }
}