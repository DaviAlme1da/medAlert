package com.davialmeida.medalert.core.domain.usecase

import com.davialmeida.medalert.profile.domain.model.Profile
import com.davialmeida.medalert.profile.domain.model.ProfileColor
import com.davialmeida.medalert.profile.domain.usecase.EnsureActiveProfileUseCase
import com.davialmeida.medalert.profile.domain.usecase.EnsureProfileUseCase
import com.davialmeida.medalert.profile.domain.usecase.FakeProfileRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class InitializeAppUseCaseTest {

    private lateinit var repository: FakeProfileRepository
    private lateinit var useCase: InitializeAppUseCase

    @Before
    fun setup() {
        repository = FakeProfileRepository()
        useCase = InitializeAppUseCase(
            ensureProfileUseCase = EnsureProfileUseCase(repository),
            ensureActiveProfileUseCase = EnsureActiveProfileUseCase(repository)
        )
    }

    @Test
    fun `first launch creates the default profile and makes it active`() = runTest {
        useCase(defaultProfileName = "Meu perfil")

        val profiles = repository.observeAll().first()

        assertEquals(1, profiles.size)
        assertEquals("Meu perfil", profiles.first().name)
        assertEquals(profiles.first().id, repository.storedActiveProfileId())
    }

    @Test
    fun `recovers an invalid active profile id`() = runTest {
        val firstId = repository.create(
            Profile(id = 0, name = "Davi", color = ProfileColor.BLUE)
        )
        repository.setActiveProfileId(999)

        useCase(defaultProfileName = "Meu perfil")

        assertEquals(firstId, repository.storedActiveProfileId())
        assertEquals(1, repository.observeAll().first().size)
    }

    @Test
    fun `keeps a valid active profile`() = runTest {
        repository.create(
            Profile(id = 0, name = "Davi", color = ProfileColor.BLUE)
        )
        val secondId = repository.create(
            Profile(id = 0, name = "Joao", color = ProfileColor.GREEN)
        )
        repository.setActiveProfileId(secondId)

        useCase(defaultProfileName = "Meu perfil")

        assertEquals(secondId, repository.storedActiveProfileId())
    }
}