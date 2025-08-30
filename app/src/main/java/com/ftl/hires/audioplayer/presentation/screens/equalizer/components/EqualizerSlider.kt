package com.ftl.hires.audioplayer.presentation.screens.equalizer.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.ftl.hires.audioplayer.audio.equalizer.EqualizerBand
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import com.ftl.hires.audioplayer.presentation.theme.FTLDimensions
import com.ftl.hires.audioplayer.presentation.theme.Pixel9ProDimensions
import kotlin.math.roundToInt

/**
 * Cyberpunk-styled vertical EQ slider for 32-band equalizer
 */
@Composable
fun EqualizerSlider(
    band: EqualizerBand,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val density = LocalDensity.current
    val dimensions = Pixel9ProDimensions.default
    val sliderHeight = dimensions.eqSliderHeight
    val sliderWidth = dimensions.eqSliderWidth
    
    // Enhanced touch state management
    var isDragging by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    var localGain by remember(band.gain) { mutableStateOf(band.gain) }
    var lastTapTime by remember { mutableStateOf(0L) }
    
    // Update local state when band changes (e.g., preset applied)
    LaunchedEffect(band.gain) {
        if (!isDragging) {
            localGain = band.gain
        }
    }
    
    // Normalize value to 0-1 range
    val normalizedValue = (localGain - EqualizerBand.MIN_GAIN) / 
                         (EqualizerBand.MAX_GAIN - EqualizerBand.MIN_GAIN)
    
    // Colors based on frequency category
    val category = band.getFrequencyCategory()
    val primaryColor = category.color
    val glowColor = primaryColor.copy(alpha = 0.6f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(32.dp) // Increased from 28dp for wider sliders
            .padding(horizontal = 2.dp) // Add padding for better touch targets
    ) {
        // Gain value display
        Text(
            text = if (localGain >= 0) "+${localGain.roundToInt()}" else "${localGain.roundToInt()}",
            fontSize = dimensions.eqGainText,
            color = if (localGain == 0f) SubcoderColors.LightGrey else primaryColor,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.height(12.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Enhanced vertical slider with better touch handling
        Box(
            modifier = Modifier
                .width(sliderWidth + 8.dp) // Extra width for touch area
                .height(sliderHeight)
                .clip(RoundedCornerShape(sliderWidth / 2))
                .background(SubcoderColors.DarkGrey.copy(alpha = 0.3f))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(band.id) {
                        // Handle tap gestures for single-tap positioning and double-tap reset
                        detectTapGestures(
                            onPress = { offset ->
                                isPressed = true
                                val currentTime = System.currentTimeMillis()
                                val heightPx = size.height
                                val tapPosition = 1f - (offset.y / heightPx)
                                val clampedPosition = tapPosition.coerceIn(0f, 1f)
                                
                                // Check for double-tap (within 300ms and on the knob area)
                                val knobY = heightPx - (normalizedValue * heightPx)
                                val isOnKnob = kotlin.math.abs(offset.y - knobY) < 40f // 40px touch area around knob
                                
                                if (currentTime - lastTapTime < 300 && isOnKnob) {
                                    // Double-tap: Reset to 0dB
                                    localGain = 0f
                                    onValueChange(localGain)
                                } else {
                                    // Single-tap: Jump to position
                                    localGain = (EqualizerBand.MIN_GAIN + 
                                               clampedPosition * (EqualizerBand.MAX_GAIN - EqualizerBand.MIN_GAIN))
                                        .coerceIn(EqualizerBand.MIN_GAIN, EqualizerBand.MAX_GAIN)
                                    onValueChange(localGain)
                                }
                                
                                lastTapTime = currentTime
                                
                                // Wait for release
                                tryAwaitRelease()
                                isPressed = false
                            }
                        )
                    }
                    .pointerInput("${band.id}_drag") {
                        // Handle drag gestures separately for smooth dragging
                        detectDragGestures(
                            onDragStart = { _ ->
                                isDragging = true
                            },
                            onDragEnd = {
                                isDragging = false
                                onValueChange(localGain)
                            },
                            onDrag = { _, dragAmount ->
                                val heightPx = size.height
                                val deltaValue = -dragAmount.y / heightPx * 
                                               (EqualizerBand.MAX_GAIN - EqualizerBand.MIN_GAIN)
                                localGain = (localGain + deltaValue).coerceIn(
                                    EqualizerBand.MIN_GAIN,
                                    EqualizerBand.MAX_GAIN
                                )
                                // Provide immediate feedback during drag
                                onValueChange(localGain)
                            }
                        )
                    }
            ) {
                drawEnhancedEqualizerSlider(
                    normalizedValue = normalizedValue,
                    primaryColor = primaryColor,
                    glowColor = glowColor,
                    isEnabled = band.isEnabled,
                    isDragging = isDragging,
                    isPressed = isPressed
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Frequency label
        Text(
            text = band.getDisplayFrequency(),
            fontSize = dimensions.eqFrequencyText,
            color = SubcoderColors.LightGrey,
            fontFamily = OrbitronFontFamily,
            textAlign = TextAlign.Center,
            lineHeight = 8.sp,
            modifier = Modifier.height(14.dp)
        )
    }
}

/**
 * Draw the enhanced cyberpunk EQ slider with bigger knobs and better interaction
 */
private fun DrawScope.drawEnhancedEqualizerSlider(
    normalizedValue: Float,
    primaryColor: Color,
    glowColor: Color,
    isEnabled: Boolean,
    isDragging: Boolean = false,
    isPressed: Boolean = false
) {
    val trackWidth = size.width * 0.4f // Wider track
    val trackHeight = size.height
    val centerX = size.width / 2f
    val centerY = size.height / 2f // This is always the true center - 0dB position
    
    if (isEnabled) {
        // Calculate thumb position
        val thumbY = trackHeight - (normalizedValue * trackHeight)
        
        // Enhanced fill colors
        val fillColor = if (normalizedValue > 0.5f) {
            primaryColor.copy(alpha = 0.8f)
        } else {
            primaryColor.copy(alpha = 0.6f)
        }
        
        // Draw track background first
        drawRect(
            color = SubcoderColors.DarkGrey.copy(alpha = 0.4f),
            topLeft = Offset(centerX - trackWidth / 2f, 0f),
            size = androidx.compose.ui.geometry.Size(trackWidth, trackHeight)
        )
        
        // Draw fill from center line to knob position
        if (normalizedValue > 0.5f) {
            // Positive gain: fill from center up to knob
            drawRect(
                color = fillColor,
                topLeft = Offset(centerX - trackWidth / 2f, thumbY),
                size = androidx.compose.ui.geometry.Size(trackWidth, centerY - thumbY)
            )
        } else {
            // Negative gain: fill from center down to knob
            drawRect(
                color = fillColor,
                topLeft = Offset(centerX - trackWidth / 2f, centerY),
                size = androidx.compose.ui.geometry.Size(trackWidth, thumbY - centerY)
            )
        }
        
        // Calculate dynamic knob size based on interaction state
        val baseKnobRadius = size.width * 0.35f // Much larger base knob
        val knobRadius = when {
            isDragging -> baseKnobRadius * 1.3f
            isPressed -> baseKnobRadius * 1.15f
            else -> baseKnobRadius
        }
        
        // Enhanced glow effect - multiple layers for better visibility
        val glowRadius = knobRadius * 1.8f
        drawCircle(
            color = glowColor,
            radius = glowRadius,
            center = Offset(centerX, thumbY),
            alpha = when {
                isDragging -> 0.8f
                isPressed -> 0.6f
                else -> 0.4f
            }
        )
        
        // Outer glow ring
        drawCircle(
            color = glowColor,
            radius = knobRadius * 1.4f,
            center = Offset(centerX, thumbY),
            alpha = 0.3f
        )
        
        // Main knob body - gradient effect
        drawCircle(
            color = primaryColor,
            radius = knobRadius,
            center = Offset(centerX, thumbY)
        )
        
        // Inner knob highlight - bigger and more prominent
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = knobRadius * 0.4f,
            center = Offset(centerX, thumbY)
        )
        
        // Center dot for precision
        drawCircle(
            color = primaryColor.copy(alpha = 0.8f),
            radius = knobRadius * 0.15f,
            center = Offset(centerX, thumbY)
        )
        
    } else {
        // Disabled state - more visible
        drawRect(
            color = Color.Gray.copy(alpha = 0.4f),
            topLeft = Offset(centerX - trackWidth / 2f, 0f),
            size = androidx.compose.ui.geometry.Size(trackWidth, trackHeight)
        )
        
        // Disabled knob
        val thumbY = trackHeight - (normalizedValue * trackHeight)
        drawCircle(
            color = Color.Gray.copy(alpha = 0.6f),
            radius = size.width * 0.3f,
            center = Offset(centerX, thumbY)
        )
    }
    
    // ALWAYS draw the zero line (center) on top - this should never move or disappear
    drawLine(
        color = Color.White.copy(alpha = 0.8f), // More visible
        start = Offset(centerX - size.width * 0.45f, centerY),
        end = Offset(centerX + size.width * 0.45f, centerY),
        strokeWidth = 2.dp.toPx()
    )
}