package com.ftl.hires.audioplayer.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pixel 9 Pro optimized dimensions
 * Screen: 6.3" OLED, 1280 x 2856 pixels
 * Aspect ratio: 20:9
 */
data class FTLDimensions(
    // Navigation & Bars
    val bottomNavHeight: Dp = 56.dp,
    val nowPlayingBarHeight: Dp = 72.dp,
    val topBarHeight: Dp = 56.dp,
    
    // Equalizer specific for Pixel 9 Pro - Enhanced for better touch interaction
    val eqSliderHeight: Dp = 180.dp,  // Increased from 140dp for even better control
    val eqSliderWidth: Dp = 24.dp,    // Increased from 18dp for easier touch
    val eqSliderSpacing: Dp = 2.dp,   // Slightly more spacing for 32 bands
    val eqHeaderHeight: Dp = 48.dp,
    val eqControlsHeight: Dp = 40.dp,
    
    // Library/List items
    val trackItemHeight: Dp = 64.dp,
    val albumItemSize: Dp = 140.dp,    // Grid items for albums
    val artistItemHeight: Dp = 72.dp,
    
    // Padding & Spacing
    val screenPaddingHorizontal: Dp = 12.dp,  // Reduced from 16dp
    val screenPaddingVertical: Dp = 8.dp,     // Reduced from 16dp
    val itemSpacing: Dp = 8.dp,
    val sectionSpacing: Dp = 16.dp,
    
    // Text sizes optimized for Pixel 9 Pro
    val titleLarge: TextUnit = 24.sp,
    val titleMedium: TextUnit = 18.sp,
    val titleSmall: TextUnit = 14.sp,
    val bodyLarge: TextUnit = 14.sp,
    val bodyMedium: TextUnit = 12.sp,
    val bodySmall: TextUnit = 11.sp,
    val labelLarge: TextUnit = 12.sp,
    val labelMedium: TextUnit = 10.sp,
    val labelSmall: TextUnit = 9.sp,
    
    // EQ specific text
    val eqFrequencyText: TextUnit = 7.sp,
    val eqGainText: TextUnit = 8.sp,
    
    // Button sizes
    val iconButtonSize: Dp = 40.dp,
    val smallIconButtonSize: Dp = 32.dp,
    val fabSize: Dp = 56.dp,
    
    // Card dimensions
    val cardElevation: Dp = 4.dp,
    val cardCornerRadius: Dp = 12.dp
)

val LocalDimensions = compositionLocalOf { FTLDimensions() }

/**
 * Pixel 9 Pro specific dimensions provider
 */
object Pixel9ProDimensions {
    val default = FTLDimensions()
    
    // Landscape adjustments if needed
    val landscape = FTLDimensions(
        bottomNavHeight = 48.dp,
        nowPlayingBarHeight = 60.dp,
        eqSliderHeight = 80.dp
    )
}