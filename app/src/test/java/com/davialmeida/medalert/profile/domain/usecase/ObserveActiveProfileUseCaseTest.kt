package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ObserveActiveProfileUseCaseTest {

    private lateinit var repository: FakeProfileRepository
    private lateinit var useCase: ObserveActiveProfileUseCase

    @Before
    fun setup() {
        repository = FakeProfileRepository()
        useCase = ObserveActiveProfileUseCase(repository)
    }

    private suspend fun createProfile(name: String): Long {
        return repository.create(
            Profile(id = 0, name = name, color = ProfileColor.BLUE)
        )
    }

    @Test
    fun `returns the active profile`() = runTest {
        createProfile("Davi")
        val secondId = createProfile("Joao")
        repository.setActiveProfileId(secondId)

        assertEquals(secondId, useCase().first()?.id)
    }

    @Test
    fun `invalid id returns the first profile without writing`() = runTest {
        val firstId = createProfile("Davi")
        repository.setActiveProfileId(999)

        assertEquals(firstId, useCase().first()?.id)
        assertEquals(999L, repository.storedActiveProfileId())
    }

    @Test
    fun `returns null when there are no profiles`() = runTest {
        assertNull(useCase().first())
    }

    @Test
    fun `reflects a rename of the active profile`() = runTest {
        val id = createProfile("Davi")
        repository.setActiveProfileId(id)

        repository.update(
            Profile(id = id, name = "Davi Lucas", color = ProfileColor.BLUE)
        )

        assertEquals("Davi Lucas", useCase().first()?.name)
    }
}