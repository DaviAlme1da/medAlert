package com.davialmeida.medalert.compose.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkPrimary,

    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkPrimaryContainer,
    onSecondaryContainer = DarkPrimary,

    tertiary = DarkWarning,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkWarningContainer,
    onTertiaryContainer = DarkWarningOnContainer,

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = DarkTextSecondary,

    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = DarkSurfaceHigh,

    outline = DarkTextSecondary,
    outlineVariant = DarkBorder,

    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkErrorOnContainer,

    inverseSurface = DarkTextPrimary,
    inverseOnSurface = DarkBackground
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimaryTint,
    onPrimaryContainer = PrimaryBlue,

    secondary = SecondaryGreen,
    onSecondary = SurfaceWhite,
    secondaryContainer = PrimaryTint,
    onSecondaryContainer = PrimaryBlue,

    tertiary = WarningAmber,
    onTertiary = TextPrimary,
    tertiaryContainer = WarningContainer,
    onTertiaryContainer = WarningOnContainer,

    background = BackgroundOffWhite,
    onBackground = TextPrimary,

    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = PrimaryTint,
    onSurfaceVariant = TextSecondary,

    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = SurfaceWhite,
    surfaceContainer = SurfaceWhite,
    surfaceContainerHigh = SurfaceWhite,
    surfaceContainerHighest = PrimaryTint,

    outline = BorderStrong,
    outlineVariant = BorderLight,

    error = ErrorRed,
    onError = SurfaceWhite,
    errorContainer = ErrorContainer,
    onErrorContainer = ErrorOnContainer,

    inverseSurface = TextPrimary,
    inverseOnSurface = SurfaceWhite
)

@Composable
fun MedAlertTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Cor dinâmica fica desligada: o visual vem do Figma.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalStatusColors provides if (darkTheme) DarkStatusColors else LightStatusColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = MedShapes,
            content = content
        )
    }
}