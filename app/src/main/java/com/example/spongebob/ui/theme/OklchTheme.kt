package com.example.spongebob.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.spongebob.ui.theme.OklchLightColors as Light
import com.example.spongebob.ui.theme.OklchDarkColors as Dark

private val OklchLightColorScheme = lightColorScheme(
    primary = Light.primary,
    onPrimary = Light.onPrimary,
    primaryContainer = Light.primary.copy(alpha = 0.12f),
    onPrimaryContainer = Light.onPrimary,

    secondary = Light.secondary,
    onSecondary = Light.onSecondary,
    secondaryContainer = Light.secondary.copy(alpha = 0.12f),
    onSecondaryContainer = Light.onSecondary,

    tertiary = Light.accent,
    onTertiary = Light.onAccent,

    background = Light.background,
    onBackground = Light.foreground,
    surface = Light.card,
    onSurface = Light.onCard,

    surfaceVariant = Light.border,
    onSurfaceVariant = Light.onCard,
    surfaceContainer = Light.background,
    surfaceContainerHighest = Light.muted,

    error = Light.destructive,
    onError = Light.onDestructive,
    errorContainer = Light.destructive.copy(alpha = 0.12f),
    onErrorContainer = Light.onDestructive,

    outlineVariant = Light.input,
    inverseSurface = Light.foreground,
    inverseOnSurface = Light.background,
    inversePrimary = Light.foreground,

    scrim = Light.foreground.copy(alpha = 0.1f)
)

private val OklchDarkColorScheme = darkColorScheme(
    primary = Dark.primary,
    onPrimary = Dark.onPrimary,
    primaryContainer = Dark.primary.copy(alpha = 0.16f),
    onPrimaryContainer = Dark.onPrimary,

    secondary = Dark.secondary,
    onSecondary = Dark.onSecondary,
    secondaryContainer = Dark.secondary.copy(alpha = 0.16f),
    onSecondaryContainer = Dark.onSecondary,

    tertiary = Dark.accent,
    onTertiary = Dark.onAccent,

    background = Dark.background,
    onBackground = Dark.foreground,
    surface = Dark.card,
    onSurface = Dark.onCard,

    surfaceVariant = Dark.border,
    onSurfaceVariant = Dark.onCard,
    surfaceContainer = Dark.background,
    surfaceContainerHighest = Dark.muted,

    error = Dark.destructive,
    onError = Dark.onDestructive,
    errorContainer = Dark.destructive.copy(alpha = 0.16f),
    onErrorContainer = Dark.onDestructive,

    outlineVariant = Dark.input,
    inverseSurface = Dark.foreground,
    inverseOnSurface = Dark.background,
    inversePrimary = Dark.foreground,

    scrim = Dark.foreground.copy(alpha = 0.1f)
)

@Composable
fun OklchTheme(
    themeOption: ThemeOption = ThemeOption.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeOption) {
        ThemeOption.LIGHT -> false
        ThemeOption.DARK -> true
        ThemeOption.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (darkTheme) OklchDarkColorScheme else OklchLightColorScheme,
        typography = Typography,
        content = content
    )
}
