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

class UpdateProfileUseCaseTest {

    private lateinit var repository: FakeProfileRepository
    private lateinit var useCase: UpdateProfileUseCase

    @Before
    fun setup() {
        repository = FakeProfileRepository()
        useCase = UpdateProfileUseCase(repository)
    }

    @Test
    fun `deve atualizar perfil com dados validos`() = runTest {
        val id = repository.create(
            Profile(
                id = 0,
                name = "Davi",
                color = ProfileColor.BLUE
            )
        )

        val updatedProfile = Profile(
            id = id,
            name = "Davi Lucas",
            color = ProfileColor.GREEN
        )

        val result = useCase(updatedProfile)

        assertTrue(result is AppResult.Success)

        val profile = repository
            .observeById(id)
            .first()

        assertEquals("Davi Lucas", profile?.name)
        assertEquals(ProfileColor.GREEN, profile?.color)
    }

    @Test
    fun `nao deve atualizar perfil com nome vazio`() = runTest {
        val id = repository.create(
            Profile(
                id = 0,
                name = "Davi",
                color = ProfileColor.BLUE
            )
        )

        val updatedProfile = Profile(
            id = id,
            name = "",
            color = ProfileColor.BLUE
        )

        val result = useCase(updatedProfile)

        assertEquals(
            AppResult.Error(ProfileError.EmptyName),
            result
        )

        val profile = repository
            .observeById(id)
            .first()

        assertEquals("Davi", profile?.name)
    }

    @Test
    fun `nao deve atualizar perfil com nome menor que o minimo`() = runTest {
        val id = repository.create(
            Profile(
                id = 0,
                name = "Davi",
                color = ProfileColor.BLUE
            )
        )

        val updatedProfile = Profile(
            id = id,
            name = "Ab",
            color = ProfileColor.BLUE
        )

        val result = useCase(updatedProfile)

        assertEquals(
            AppResult.Error(ProfileError.NameTooShort),
            result
        )

        val profile = repository
            .observeById(id)
            .first()

        assertEquals("Davi", profile?.name)
    }

    @Test
    fun `nao deve atualizar perfil que nao existe`() = runTest {
        val profile = Profile(
            id = 999,
            name = "Davi",
            color = ProfileColor.BLUE
        )

        val result = useCase(profile)

        assertEquals(
            AppResult.Error(ProfileError.ProfileNotFound),
            result
        )
    }

    @Test
    fun `deve remover espacos do nome antes de atualizar`() = runTest {
        val id = repository.create(
            Profile(
                id = 0,
                name = "Davi",
                color = ProfileColor.BLUE
            )
        )

        val updatedProfile = Profile(
            id = id,
            name = "  Davi Lucas  ",
            color = ProfileColor.BLUE
        )

        val result = useCase(updatedProfile)

        assertTrue(result is AppResult.Success)

        val profile = repository
            .observeById(id)
            .first()

        assertEquals("Davi Lucas", profile?.name)
    }

    @Test
    fun `returns UnexpectedAppError when the repository fails`() = runTest {
        val id = repository.create(
            Profile(id = 0, name = "Davi", color = ProfileColor.BLUE)
        )
        repository.failOnUpdate = true

        val result = useCase(
            Profile(id = id, name = "Davi Lucas", color = ProfileColor.GREEN)
        )

        assertEquals(AppResult.Error(UnexpectedAppError), result)
    }
}