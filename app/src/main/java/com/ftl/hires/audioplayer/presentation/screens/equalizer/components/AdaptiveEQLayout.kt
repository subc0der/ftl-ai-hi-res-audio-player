package com.ftl.hires.audioplayer.presentation.screens.equalizer.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ftl.hires.audioplayer.audio.AudioController
import com.ftl.hires.audioplayer.audio.equalizer.EQMode
import com.ftl.hires.audioplayer.audio.equalizer.EqualizerBand
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

/**
 * Screen size categories for adaptive layouts
 */
enum class ScreenSizeCategory {
    COMPACT,    // < 600dp width (phones in portrait)
    STANDARD,   // 600-840dp width (tablets, foldables)
    EXPANDED    // > 840dp width (tablets in landscape, desktop)
}

/**
 * Determine current screen size category
 */
@Composable
fun rememberScreenSizeCategory(): ScreenSizeCategory {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    return when {
        screenWidth < 600.dp -> ScreenSizeCategory.COMPACT
        screenWidth < 840.dp -> ScreenSizeCategory.STANDARD
        else -> ScreenSizeCategory.EXPANDED
    }
}

/**
 * Main adaptive EQ layout that adjusts based on screen size
 */
@Composable
fun AdaptiveEQLayout(
    audioController: AudioController,
    modifier: Modifier = Modifier
) {
    val screenSize = rememberScreenSizeCategory()
    val currentMode by audioController.currentEQMode.collectAsStateWithLifecycle()
    val equalizerBands by audioController.equalizerBands.collectAsStateWithLifecycle()
    val isEqualizerEnabled by audioController.isEqualizerEnabled.collectAsStateWithLifecycle()
    
    AnimatedContent(
        targetState = screenSize,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) with
            fadeOut(animationSpec = tween(300))
        },
        label = "adaptive_layout"
    ) { size ->
        when (size) {
            ScreenSizeCategory.COMPACT -> CompactEQLayout(
                currentMode = currentMode,
                equalizerBands = equalizerBands,
                isEnabled = isEqualizerEnabled,
                onModeSelected = { audioController.switchEQMode(it) },
                onBandChange = { band, gain -> 
                    audioController.updateEqualizerBand(band.id, gain)
                },
                modifier = modifier
            )
            
            ScreenSizeCategory.STANDARD -> StandardEQLayout(
                currentMode = currentMode,
                equalizerBands = equalizerBands,
                isEnabled = isEqualizerEnabled,
                onModeSelected = { audioController.switchEQMode(it) },
                onBandChange = { band, gain -> 
                    audioController.updateEqualizerBand(band.id, gain)
                },
                modifier = modifier
            )
            
            ScreenSizeCategory.EXPANDED -> ExpandedEQLayout(
                currentMode = currentMode,
                equalizerBands = equalizerBands,
                isEnabled = isEqualizerEnabled,
                onModeSelected = { audioController.switchEQMode(it) },
                onBandChange = { band, gain -> 
                    audioController.updateEqualizerBand(band.id, gain)
                },
                modifier = modifier
            )
        }
    }
}

/**
 * Compact layout for phones in portrait mode
 * - Vertical scrolling
 * - Dropdown mode selector
 * - Sliders in scrollable rows or grid
 */
@Composable
fun CompactEQLayout(
    currentMode: EQMode,
    equalizerBands: List<EqualizerBand>,
    isEnabled: Boolean,
    onModeSelected: (EQMode) -> Unit,
    onBandChange: (EqualizerBand, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SubcoderColors.PureBlack)
            .padding(16.dp)
    ) {
        // Compact header with dropdown selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AUDIO MATRIX",
                style = MaterialTheme.typography.titleMedium,
                color = if (isEnabled) SubcoderColors.ElectricBlue else SubcoderColors.LightGrey
            )
            
            DropdownEQModeSelector(
                currentMode = currentMode,
                onModeSelected = onModeSelected
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Adaptive band display based on count
        when {
            currentMode.bandCount <= 10 -> {
                // Single row with horizontal scroll for few bands
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(equalizerBands) { band ->
                        CompactEqualizerSlider(
                            band = band,
                            onValueChange = { onBandChange(band, it) },
                            isEnabled = isEnabled,
                            modeColor = getModeColor(currentMode)
                        )
                    }
                }
            }
            
            currentMode.bandCount <= 20 -> {
                // Two-row grid for medium band counts
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(equalizerBands) { band ->
                        CompactEqualizerSlider(
                            band = band,
                            onValueChange = { onBandChange(band, it) },
                            isEnabled = isEnabled,
                            modeColor = getModeColor(currentMode),
                            showFrequency = false
                        )
                    }
                }
            }
            
            else -> {
                // Scrollable grid for many bands
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(equalizerBands) { band ->
                        MiniEqualizerSlider(
                            band = band,
                            onValueChange = { onBandChange(band, it) },
                            isEnabled = isEnabled,
                            modeColor = getModeColor(currentMode)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Standard layout for tablets and foldables
 * - Side panel for mode selection
 * - Larger slider area
 */
@Composable
fun StandardEQLayout(
    currentMode: EQMode,
    equalizerBands: List<EqualizerBand>,
    isEnabled: Boolean,
    onModeSelected: (EQMode) -> Unit,
    onBandChange: (EqualizerBand, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(SubcoderColors.PureBlack)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left panel with mode selector
        Column(
            modifier = Modifier.width(200.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "EQ MODES",
                style = MaterialTheme.typography.titleSmall,
                color = SubcoderColors.Cyan
            )
            
            val modes = listOf(
                EQMode.Simple5Band,
                EQMode.Standard10Band,
                EQMode.Advanced20Band,
                EQMode.Pro32Band
            )
            
            modes.forEach { mode ->
                EQModeChip(
                    mode = mode,
                    isSelected = mode == currentMode,
                    onClick = { onModeSelected(mode) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Main EQ area
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = SubcoderColors.DarkGrey.copy(alpha = 0.2f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Adaptive grid based on band count
                val columns = when {
                    currentMode.bandCount <= 10 -> currentMode.bandCount
                    currentMode.bandCount <= 20 -> 10
                    else -> 16
                }
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(equalizerBands) { band ->
                        StandardEqualizerSlider(
                            band = band,
                            onValueChange = { onBandChange(band, it) },
                            isEnabled = isEnabled,
                            modeColor = getModeColor(currentMode)
                        )
                    }
                }
                
                // Zero dB reference line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.3f))
                        .align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * Expanded layout for large screens
 * - Full mode selector with details
 * - Maximum slider size
 * - Additional controls and visualizations
 */
@Composable
fun ExpandedEQLayout(
    currentMode: EQMode,
    equalizerBands: List<EqualizerBand>,
    isEnabled: Boolean,
    onModeSelected: (EQMode) -> Unit,
    onBandChange: (EqualizerBand, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SubcoderColors.PureBlack)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Full mode selector at top
        EQModeSelector(
            currentMode = currentMode,
            onModeSelected = onModeSelected,
            showCurrentModeDetails = true
        )
        
        // Main EQ display with maximum space
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = SubcoderColors.DarkGrey.copy(alpha = 0.15f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Full-size sliders in horizontal scroll
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        when {
                            currentMode.bandCount <= 10 -> 24.dp
                            currentMode.bandCount <= 20 -> 16.dp
                            else -> 12.dp
                        }
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(equalizerBands) { band ->
                        ExpandedEqualizerSlider(
                            band = band,
                            onValueChange = { onBandChange(band, it) },
                            isEnabled = isEnabled,
                            modeColor = getModeColor(currentMode)
                        )
                    }
                }
                
                // Zero dB reference line with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                        .align(Alignment.Center)
                )
            }
        }
        
        // Additional controls at bottom
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Bands: ${currentMode.bandCount}",
                color = getModeColor(currentMode)
            )
            Text(
                text = "Range: ${equalizerBands.firstOrNull()?.frequency ?: 0}Hz - ${equalizerBands.lastOrNull()?.frequency ?: 20000}Hz",
                color = SubcoderColors.LightGrey
            )
            Text(
                text = if (isEnabled) "EQ ACTIVE" else "EQ BYPASSED",
                color = if (isEnabled) SubcoderColors.SuccessGreen else SubcoderColors.WarningOrange
            )
        }
    }
}

/**
 * Get mode-specific color
 */
private fun getModeColor(mode: EQMode): Color {
    return when (mode) {
        is EQMode.Simple5Band -> SubcoderColors.Orange
        is EQMode.Standard10Band -> SubcoderColors.Cyan
        is EQMode.Advanced20Band -> SubcoderColors.NeonGreen
        is EQMode.Pro32Band -> SubcoderColors.ElectricBlue
    }
}

/**
 * Compact equalizer slider for small screens
 */
@Composable
private fun CompactEqualizerSlider(
    band: EqualizerBand,
    onValueChange: (Float) -> Unit,
    isEnabled: Boolean,
    modeColor: Color,
    showFrequency: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(60.dp)
    ) {
        Slider(
            value = band.gain,
            onValueChange = onValueChange,
            valueRange = EqualizerBand.MIN_GAIN..EqualizerBand.MAX_GAIN,
            enabled = isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = modeColor,
                activeTrackColor = modeColor.copy(alpha = 0.6f),
                inactiveTrackColor = SubcoderColors.MediumGrey
            ),
            modifier = Modifier
                .height(120.dp)
                .graphicsLayer { rotationZ = -90f }
        )
        
        if (showFrequency) {
            Text(
                text = formatFrequency(band.frequency),
                style = MaterialTheme.typography.labelSmall,
                color = SubcoderColors.LightGrey
            )
        }
    }
}

/**
 * Mini equalizer slider for very compact layouts
 */
@Composable
private fun MiniEqualizerSlider(
    band: EqualizerBand,
    onValueChange: (Float) -> Unit,
    isEnabled: Boolean,
    modeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(40.dp)
            .height(80.dp)
    ) {
        Slider(
            value = band.gain,
            onValueChange = onValueChange,
            valueRange = EqualizerBand.MIN_GAIN..EqualizerBand.MAX_GAIN,
            enabled = isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = modeColor,
                activeTrackColor = modeColor.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = -90f }
        )
    }
}

/**
 * Standard equalizer slider for medium screens
 */
@Composable
private fun StandardEqualizerSlider(
    band: EqualizerBand,
    onValueChange: (Float) -> Unit,
    isEnabled: Boolean,
    modeColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Slider(
            value = band.gain,
            onValueChange = onValueChange,
            valueRange = EqualizerBand.MIN_GAIN..EqualizerBand.MAX_GAIN,
            enabled = isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = modeColor,
                activeTrackColor = modeColor.copy(alpha = 0.7f),
                inactiveTrackColor = SubcoderColors.MediumGrey
            ),
            modifier = Modifier
                .height(150.dp)
                .graphicsLayer { rotationZ = -90f }
        )
        
        Text(
            text = formatFrequency(band.frequency),
            style = MaterialTheme.typography.labelMedium,
            color = modeColor.copy(alpha = 0.8f)
        )
    }
}

/**
 * Expanded equalizer slider for large screens
 */
@Composable
private fun ExpandedEqualizerSlider(
    band: EqualizerBand,
    onValueChange: (Float) -> Unit,
    isEnabled: Boolean,
    modeColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(80.dp)
    ) {
        Text(
            text = "${band.gain.toInt()}dB",
            style = MaterialTheme.typography.labelLarge,
            color = modeColor
        )
        
        Slider(
            value = band.gain,
            onValueChange = onValueChange,
            valueRange = EqualizerBand.MIN_GAIN..EqualizerBand.MAX_GAIN,
            enabled = isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = modeColor,
                activeTrackColor = modeColor.copy(alpha = 0.8f),
                inactiveTrackColor = SubcoderColors.MediumGrey.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .height(200.dp)
                .graphicsLayer { rotationZ = -90f }
        )
        
        Text(
            text = formatFrequency(band.frequency),
            style = MaterialTheme.typography.bodyMedium,
            color = modeColor
        )
    }
}

/**
 * Format frequency for display
 */
private fun formatFrequency(frequency: Int): String {
    return when {
        frequency >= 1000 -> "${frequency / 1000}k"
        else -> "$frequency"
    }
}