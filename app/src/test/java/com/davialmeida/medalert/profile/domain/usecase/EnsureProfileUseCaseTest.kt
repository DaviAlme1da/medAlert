package com.davialmeida.medalert.profile.domain.usecase

import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EnsureProfileUseCaseTest {

    private lateinit var repository: FakeProfileRepository
    private lateinit var useCase: EnsureProfileUseCase

    @Before
    fun setup() {
        repository = FakeProfileRepository()
        useCase = EnsureProfileUseCase(repository)
    }

    @Test
    fun `creates the default profile with the given name and makes it active`() = runTest {
        useCase("Meu perfil")

        val profiles = repository.observeAll().first()

        assertEquals(1, profiles.size)
        assertEquals("Meu perfil", profiles.first().name)
        assertEquals(profiles.first().id, repository.storedActiveProfileId())
    }

    @Test
    fun `does nothing when a profile already exists`() = runTest {
        repository.create(
            Profile(id = 0, name = "Davi", color = ProfileColor.GREEN)
        )

        useCase("Meu perfil")

        val profiles = repository.observeAll().first()

        assertEquals(1, profiles.size)
        assertEquals("Davi", profiles.first().name)
    }

    @Test
    fun `calling twice creates only one profile`() = runTest {
        useCase("Meu perfil")
        useCase("Meu perfil")

        assertEquals(1, repository.observeAll().first().size)
    }
}