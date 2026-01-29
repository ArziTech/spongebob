package com.example.spongebob.ui.theme

import android.app.Activity
import android.os.Build
import android.view.WindowInsetsController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// SpongeBob Dark Theme - Deep Ocean
private val DarkColorScheme = darkColorScheme(
    primary = OceanBlue,
    onPrimary = BubbleWhite,
    primaryContainer = DeepSea,
    onPrimaryContainer = BubbleWhite,

    secondary = SpongeYellow,
    onSecondary = DeepSea,
    secondaryContainer = SpongeYellowDark,
    onSecondaryContainer = DeepSea,

    tertiary = PatrickPink,
    onTertiary = BubbleWhite,

    background = DeepSea,
    onBackground = BubbleWhite,

    surface = Color(0xFF0D47A1),
    onSurface = BubbleWhite,

    surfaceVariant = Color(0xFF1565C0),
    onSurfaceVariant = SeaFoam,

    error = KrabRed,
    onError = BubbleWhite,

    outline = SeaFoam.copy(alpha = 0.5f),
    outlineVariant = OceanBlue.copy(alpha = 0.3f)
)

// SpongeBob Light Theme - Shallow Water
private val LightColorScheme = lightColorScheme(
    primary = OceanBlue,
    onPrimary = BubbleWhite,
    primaryContainer = ShallowWater,
    onPrimaryContainer = DeepSea,

    secondary = SpongeYellowDark,
    onSecondary = DeepSea,
    secondaryContainer = SpongeYellowLight,
    onSecondaryContainer = DeepSea,

    tertiary = PatrickPink,
    onTertiary = BubbleWhite,

    background = BubbleWhite,
    onBackground = DeepSea,

    surface = Color(0xFFE1F5FE),
    onSurface = DeepSea,

    surfaceVariant = SeaFoam.copy(alpha = 0.3f),
    onSurfaceVariant = OceanBlueDark,

    error = KrabRed,
    onError = BubbleWhite,

    outline = OceanBlue.copy(alpha = 0.3f),
    outlineVariant = ShallowWater.copy(alpha = 0.5f)
)

@Composable
fun SpongebobTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled to use SpongeBob theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            // Set status bar color
            window.statusBarColor = colorScheme.primary.toArgb()
            // Set light/dark status bar appearance
            insetsController.isAppearanceLightStatusBars = !darkTheme
            // Set light/dark navigation bar appearance
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
