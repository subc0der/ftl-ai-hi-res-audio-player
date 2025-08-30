package com.ftl.hires.audioplayer.presentation.screens.equalizer.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ftl.hires.audioplayer.audio.equalizer.EQMode
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import kotlinx.coroutines.launch

/**
 * Individual EQ Mode Chip with cyberpunk glow effects
 * 
 * Features:
 * - Animated glow when selected
 * - Pulsing animation for active mode
 * - Haptic feedback on interaction
 * - Mode-specific color theming
 */
@Composable
fun EQModeChip(
    mode: EQMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBandCount: Boolean = true,
    enableGlow: Boolean = true,
    enableHaptics: Boolean = true
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animation states
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f
            isSelected -> 1.05f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "chip_scale"
    )
    
    // Glow animation for selected state
    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.6f else 0f,
        animationSpec = tween(300),
        label = "glow_alpha"
    )
    
    // Pulsing animation for selected chip
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    // Get mode-specific color
    val modeColor = when (mode) {
        is EQMode.Simple5Band -> SubcoderColors.Orange
        is EQMode.Standard10Band -> SubcoderColors.Cyan
        is EQMode.Advanced20Band -> SubcoderColors.NeonGreen
        is EQMode.Pro32Band -> SubcoderColors.ElectricBlue
    }
    
    val backgroundColor = if (isSelected) {
        modeColor.copy(alpha = 0.2f)
    } else {
        SubcoderColors.DarkGrey.copy(alpha = 0.3f)
    }
    
    val borderColor = if (isSelected) {
        modeColor.copy(alpha = pulseAlpha)
    } else {
        SubcoderColors.MediumGrey.copy(alpha = 0.5f)
    }
    
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .drawBehind {
                // Draw glow effect behind the chip
                if (enableGlow && isSelected) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                modeColor.copy(alpha = glowAlpha * 0.5f),
                                modeColor.copy(alpha = glowAlpha * 0.2f),
                                Color.Transparent
                            ),
                            radius = size.maxDimension
                        )
                    )
                }
            }
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (enableHaptics) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Mode name
            Text(
                text = mode.displayName.uppercase(),
                fontSize = 12.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) modeColor else SubcoderColors.LightGrey,
                textAlign = TextAlign.Center
            )
            
            if (showBandCount) {
                Spacer(modifier = Modifier.height(4.dp))
                
                // Band count with animated color
                AnimatedContent(
                    targetState = mode.bandCount,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) with
                        fadeOut(animationSpec = tween(300))
                    },
                    label = "band_count"
                ) { bandCount ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = bandCount.toString(),
                            fontSize = 18.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) modeColor else SubcoderColors.OffWhite
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "BANDS",
                            fontSize = 8.sp,
                            fontFamily = OrbitronFontFamily,
                            color = if (isSelected) modeColor.copy(alpha = 0.8f) else SubcoderColors.LightGrey
                        )
                    }
                }
            }
            
            // Optional mode description for selected chip
            AnimatedVisibility(
                visible = isSelected,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mode.description,
                        fontSize = 9.sp,
                        color = SubcoderColors.LightGrey,
                        textAlign = TextAlign.Center,
                        lineHeight = 11.sp
                    )
                }
            }
        }
        
        // Animated glow overlay for selected state
        if (enableGlow && isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(8.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                modeColor.copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

/**
 * Compact version of EQModeChip for limited space
 */
@Composable
fun CompactEQModeChip(
    mode: EQMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    val modeColor = when (mode) {
        is EQMode.Simple5Band -> SubcoderColors.Orange
        is EQMode.Standard10Band -> SubcoderColors.Cyan
        is EQMode.Advanced20Band -> SubcoderColors.NeonGreen
        is EQMode.Pro32Band -> SubcoderColors.ElectricBlue
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "compact_scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isSelected) {
                    modeColor.copy(alpha = 0.3f)
                } else {
                    SubcoderColors.DarkGrey.copy(alpha = 0.3f)
                }
            )
            .border(
                width = 1.dp,
                color = if (isSelected) modeColor else SubcoderColors.MediumGrey,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${mode.bandCount}",
                fontSize = 14.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) modeColor else SubcoderColors.OffWhite
            )
            Text(
                text = "B",
                fontSize = 10.sp,
                fontFamily = OrbitronFontFamily,
                color = if (isSelected) modeColor else SubcoderColors.LightGrey,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

/**
 * Preview helper for EQModeChip
 */
@Composable
fun PreviewEQModeChip() {
    Column(
        modifier = Modifier
            .background(SubcoderColors.PureBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EQModeChip(
            mode = EQMode.Pro32Band,
            isSelected = true,
            onClick = {}
        )
        
        EQModeChip(
            mode = EQMode.Standard10Band,
            isSelected = false,
            onClick = {}
        )
        
        CompactEQModeChip(
            mode = EQMode.Simple5Band,
            isSelected = true,
            onClick = {}
        )
    }
}