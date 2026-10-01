package com.davialmeida.medalert.compose.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
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
    surfaceVariant = BorderLight,
    onSurfaceVariant = TextSecondary,

    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = SurfaceWhite,
    surfaceContainer = SurfaceWhite,
    surfaceContainerHigh = SurfaceWhite,
    surfaceContainerHighest = SurfaceWhite,

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
    // Dynamic color is available on Android 12+
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}