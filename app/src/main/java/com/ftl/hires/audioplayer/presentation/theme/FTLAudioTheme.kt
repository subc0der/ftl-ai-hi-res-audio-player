package com.ftl.hires.audioplayer.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * FTL Hi-Res Audio Player - Subcoder Cipher Matrix Theme
 * 
 * Cyberpunk aesthetic with:
 * - Primary: Cyan #00FFFF (Matrix green alternative)
 * - Secondary: Orange #FF6600 (Warning/accent color)
 * - Background: Pure Black #000000 (Deep space/matrix background)
 * - Dark theme by default for audiophile experience
 */

// Subcoder Cipher Matrix Color Palette
object SubcoderColors {
    val Cyan = Color(0xFF00FFFF)           // Primary matrix cyan
    val CyanVariant = Color(0xFF00CCCC)    // Darker cyan variant
    val Orange = Color(0xFFFF6600)         // Orange accent
    val OrangeVariant = Color(0xFFCC5500)  // Darker orange variant
    val PureBlack = Color(0xFF000000)      // Pure black background
    val MatrixBlack = Color(0xFF001100)    // Subtle green-tinted black
    val DarkGrey = Color(0xFF111111)       // Very dark grey
    val MediumGrey = Color(0xFF333333)     // Medium grey for surfaces
    val LightGrey = Color(0xFF666666)      // Light grey for disabled elements
    val NeonGreen = Color(0xFF00FF00)      // Classic matrix green
    val ElectricBlue = Color(0xFF0080FF)   // Electric blue accent
    val White = Color(0xFFFFFFFF)          // Pure white for contrast
    val OffWhite = Color(0xFFE0E0E0)       // Slightly dimmed white
    val ErrorRed = Color(0xFFFF4444)       // Error state color
    val WarningYellow = Color(0xFFFFDD00)  // Warning state color
    val SuccessGreen = Color(0xFF44FF44)   // Success state color
}

// Dark Theme Color Scheme (Default for Cyberpunk Aesthetic)
private val DarkColorScheme = darkColorScheme(
    // Primary colors - Cyan matrix theme
    primary = SubcoderColors.Cyan,
    onPrimary = SubcoderColors.PureBlack,
    primaryContainer = SubcoderColors.CyanVariant,
    onPrimaryContainer = SubcoderColors.White,
    
    // Secondary colors - Orange accents
    secondary = SubcoderColors.Orange,
    onSecondary = SubcoderColors.PureBlack,
    secondaryContainer = SubcoderColors.OrangeVariant,
    onSecondaryContainer = SubcoderColors.White,
    
    // Tertiary colors - Electric blue
    tertiary = SubcoderColors.ElectricBlue,
    onTertiary = SubcoderColors.PureBlack,
    tertiaryContainer = SubcoderColors.ElectricBlue.copy(alpha = 0.3f),
    onTertiaryContainer = SubcoderColors.White,
    
    // Background colors - Pure black matrix
    background = SubcoderColors.PureBlack,
    onBackground = SubcoderColors.OffWhite,
    
    // Surface colors - Dark grey variations
    surface = SubcoderColors.DarkGrey,
    onSurface = SubcoderColors.OffWhite,
    surfaceVariant = SubcoderColors.MediumGrey,
    onSurfaceVariant = SubcoderColors.LightGrey,
    
    // Additional surface colors
    surfaceTint = SubcoderColors.Cyan,
    inverseSurface = SubcoderColors.White,
    inverseOnSurface = SubcoderColors.PureBlack,
    
    // Error colors
    error = SubcoderColors.ErrorRed,
    onError = SubcoderColors.White,
    errorContainer = SubcoderColors.ErrorRed.copy(alpha = 0.3f),
    onErrorContainer = SubcoderColors.White,
    
    // Outline colors
    outline = SubcoderColors.LightGrey,
    outlineVariant = SubcoderColors.MediumGrey,
    
    // Scrim
    scrim = SubcoderColors.PureBlack.copy(alpha = 0.8f),
    
    // Inverse primary
    inversePrimary = SubcoderColors.CyanVariant
)

// Light Theme Color Scheme (Alternative for non-cyberpunk mode)
private val LightColorScheme = lightColorScheme(
    // Primary colors - Cyan but adjusted for light theme
    primary = SubcoderColors.CyanVariant,
    onPrimary = SubcoderColors.White,
    primaryContainer = SubcoderColors.Cyan.copy(alpha = 0.2f),
    onPrimaryContainer = SubcoderColors.CyanVariant,
    
    // Secondary colors - Orange adjusted for light theme
    secondary = SubcoderColors.OrangeVariant,
    onSecondary = SubcoderColors.White,
    secondaryContainer = SubcoderColors.Orange.copy(alpha = 0.2f),
    onSecondaryContainer = SubcoderColors.OrangeVariant,
    
    // Tertiary colors - Electric blue adjusted
    tertiary = SubcoderColors.ElectricBlue,
    onTertiary = SubcoderColors.White,
    tertiaryContainer = SubcoderColors.ElectricBlue.copy(alpha = 0.2f),
    onTertiaryContainer = SubcoderColors.ElectricBlue,
    
    // Background colors - Light theme
    background = SubcoderColors.White,
    onBackground = SubcoderColors.PureBlack,
    
    // Surface colors - Light variations
    surface = Color(0xFFF8F8F8),
    onSurface = SubcoderColors.PureBlack,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = SubcoderColors.MediumGrey,
    
    // Additional surface colors
    surfaceTint = SubcoderColors.CyanVariant,
    inverseSurface = SubcoderColors.DarkGrey,
    inverseOnSurface = SubcoderColors.White,
    
    // Error colors
    error = SubcoderColors.ErrorRed,
    onError = SubcoderColors.White,
    errorContainer = SubcoderColors.ErrorRed.copy(alpha = 0.1f),
    onErrorContainer = SubcoderColors.ErrorRed,
    
    // Outline colors
    outline = SubcoderColors.MediumGrey,
    outlineVariant = SubcoderColors.LightGrey,
    
    // Scrim
    scrim = SubcoderColors.PureBlack.copy(alpha = 0.5f),
    
    // Inverse primary
    inversePrimary = SubcoderColors.Cyan
)

/**
 * Custom colors for specific FTL Audio Player components
 */
object FTLAudioColors {
    // Waveform visualization colors
    val WaveformActive = SubcoderColors.Cyan
    val WaveformInactive = SubcoderColors.MediumGrey
    val WaveformBackground = SubcoderColors.PureBlack
    
    // EQ band colors (32-band parametric EQ)
    val EqBandLow = SubcoderColors.Orange      // Low frequencies
    val EqBandMid = SubcoderColors.Cyan        // Mid frequencies
    val EqBandHigh = SubcoderColors.NeonGreen  // High frequencies
    
    // Audio format indicators
    val HighResIndicator = SubcoderColors.NeonGreen    // Hi-res formats (FLAC, DSD)
    val LosslessIndicator = SubcoderColors.Cyan        // Lossless formats
    val CompressedIndicator = SubcoderColors.Orange    // Compressed formats
    
    // Player state colors
    val PlayingState = SubcoderColors.SuccessGreen
    val PausedState = SubcoderColors.WarningYellow
    val StoppedState = SubcoderColors.LightGrey
    val ErrorState = SubcoderColors.ErrorRed
    
    // Visualizer colors
    val SpectrumAnalyzer = SubcoderColors.Cyan
    val VuMeter = SubcoderColors.Orange
    val PhaseScope = SubcoderColors.ElectricBlue
    
    // Matrix-style text effects
    val MatrixTextPrimary = SubcoderColors.NeonGreen
    val MatrixTextSecondary = SubcoderColors.Cyan
    val MatrixTextFading = SubcoderColors.LightGrey
}

/**
 * Main theme composable for FTL Hi-Res Audio Player
 */
@Composable
fun FTLAudioPlayerTheme(
    darkTheme: Boolean = true, // Default to dark theme for cyberpunk aesthetic
    dynamicColor: Boolean = false, // Disable dynamic color to maintain cyberpunk theme
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
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SubcoderTypography,
        shapes = SubcoderShapes,
        content = content
    )
}

/**
 * Alternative theme name for backward compatibility
 */
@Composable
fun SubcoderCipherMatrixTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FTLAudioPlayerTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}

/**
 * Force dark theme variant for maximum cyberpunk experience
 */
@Composable
fun FTLAudioPlayerDarkTheme(
    content: @Composable () -> Unit
) {
    FTLAudioPlayerTheme(
        darkTheme = true,
        dynamicColor = false,
        content = content
    )
}

/**
 * Extension functions for color accessibility
 */
@Composable
fun ColorScheme.audioPlayerPrimary() = SubcoderColors.Cyan

@Composable
fun ColorScheme.audioPlayerSecondary() = SubcoderColors.Orange

@Composable
fun ColorScheme.audioPlayerBackground() = SubcoderColors.PureBlack

@Composable
fun ColorScheme.matrixGreen() = SubcoderColors.NeonGreen

@Composable
fun ColorScheme.electricBlue() = SubcoderColors.ElectricBlue

/**
 * Preview helper for theme development
 */
object ThemePreview {
    val AllColors = listOf(
        "Cyan" to SubcoderColors.Cyan,
        "Orange" to SubcoderColors.Orange,
        "Pure Black" to SubcoderColors.PureBlack,
        "Neon Green" to SubcoderColors.NeonGreen,
        "Electric Blue" to SubcoderColors.ElectricBlue,
        "Error Red" to SubcoderColors.ErrorRed,
        "Success Green" to SubcoderColors.SuccessGreen,
        "Warning Yellow" to SubcoderColors.WarningYellow
    )
}