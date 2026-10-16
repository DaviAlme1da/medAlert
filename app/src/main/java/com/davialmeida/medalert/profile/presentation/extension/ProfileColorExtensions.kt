package com.davialmeida.medalert.profile.presentation.extension

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.davialmeida.medalert.R
import com.davialmeida.medalert.compose.theme.ProfileBlue
import com.davialmeida.medalert.compose.theme.ProfileGreen
import com.davialmeida.medalert.compose.theme.ProfilePurple
import com.davialmeida.medalert.profile.domain.model.ProfileColor

fun ProfileColor.toComposeColor(): Color {
    return when (this) {
        ProfileColor.BLUE -> ProfileBlue
        ProfileColor.GREEN -> ProfileGreen
        ProfileColor.PURPLE -> ProfilePurple
    }
}

@StringRes
fun ProfileColor.labelRes(): Int {
    return when (this) {
        ProfileColor.BLUE -> R.string.profile_color_blue
        ProfileColor.GREEN -> R.string.profile_color_green
        ProfileColor.PURPLE -> R.string.profile_color_purple
    }
}