package com.davialmeida.medalert.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class StatusColors(
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val onDangerContainer: Color
)

val LightStatusColors = StatusColors(
    success = SecondaryGreen,
    successContainer = SuccessContainer,
    onSuccessContainer = SuccessOnContainer,
    warning = WarningAmber,
    warningContainer = WarningContainer,
    onWarningContainer = WarningOnContainer,
    danger = ErrorRed,
    dangerContainer = ErrorContainer,
    onDangerContainer = ErrorOnContainer
)

val DarkStatusColors = StatusColors(
    success = DarkSecondary,
    successContainer = DarkSuccessContainer,
    onSuccessContainer = DarkSuccessOnContainer,
    warning = DarkWarning,
    warningContainer = DarkWarningContainer,
    onWarningContainer = DarkWarningOnContainer,
    danger = DarkError,
    dangerContainer = DarkErrorContainer,
    onDangerContainer = DarkErrorOnContainer
)

val LocalStatusColors = staticCompositionLocalOf { LightStatusColors }

val MaterialTheme.statusColors: StatusColors
    @Composable
    @ReadOnlyComposable
    get() = LocalStatusColors.current