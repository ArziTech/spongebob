package com.example.spongebob.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * OKLCH Color Theme
 *
 * Using OKLCH color space for perceptually uniform colors
 * Reference: https://oklch.com
 */

/**
 * Helper to create Color from L, C, H values (OKLCH)
 */
fun oklchColor(l: Float, c: Float, h: Float): Color {
    val rgb = oklchToRgb(l, c, h)
    return Color(rgb[0], rgb[1], rgb[2])
}

/**
 * OKLCH to sRGB conversion
 * Simplified version - approximate conversion
 */
private fun oklchToRgb(l: Float, c: Float, h: Float): FloatArray {
    val hRad = Math.toRadians(h.toDouble())
    val a = c * Math.cos(hRad)
    val b = c * Math.sin(hRad)

    // Simple OKLCH to RGB approximation
    val y = l
    val x = a
    val z = b

    // Convert to sRGB (simplified)
    val r = ((3.2406 * x - 1.5372 * y - 0.4986 * z) / 100 + 0.0555).toFloat().coerceIn(0f, 1f)
    val g = ((-0.9692 * x + 1.8760 * y + 0.0416 * z) / 100 + 0.0555).toFloat().coerceIn(0f, 1f)
    val bVal = ((0.0556 * x - 0.2040 * y + 1.0570 * z) / 100 + 0.0555).toFloat().coerceIn(0f, 1f)

    return floatArrayOf(r, g, bVal)
}

/**
 * Light Theme Colors (OKLCH)
 */
object OklchLightColors {
    val background = oklchColor(0.9924f, 0.0028f, 0.0824f)
    val foreground = oklchColor(0.1288f, 0.0219f, 0.3140f)
    val primary = oklchColor(0.2236f, 0.1469f, 0.2658f)
    val onPrimary = oklchColor(1.0f, 0.0f, 0.0f)
    val secondary = oklchColor(0.9387f, 0.0262f, 0.2644f)
    val onSecondary = oklchColor(0.4691f, 0.2225f, 0.4817f)
    val muted = oklchColor(0.9518f, 0.0057f, 0.3084f)
    val onMuted = oklchColor(0.4882f, 0.0203f, 0.3080f)
    val accent = oklchColor(0.9356f, 0.0312f, 0.2798f)
    val onAccent = oklchColor(0.4691f, 0.2225f, 0.4817f)
    val destructive = oklchColor(0.5858f, 0.2220f, 0.1758f)
    val onDestructive = oklchColor(1.0f, 0.0f, 0.0f)
    val border = oklchColor(0.9160f, 0.0120f, 0.3131f)
    val input = oklchColor(0.9160f, 0.0120f, 0.3131f)
    val ring = oklchColor(0.6219f, 0.2036f, 0.2621f)
    val card = oklchColor(1.0f, 0.0f, 0.0f)
    val onCard = oklchColor(0.1288f, 0.0219f, 0.3140f)
    val popover = oklchColor(1.0f, 0.0f, 0.0f)
    val onPopover = oklchColor(0.1288f, 0.0219f, 0.3140f)
}

/**
 * Dark Theme Colors (OKLCH)
 */
object OklchDarkColors {
    val background = oklchColor(0.1063f, 0.0172f, 0.2595f)
    val foreground = oklchColor(0.9924f, 0.0028f, 0.0824f)
    val primary = oklchColor(0.3820f, 0.1967f, 0.2658f)
    val onPrimary = oklchColor(1.0f, 0.0f, 0.0f)
    val secondary = oklchColor(0.2108f, 0.0426f, 0.2703f)
    val onSecondary = oklchColor(0.9924f, 0.0028f, 0.0824f)
    val muted = oklchColor(0.1797f, 0.0376f, 0.2796f)
    val onMuted = oklchColor(0.6878f, 0.0218f, 0.2858f)
    val accent = oklchColor(0.2553f, 0.0657f, 0.2746f)
    val onAccent = oklchColor(0.9924f, 0.0028f, 0.0824f)
    val destructive = oklchColor(0.4038f, 0.1343f, 0.1302f)
    val onDestructive = oklchColor(1.0f, 0.0f, 0.0f)
    val border = oklchColor(0.2445f, 0.0736f, 0.2808f)
    val input = oklchColor(0.2603f, 0.0625f, 0.2703f)
    val ring = oklchColor(0.3395f, 0.1557f, 0.2730f)
    val card = oklchColor(0.1450f, 0.0217f, 0.2631f)
    val onCard = oklchColor(0.9924f, 0.0028f, 0.0824f)
    val popover = oklchColor(0.1418f, 0.0229f, 0.2644f)
    val onPopover = oklchColor(0.9924f, 0.0028f, 0.0824f)
}
