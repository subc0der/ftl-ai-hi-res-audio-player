package com.ftl.hires.audioplayer.presentation.screens.equalizer.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ftl.hires.audioplayer.audio.equalizer.EQMode
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import kotlinx.coroutines.launch

/**
 * Main EQ Mode Selector with animated chip selection
 * 
 * Features:
 * - Horizontal scrollable chip layout
 * - Smooth animations between modes
 * - Adaptive layout based on screen size
 * - Current mode indicator with details
 */
@Composable
fun EQModeSelector(
    currentMode: EQMode,
    onModeSelected: (EQMode) -> Unit,
    modifier: Modifier = Modifier,
    showCurrentModeDetails: Boolean = true,
    enableAnimations: Boolean = true
) {
    val modes = remember {
        listOf(
            EQMode.Simple5Band,
            EQMode.Standard10Band,
            EQMode.Advanced20Band,
            EQMode.Pro32Band
        )
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Current mode indicator with details
        if (showCurrentModeDetails) {
            CurrentModeIndicator(
                currentMode = currentMode,
                enableAnimations = enableAnimations
            )
        }
        
        // Mode selection chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modes.forEach { mode ->
                EQModeChip(
                    mode = mode,
                    isSelected = mode == currentMode,
                    onClick = { onModeSelected(mode) },
                    enableGlow = enableAnimations
                )
            }
        }
    }
}

/**
 * Current mode indicator with animated details
 */
@Composable
private fun CurrentModeIndicator(
    currentMode: EQMode,
    enableAnimations: Boolean,
    modifier: Modifier = Modifier
) {
    val modeColor = when (currentMode) {
        is EQMode.Simple5Band -> SubcoderColors.Orange
        is EQMode.Standard10Band -> SubcoderColors.Cyan
        is EQMode.Advanced20Band -> SubcoderColors.NeonGreen
        is EQMode.Pro32Band -> SubcoderColors.ElectricBlue
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SubcoderColors.DarkGrey.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            modeColor.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (currentMode) {
                                is EQMode.Simple5Band -> Icons.Default.GraphicEq
                                is EQMode.Standard10Band -> Icons.Default.Equalizer
                                is EQMode.Advanced20Band -> Icons.Default.Tune
                                is EQMode.Pro32Band -> Icons.Default.Settings
                            },
                            contentDescription = null,
                            tint = modeColor,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Text(
                            text = "ACTIVE MODE",
                            fontSize = 10.sp,
                            fontFamily = OrbitronFontFamily,
                            color = SubcoderColors.LightGrey
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    AnimatedContent(
                        targetState = currentMode,
                        transitionSpec = {
                            if (enableAnimations) {
                                (fadeIn(animationSpec = tween(300)) +
                                slideInVertically { height -> height }) with
                                (fadeOut(animationSpec = tween(300)) +
                                slideOutVertically { height -> -height })
                            } else {
                                EnterTransition.None with ExitTransition.None
                            }
                        },
                        label = "mode_name"
                    ) { mode ->
                        Text(
                            text = mode.displayName.uppercase(),
                            fontSize = 16.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = modeColor
                        )
                    }
                }
                
                // Band count badge
                AnimatedContent(
                    targetState = currentMode.bandCount,
                    transitionSpec = {
                        if (enableAnimations) {
                            (fadeIn(animationSpec = tween(300)) +
                            scaleIn(initialScale = 0.8f)) with
                            (fadeOut(animationSpec = tween(300)) +
                            scaleOut(targetScale = 1.2f))
                        } else {
                            EnterTransition.None with ExitTransition.None
                        }
                    },
                    label = "band_count_badge"
                ) { bandCount ->
                    Box(
                        modifier = Modifier
                            .background(
                                modeColor.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = bandCount.toString(),
                                fontSize = 24.sp,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = modeColor
                            )
                            Text(
                                text = "BANDS",
                                fontSize = 10.sp,
                                fontFamily = OrbitronFontFamily,
                                color = modeColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact mode selector for limited space (header/toolbar)
 */
@Composable
fun CompactEQModeSelector(
    currentMode: EQMode,
    onModeSelected: (EQMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = remember {
        listOf(
            EQMode.Simple5Band,
            EQMode.Standard10Band,
            EQMode.Advanced20Band,
            EQMode.Pro32Band
        )
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        modes.forEach { mode ->
            CompactEQModeChip(
                mode = mode,
                isSelected = mode == currentMode,
                onClick = { onModeSelected(mode) }
            )
        }
    }
}

/**
 * Dropdown style mode selector
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownEQModeSelector(
    currentMode: EQMode,
    onModeSelected: (EQMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val modes = remember {
        listOf(
            EQMode.Simple5Band,
            EQMode.Standard10Band,
            EQMode.Advanced20Band,
            EQMode.Pro32Band
        )
    }
    
    val modeColor = when (currentMode) {
        is EQMode.Simple5Band -> SubcoderColors.Orange
        is EQMode.Standard10Band -> SubcoderColors.Cyan
        is EQMode.Advanced20Band -> SubcoderColors.NeonGreen
        is EQMode.Pro32Band -> SubcoderColors.ElectricBlue
    }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.menuAnchor(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = modeColor
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(
                    colors = listOf(modeColor, modeColor.copy(alpha = 0.3f))
                )
            )
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${currentMode.bandCount}B",
                fontSize = 12.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SubcoderColors.DarkGrey)
        ) {
            modes.forEach { mode ->
                val itemColor = when (mode) {
                    is EQMode.Simple5Band -> SubcoderColors.Orange
                    is EQMode.Standard10Band -> SubcoderColors.Cyan
                    is EQMode.Advanced20Band -> SubcoderColors.NeonGreen
                    is EQMode.Pro32Band -> SubcoderColors.ElectricBlue
                }
                
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = mode.displayName,
                                    fontSize = 12.sp,
                                    fontFamily = OrbitronFontFamily,
                                    color = if (mode == currentMode) itemColor else SubcoderColors.OffWhite
                                )
                                Text(
                                    text = mode.description,
                                    fontSize = 10.sp,
                                    color = SubcoderColors.LightGrey
                                )
                            }
                            
                            Text(
                                text = "${mode.bandCount}B",
                                fontSize = 14.sp,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = itemColor
                            )
                        }
                    },
                    onClick = {
                        onModeSelected(mode)
                        expanded = false
                    },
                    modifier = Modifier.background(
                        if (mode == currentMode) {
                            itemColor.copy(alpha = 0.1f)
                        } else {
                            Color.Transparent
                        }
                    )
                )
            }
        }
    }
}