package com.davialmeida.medalert.profile.domain.validation

import com.davialmeida.medalert.core.domain.AppResult
import com.davialmeida.medalert.profile.domain.error.ProfileError
import org.junit.Assert
import org.junit.Test

class ProfileValidatorTest {

    @Test
    fun `blank name returns EmptyName`() {
        Assert.assertEquals(
            AppResult.Error(ProfileError.EmptyName),
            ProfileValidator.validateName("   ")
        )
    }

    @Test
    fun `name with 2 characters returns NameTooShort`() {
        Assert.assertEquals(
            AppResult.Error(ProfileError.NameTooShort),
            ProfileValidator.validateName("Ab")
        )
    }

    @Test
    fun `name with 3 characters is valid`() {
        Assert.assertEquals(
            AppResult.Success(Unit),
            ProfileValidator.validateName("Abc")
        )
    }

    @Test
    fun `name with 30 characters is valid`() {
        Assert.assertEquals(
            AppResult.Success(Unit),
            ProfileValidator.validateName("a".repeat(30))
        )
    }

    @Test
    fun `name with 31 characters returns NameTooLong`() {
        Assert.assertEquals(
            AppResult.Error(ProfileError.NameTooLong),
            ProfileValidator.validateName("a".repeat(31))
        )
    }

    @Test
    fun `spaces around a short name are ignored`() {
        Assert.assertEquals(
            AppResult.Error(ProfileError.NameTooShort),
            ProfileValidator.validateName("  Ab  ")
        )
    }

    @Test
    fun `spaces around a valid name are ignored`() {
        Assert.assertEquals(
            AppResult.Success(Unit),
            ProfileValidator.validateName("  Abc  ")
        )
    }
}