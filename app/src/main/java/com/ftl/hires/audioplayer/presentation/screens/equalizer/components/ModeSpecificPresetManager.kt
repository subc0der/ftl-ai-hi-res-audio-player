package com.ftl.hires.audioplayer.presentation.screens.equalizer.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ftl.hires.audioplayer.audio.equalizer.EQMode
import com.ftl.hires.audioplayer.audio.equalizer.ModeSpecificPreset
import com.ftl.hires.audioplayer.audio.equalizer.ModeSpecificPresets
import com.ftl.hires.audioplayer.audio.equalizer.PresetCategory
import com.ftl.hires.audioplayer.audio.equalizer.PresetConverter
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Mode-Specific Preset Manager UI
 * 
 * Displays and manages presets tailored for each EQ mode with:
 * - Animated preset selection
 * - Smart preset conversion
 * - Loading states and transitions
 * - Category organization
 */
@Composable
fun ModeSpecificPresetManager(
    currentMode: EQMode,
    selectedPreset: ModeSpecificPreset?,
    onPresetSelected: (ModeSpecificPreset) -> Unit,
    onPresetConverted: (ModeSpecificPreset) -> Unit = {},
    modifier: Modifier = Modifier,
    showConversions: Boolean = true,
    isLoading: Boolean = false
) {
    var selectedCategory by remember { mutableStateOf<PresetCategory?>(null) }
    var showAllPresets by remember { mutableStateOf(false) }
    var isConverting by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val currentPresets = ModeSpecificPresets.getPresetsForMode(currentMode)
    val categories = currentPresets.groupBy { it.category }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SubcoderColors.DarkGrey.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with mode indicator
        PresetManagerHeader(
            currentMode = currentMode,
            presetCount = currentPresets.size,
            isLoading = isLoading
        )
        
        // Quick presets (recommended)
        AnimatedVisibility(
            visible = !showAllPresets,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            QuickPresetSelection(
                presets = ModeSpecificPresets.getRecommendedPresets(currentMode),
                selectedPreset = selectedPreset,
                onPresetSelected = onPresetSelected,
                isLoading = isLoading
            )
        }
        
        // Category tabs
        CategoryTabs(
            categories = categories.keys.toList(),
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                selectedCategory = if (selectedCategory == category) null else category
            },
            modeColor = currentMode.getModeColor()
        )
        
        // Preset grid by category
        AnimatedVisibility(
            visible = selectedCategory != null,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
            exit = fadeOut(animationSpec = tween(200)) + shrinkVertically()
        ) {
            selectedCategory?.let { category ->
                val categoryPresets = categories[category] ?: emptyList()
                PresetGrid(
                    presets = categoryPresets,
                    selectedPreset = selectedPreset,
                    onPresetSelected = onPresetSelected,
                    modeColor = currentMode.getModeColor(),
                    isLoading = isLoading
                )
            }
        }
        
        // Conversion section
        if (showConversions) {
            AnimatedVisibility(
                visible = !isLoading,
                enter = fadeIn(animationSpec = tween(300, delayMillis = 200)),
                exit = fadeOut()
            ) {
                PresetConversionSection(
                    targetMode = currentMode,
                    onPresetConverted = onPresetConverted,
                    isConverting = isConverting,
                    onConvertingChanged = { isConverting = it }
                )
            }
        }
        
        // Toggle for showing all presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = { showAllPresets = !showAllPresets },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = currentMode.getModeColor()
                )
            ) {
                Text(
                    text = if (showAllPresets) "SHOW RECOMMENDED" else "SHOW ALL PRESETS",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (showAllPresets) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Preset manager header with loading state
 */
@Composable
private fun PresetManagerHeader(
    currentMode: EQMode,
    presetCount: Int,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "MODE PRESETS",
                style = MaterialTheme.typography.titleMedium,
                color = currentMode.getModeColor(),
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold
            )
            
            AnimatedContent(
                targetState = if (isLoading) "Loading..." else "$presetCount presets for ${currentMode.displayName}",
                transitionSpec = {
                    fadeIn() with fadeOut()
                },
                label = "preset_count"
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = SubcoderColors.LightGrey
                )
            }
        }
        
        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = currentMode.getModeColor(),
                strokeWidth = 2.dp
            )
        }
    }
}

/**
 * Quick preset selection (recommended presets)
 */
@Composable
private fun QuickPresetSelection(
    presets: List<ModeSpecificPreset>,
    selectedPreset: ModeSpecificPreset?,
    onPresetSelected: (ModeSpecificPreset) -> Unit,
    isLoading: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "RECOMMENDED",
            fontSize = 12.sp,
            fontFamily = OrbitronFontFamily,
            color = SubcoderColors.Cyan,
            fontWeight = FontWeight.Medium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(presets) { preset ->
                PresetCard(
                    preset = preset,
                    isSelected = preset == selectedPreset,
                    onSelected = { onPresetSelected(preset) },
                    isLoading = isLoading,
                    isCompact = false
                )
            }
        }
    }
}

/**
 * Category tabs for preset organization
 */
@Composable
private fun CategoryTabs(
    categories: List<PresetCategory>,
    selectedCategory: PresetCategory?,
    onCategorySelected: (PresetCategory) -> Unit,
    modeColor: Color
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            
            OutlinedButton(
                onClick = { onCategorySelected(category) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) modeColor.copy(alpha = 0.2f) else Color.Transparent,
                    contentColor = if (isSelected) modeColor else SubcoderColors.LightGrey
                ),
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = category.icon,
                        fontSize = 12.sp
                    )
                    Text(
                        text = category.displayName,
                        fontSize = 10.sp,
                        fontFamily = OrbitronFontFamily
                    )
                }
            }
        }
    }
}

/**
 * Preset grid display
 */
@Composable
private fun PresetGrid(
    presets: List<ModeSpecificPreset>,
    selectedPreset: ModeSpecificPreset?,
    onPresetSelected: (ModeSpecificPreset) -> Unit,
    modeColor: Color,
    isLoading: Boolean
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 300.dp)
    ) {
        items(presets.chunked(2)) { presetPair ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presetPair.forEach { preset ->
                    PresetCard(
                        preset = preset,
                        isSelected = preset == selectedPreset,
                        onSelected = { onPresetSelected(preset) },
                        isLoading = isLoading,
                        modifier = Modifier.weight(1f),
                        isCompact = true
                    )
                }
                
                // Fill remaining space if odd number of presets
                if (presetPair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Individual preset card with animations
 */
@Composable
private fun PresetCard(
    preset: ModeSpecificPreset,
    isSelected: Boolean,
    onSelected: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    isCompact: Boolean = true
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "preset_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isLoading) 0.6f else 1f,
        label = "preset_alpha"
    )
    
    Card(
        onClick = {
            if (!isLoading) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onSelected()
            }
        },
        modifier = modifier
            .scale(scale)
            .alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                preset.modeColor.copy(alpha = 0.2f)
            } else {
                SubcoderColors.DarkGrey.copy(alpha = 0.3f)
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, preset.modeColor)
        } else {
            BorderStroke(1.dp, SubcoderColors.MediumGrey)
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Preset name
            Text(
                text = preset.name,
                fontSize = if (isCompact) 12.sp else 14.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) preset.modeColor else SubcoderColors.OffWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Description
            if (!isCompact || preset.description.length < 30) {
                Text(
                    text = preset.description,
                    fontSize = 10.sp,
                    color = SubcoderColors.LightGrey,
                    maxLines = if (isCompact) 2 else 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )
            }
            
            // Tags (if not compact)
            if (!isCompact && preset.tags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(preset.tags.take(3)) { tag ->
                        Surface(
                            color = preset.modeColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 8.sp,
                                color = preset.modeColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preset conversion section
 */
@Composable
private fun PresetConversionSection(
    targetMode: EQMode,
    onPresetConverted: (ModeSpecificPreset) -> Unit,
    isConverting: Boolean,
    onConvertingChanged: (Boolean) -> Unit
) {
    var showConversionDialog by remember { mutableStateOf(false) }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = SubcoderColors.MediumGrey.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PRESET CONVERSION",
                        fontSize = 10.sp,
                        fontFamily = OrbitronFontFamily,
                        color = SubcoderColors.Cyan
                    )
                    Text(
                        text = "Convert presets from other modes",
                        fontSize = 9.sp,
                        color = SubcoderColors.LightGrey
                    )
                }
                
                OutlinedButton(
                    onClick = { showConversionDialog = true },
                    enabled = !isConverting,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SubcoderColors.Orange
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    if (isConverting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.dp,
                            color = SubcoderColors.Orange
                        )
                    } else {
                        Text(
                            text = "CONVERT",
                            fontSize = 10.sp,
                            fontFamily = OrbitronFontFamily
                        )
                    }
                }
            }
        }
    }
    
    // Conversion dialog would be implemented here
    // This is a placeholder for the full conversion UI
}

/**
 * Extension function to get mode color
 */
private fun EQMode.getModeColor(): Color {
    return when (this) {
        is EQMode.Simple5Band -> SubcoderColors.Orange
        is EQMode.Standard10Band -> SubcoderColors.Cyan
        is EQMode.Advanced20Band -> SubcoderColors.NeonGreen
        is EQMode.Pro32Band -> SubcoderColors.ElectricBlue
    }
}