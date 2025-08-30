package com.ftl.hires.audioplayer.presentation.screens.equalizer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ftl.hires.audioplayer.audio.equalizer.EqualizerBand
import com.ftl.hires.audioplayer.audio.equalizer.EqualizerPreset
import com.ftl.hires.audioplayer.audio.equalizer.PresetCategory
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import com.ftl.hires.audioplayer.presentation.theme.FTLAudioTypography
import com.ftl.hires.audioplayer.presentation.theme.Pixel9ProDimensions
import com.ftl.hires.audioplayer.audio.AudioController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ftl.hires.audioplayer.presentation.screens.equalizer.components.EqualizerSlider
import com.ftl.hires.audioplayer.presentation.screens.equalizer.components.EQModeSelector
import com.ftl.hires.audioplayer.presentation.screens.equalizer.components.DropdownEQModeSelector
import com.ftl.hires.audioplayer.presentation.screens.equalizer.components.ModeSpecificPresetManager
import com.ftl.hires.audioplayer.presentation.screens.equalizer.components.EQModeTransitionLoader
import com.ftl.hires.audioplayer.audio.equalizer.ModeSpecificPresets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.tween

/**
 * Audio Matrix - 32-Band Parametric Equalizer Screen
 * 
 * Cyberpunk-themed professional EQ interface with:
 * - 32-band parametric control
 * - Built-in audiophile presets
 * - Real-time frequency response visualization
 * - Custom preset save/load
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioMatrixScreen(
    audioController: AudioController,
    onPresetSelected: (String) -> Unit = {},
    onCustomEQSaved: (Map<String, Float>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // EQ State from AudioController
    val equalizerBands by audioController.equalizerBands.collectAsStateWithLifecycle()
    val selectedPreset by audioController.activeEqualizerPreset.collectAsStateWithLifecycle()
    val isEqualizerEnabled by audioController.isEqualizerEnabled.collectAsStateWithLifecycle()
    val currentEQMode by audioController.currentEQMode.collectAsStateWithLifecycle()
    
    // UI State
    var isPresetMenuExpanded by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showModeSpecificPresets by remember { mutableStateOf(false) }
    var isModeTransitioning by remember { mutableStateOf(false) }
    
    val dimensions = Pixel9ProDimensions.default
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SubcoderColors.PureBlack)
            .padding(
                horizontal = dimensions.screenPaddingHorizontal,
                vertical = dimensions.screenPaddingVertical
            )
    ) {
        // Header with title and controls
        AudioMatrixHeader(
            selectedPreset = selectedPreset,
            currentEQMode = currentEQMode,
            isEnabled = isEqualizerEnabled,
            onPresetClick = { isPresetMenuExpanded = true },
            onSaveClick = { showSaveDialog = true },
            onResetClick = { audioController.resetEqualizer() },
            onToggleEnabled = { audioController.setEqualizerEnabled(!isEqualizerEnabled) },
            onModeSelected = { mode -> 
                try {
                    audioController.switchEQMode(mode)
                } catch (e: Exception) {
                    // Log error but don't crash
                    android.util.Log.e("AudioMatrix", "Failed to switch EQ mode", e)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Frequency categories legend
        FrequencyCategoriesLegend()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Main EQ Interface
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = SubcoderColors.DarkGrey.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp) // Reduced padding for Pixel 9 Pro
            ) {
                // 32-band EQ sliders - optimized for Pixel 9 Pro screen
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(dimensions.eqSliderSpacing),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(equalizerBands) { band ->
                        EqualizerSlider(
                            band = band,
                            onValueChange = { newGain ->
                                audioController.updateEqualizerBand(band.id, newGain)
                            },
                            isCompact = true
                        )
                    }
                }
                
                // Zero dB reference line overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.3f))
                        .align(Alignment.Center)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Mode-Specific Preset Manager (Temporarily disabled to fix crash)
        // TODO: Re-enable after fixing the crash issue
        /*
        AnimatedVisibility(
            visible = !isModeTransitioning && isEqualizerEnabled,
            enter = fadeIn(animationSpec = tween(300, delayMillis = 300)) + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            ModeSpecificPresetManager(
                currentMode = currentEQMode,
                selectedPreset = null,
                onPresetSelected = { preset ->
                    // Apply the mode-specific preset
                    val bandGains = preset.bands.zip(equalizerBands).map { (presetGain, band) ->
                        band.copy(gain = presetGain)
                    }
                    bandGains.forEach { band ->
                        audioController.updateEqualizerBand(band.id, band.gain)
                    }
                },
                showConversions = true,
                isLoading = isModeTransitioning
            )
        }
        */
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // EQ Info and Controls
        AudioMatrixControls(
            equalizerBands = equalizerBands,
            currentEQMode = currentEQMode,
            isEnabled = isEqualizerEnabled,
            onGlobalGainChange = { delta ->
                // Apply delta to all bands via AudioController
                equalizerBands.forEach { band ->
                    val newGain = (band.gain + delta).coerceIn(
                        EqualizerBand.MIN_GAIN,
                        EqualizerBand.MAX_GAIN
                    )
                    audioController.updateEqualizerBand(band.id, newGain)
                }
            }
        )
    }
    
    // Mode Transition Loading Overlay (Temporarily disabled to fix crash)
    // TODO: Re-enable after fixing the crash issue
    /*
    EQModeTransitionLoader(
        isVisible = isModeTransitioning,
        fromMode = null,
        toMode = currentEQMode,
        progress = if (isModeTransitioning) 0.8f else 0f
    )
    */
    
    // Preset Selection Menu
    if (isPresetMenuExpanded) {
        PresetSelectionMenu(
            presets = EqualizerPreset.BUILT_IN_PRESETS,
            onPresetSelected = { preset ->
                audioController.applyEqualizerPreset(preset)
                onPresetSelected(preset.name)
                isPresetMenuExpanded = false
            },
            onDismiss = { isPresetMenuExpanded = false }
        )
    }
    
    // Save Custom Preset Dialog
    if (showSaveDialog) {
        SavePresetDialog(
            currentBands = equalizerBands.map { it.gain },
            onSave = { presetName ->
                val eqMap = equalizerBands.associate { 
                    it.frequency.toString() to it.gain 
                }
                onCustomEQSaved(eqMap)
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false }
        )
    }
}

/**
 * Audio Matrix header with title and main controls
 */
@Composable
private fun AudioMatrixHeader(
    selectedPreset: EqualizerPreset?,
    currentEQMode: com.ftl.hires.audioplayer.audio.equalizer.EQMode,
    isEnabled: Boolean,
    onPresetClick: () -> Unit,
    onSaveClick: () -> Unit,
    onResetClick: () -> Unit,
    onToggleEnabled: () -> Unit,
    onModeSelected: (com.ftl.hires.audioplayer.audio.equalizer.EQMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top row with title and controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title section
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "AUDIO MATRIX",
                        style = FTLAudioTypography.librarySection,
                        color = if (isEnabled) SubcoderColors.ElectricBlue else SubcoderColors.LightGrey,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Enable/Disable toggle
                    IconButton(
                        onClick = onToggleEnabled,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isEnabled) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                            contentDescription = if (isEnabled) "Disable EQ" else "Enable EQ",
                            tint = if (isEnabled) SubcoderColors.ElectricBlue else SubcoderColors.LightGrey
                        )
                    }
                }
                
                Text(
                    text = if (isEnabled) {
                        selectedPreset?.name ?: "Custom EQ - ${currentEQMode.displayName}"
                    } else {
                        "EQ Disabled"
                    },
                    style = FTLAudioTypography.settingDescription,
                    color = SubcoderColors.LightGrey
                )
            }
            
            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Preset selector
                OutlinedButton(
                    onClick = onPresetClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SubcoderColors.Cyan
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PRESETS",
                        fontSize = 12.sp,
                        fontFamily = OrbitronFontFamily
                    )
                }
                
                // Save preset
                OutlinedButton(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SubcoderColors.Orange
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                // Reset
                OutlinedButton(
                    onClick = onResetClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SubcoderColors.WarningOrange
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        // EQ Mode Selector Row
        DropdownEQModeSelector(
            currentMode = currentEQMode,
            onModeSelected = onModeSelected,
            modifier = Modifier.align(Alignment.Start)
        )
    }
}

/**
 * Frequency categories legend
 */
@Composable
private fun FrequencyCategoriesLegend(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        com.ftl.hires.audioplayer.audio.equalizer.FrequencyCategory.values().forEach { category ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        category.color.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(category.color, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = category.displayName,
                    fontSize = 10.sp,
                    color = SubcoderColors.OffWhite,
                    fontFamily = OrbitronFontFamily
                )
            }
        }
    }
}

/**
 * EQ controls and info section
 */
@Composable
private fun AudioMatrixControls(
    equalizerBands: List<EqualizerBand>,
    currentEQMode: com.ftl.hires.audioplayer.audio.equalizer.EQMode,
    isEnabled: Boolean,
    onGlobalGainChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Global gain controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "GLOBAL:",
                fontSize = 12.sp,
                color = SubcoderColors.LightGrey,
                fontFamily = OrbitronFontFamily
            )
            
            IconButton(
                onClick = { onGlobalGainChange(-1f) },
                enabled = isEnabled,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease all bands",
                    tint = if (isEnabled) SubcoderColors.Cyan else SubcoderColors.LightGrey
                )
            }
            
            IconButton(
                onClick = { onGlobalGainChange(1f) },
                enabled = isEnabled,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase all bands",
                    tint = if (isEnabled) SubcoderColors.Cyan else SubcoderColors.LightGrey
                )
            }
        }
        
        // EQ stats
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${currentEQMode.bandCount} BANDS ACTIVE - ${currentEQMode.displayName.uppercase()}",
                fontSize = 10.sp,
                color = when (currentEQMode) {
                    is com.ftl.hires.audioplayer.audio.equalizer.EQMode.Simple5Band -> SubcoderColors.Orange
                    is com.ftl.hires.audioplayer.audio.equalizer.EQMode.Standard10Band -> SubcoderColors.Cyan
                    is com.ftl.hires.audioplayer.audio.equalizer.EQMode.Advanced20Band -> SubcoderColors.NeonGreen
                    is com.ftl.hires.audioplayer.audio.equalizer.EQMode.Pro32Band -> SubcoderColors.ElectricBlue
                },
                fontFamily = OrbitronFontFamily
            )
            Text(
                text = "Range: ${EqualizerBand.MIN_GAIN.toInt()}dB to +${EqualizerBand.MAX_GAIN.toInt()}dB",
                fontSize = 9.sp,
                color = SubcoderColors.LightGrey,
                fontFamily = OrbitronFontFamily
            )
        }
    }
}

/**
 * Preset selection menu overlay
 */
@Composable
private fun PresetSelectionMenu(
    presets: List<EqualizerPreset>,
    onPresetSelected: (EqualizerPreset) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Simple preset selection - could be enhanced with a modal or dropdown
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(
                SubcoderColors.DarkGrey.copy(alpha = 0.95f),
                RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "SELECT PRESET",
                style = FTLAudioTypography.settingTitle,
                color = SubcoderColors.ElectricBlue,
                fontFamily = OrbitronFontFamily
            )
        }
        
        presets.groupBy { it.category }.forEach { (category, categoryPresets) ->
            item {
                Column {
                    Text(
                        text = "${category.icon} ${category.displayName}",
                        fontSize = 14.sp,
                        color = SubcoderColors.Cyan,
                        fontFamily = OrbitronFontFamily,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    categoryPresets.forEach { preset ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPresetSelected(preset) },
                            colors = CardDefaults.cardColors(
                                containerColor = SubcoderColors.DarkGrey.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = preset.name,
                                    fontSize = 14.sp,
                                    color = SubcoderColors.OffWhite,
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = preset.description,
                                    fontSize = 12.sp,
                                    color = SubcoderColors.LightGrey
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
        
        item {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SubcoderColors.WarningOrange
                )
            ) {
                Text("CLOSE")
            }
        }
    }
}

/**
 * Save custom preset dialog
 */
@Composable
private fun SavePresetDialog(
    currentBands: List<Float>,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "SAVE PRESET",
                fontFamily = OrbitronFontFamily,
                color = SubcoderColors.ElectricBlue
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter a name for your custom EQ preset:",
                    color = SubcoderColors.LightGrey
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    placeholder = { Text("My Custom EQ") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(presetName.ifBlank { "Custom EQ" }) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SubcoderColors.ElectricBlue
                )
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = SubcoderColors.LightGrey)
            }
        },
        containerColor = SubcoderColors.DarkGrey
    )
}