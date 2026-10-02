package com.davialmeida.medalert.core.validations

import com.davialmeida.medalert.R

fun interface Validator {
    fun validate(value: String): TextString?
}

class RequiredValidator : Validator {

    override fun validate(value: String): TextString? {
        return if (value.isBlank()) {
            TextString.ResourceString(
                R.string.error_field_required
            )
        } else {
            null
        }
    }
}

class MinLengthValidator(
    private val min: Int,
) : Validator {

    override fun validate(value: String): TextString? {
        return if (value.trim().length < min) {
            TextString.ResourceString(
                R.string.error_min_length,
                listOf(min)
            )
        } else {
            null
        }
    }
}

class NameValidator(
    private val min: Int = 3,
) : Validator {

    private val requiredValidator = RequiredValidator()
    private val minLengthValidator = MinLengthValidator(min)

    override fun validate(value: String): TextString? {
        return requiredValidator.validate(value)
            ?: minLengthValidator.validate(value)
    }
}