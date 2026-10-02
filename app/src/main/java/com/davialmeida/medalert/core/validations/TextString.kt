package com.davialmeida.medalert.core.validations

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface TextString {

    data class ResourceString(
        @StringRes val id: Int,
        val args: List<Any> = emptyList(),
    ) : TextString

    data class DynamicString(
        val value: String,
    ) : TextString

    @Composable
    fun asString(): String {
        return when (this) {
            is ResourceString -> stringResource(
                id = id,
                formatArgs = args.toTypedArray(),
            )

            is DynamicString -> value
        }
    }
}