package com.davialmeida.medalert.validations

import com.davialmeida.medalert.R
import com.davialmeida.medalert.core.validations.NameValidator
import com.davialmeida.medalert.core.validations.RequiredValidator
import com.davialmeida.medalert.core.validations.MinLengthValidator
import com.davialmeida.medalert.core.validations.TextString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test
    fun `required validator should return error for blank value`() {
        val validator = RequiredValidator()

        val empty = validator.validate("")
        val spaces = validator.validate("   ")

        assertEquals(
            TextString.ResourceString(R.string.error_field_required),
            empty
        )

        assertEquals(
            TextString.ResourceString(R.string.error_field_required),
            spaces
        )
    }

    @Test
    fun `min length validator should return error for short value`() {
        val validator = MinLengthValidator(3)

        val result = validator.validate("Da")

        assertEquals(
            TextString.ResourceString(
                R.string.error_min_length,
                listOf(3)
            ),
            result
        )
    }

    @Test
    fun `name validator should return error for blank value`() {
        val validator = NameValidator()

        val result = validator.validate("   ")

        assertEquals(
            TextString.ResourceString(
                R.string.error_field_required
            ),
            result
        )
    }

    @Test
    fun `name validator should return error for short value`() {
        val validator = NameValidator()

        val result = validator.validate("Da")

        assertEquals(
            TextString.ResourceString(
                R.string.error_min_length,
                listOf(3)
            ),
            result
        )
    }

    @Test
    fun `name validator should return null for valid name`() {
        val validator = NameValidator()

        val result = validator.validate("Davi")

        assertNull(result)
    }

    @Test
    fun `min length validator should ignore spaces around value`() {
        val validator = MinLengthValidator(3)

        val result = validator.validate("  a")

        assertEquals(
            TextString.ResourceString(
                R.string.error_min_length,
                listOf(3)
            ),
            result
        )
    }
}