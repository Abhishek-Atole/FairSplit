package com.fairsplit.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Light Color Scheme for FairSplit
 * Based on extracted design tokens from UI mockups
 */
private val LightColorScheme = lightColorScheme(
    // Primary colors
    primary = Primary,
    onPrimary = SurfaceLight,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = TextPrimaryLight,
    
    // Secondary colors (using Positive green as secondary)
    secondary = Positive,
    onSecondary = SurfaceLight,
    secondaryContainer = PositiveLight,
    onSecondaryContainer = TextPrimaryLight,
    
    // Tertiary colors (using Info blue as tertiary)
    tertiary = Info,
    onTertiary = SurfaceLight,
    tertiaryContainer = InfoLight,
    onTertiaryContainer = TextPrimaryLight,
    
    // Background colors
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    
    // Surface colors
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceTint = Primary,
    
    // Inverse colors
    inverseSurface = SurfaceDark,
    inverseOnSurface = TextPrimaryDark,
    inversePrimary = PrimaryLight,
    
    // Error colors
    error = Negative,
    onError = SurfaceLight,
    errorContainer = NegativeLight,
    onErrorContainer = TextPrimaryLight,
    
    // Outline colors
    outline = BorderLight,
    outlineVariant = BorderMediumLight,
    
    // Scrim
    scrim = ScrimLight
)

/**
 * Dark Color Scheme for FairSplit
 * Based on extracted design tokens from UI mockups
 */
private val DarkColorScheme = darkColorScheme(
    // Primary colors
    primary = PrimaryLight,
    onPrimary = BackgroundDark,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = TextPrimaryDark,
    
    // Secondary colors (using Positive green as secondary)
    secondary = PositiveLight,
    onSecondary = BackgroundDark,
    secondaryContainer = PositiveDark,
    onSecondaryContainer = TextPrimaryDark,
    
    // Tertiary colors (using Info blue as tertiary)
    tertiary = InfoLight,
    onTertiary = BackgroundDark,
    tertiaryContainer = InfoDark,
    onTertiaryContainer = TextPrimaryDark,
    
    // Background colors
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    
    // Surface colors
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceTint = PrimaryLight,
    
    // Inverse colors
    inverseSurface = SurfaceLight,
    inverseOnSurface = TextPrimaryLight,
    inversePrimary = Primary,
    
    // Error colors
    error = NegativeLight,
    onError = BackgroundDark,
    errorContainer = NegativeDark,
    onErrorContainer = TextPrimaryDark,
    
    // Outline colors
    outline = BorderDark,
    outlineVariant = BorderMediumDark,
    
    // Scrim
    scrim = ScrimDark
)

/**
 * FairSplit Theme Composable
 * 
 * Main theme wrapper for the application.
 * Applies Material3 theming with custom FairSplit design tokens.
 * 
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param dynamicColor Whether to use dynamic color (Material You) - disabled for FairSplit
 * @param content The content to apply the theme to
 */
@Composable
fun FairSplitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled to maintain brand consistency
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Note: Dynamic color is intentionally disabled to maintain FairSplit's brand identity
        // If you want to enable it in the future, uncomment the following lines:
        // dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //     val context = LocalContext.current
        //     if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        // }
        
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FairSplitTypography,
        shapes = FairSplitShapes,
        content = content
    )
}

/**
 * Preview-friendly theme wrapper for Compose previews
 */
@Composable
fun FairSplitThemePreview(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = FairSplitTypography,
        shapes = FairSplitShapes,
        content = content
    )
}
