package com.ftl.hires.audioplayer.presentation.screens.equalizer.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ftl.hires.audioplayer.audio.equalizer.EQMode
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import kotlinx.coroutines.delay

/**
 * Polished animations and loading states for EQ mode transitions
 * 
 * Features:
 * - Smooth mode transition animations
 * - Loading states with cyberpunk effects
 * - Band count morphing animations
 * - Frequency response curve transitions
 * - Audio processing indicators
 */

/**
 * Mode transition loading overlay
 */
@Composable
fun EQModeTransitionLoader(
    isVisible: Boolean,
    fromMode: EQMode?,
    toMode: EQMode?,
    progress: Float = 0f,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.8f),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 1.2f),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = SubcoderColors.DarkGrey.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mode transition visualization
                ModeTransitionVisual(
                    fromMode = fromMode,
                    toMode = toMode,
                    progress = progress
                )
                
                // Loading text with typewriter effect
                TransitionLoadingText(
                    fromMode = fromMode,
                    toMode = toMode,
                    progress = progress
                )
                
                // Progress indicator
                AnimatedProgressIndicator(
                    progress = progress,
                    color = toMode?.getModeColor() ?: SubcoderColors.Cyan
                )
            }
        }
    }
}

/**
 * Visual representation of mode transition
 */
@Composable
private fun ModeTransitionVisual(
    fromMode: EQMode?,
    toMode: EQMode?,
    progress: Float
) {
    val density = LocalDensity.current
    val fromColor = fromMode?.getModeColor() ?: SubcoderColors.LightGrey
    val toColor = toMode?.getModeColor() ?: SubcoderColors.Cyan
    
    // Animated color transition
    val currentColor by animateColorAsState(
        targetValue = if (progress > 0.5f) toColor else fromColor,
        animationSpec = tween(500),
        label = "mode_color_transition"
    )
    
    Canvas(
        modifier = Modifier
            .size(120.dp)
            .padding(8.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        
        // Draw frequency bands morphing animation
        val fromBandCount = fromMode?.bandCount ?: 0
        val toBandCount = toMode?.bandCount ?: 0
        val currentBandCount = (fromBandCount + (toBandCount - fromBandCount) * progress).toInt()
        
        // Draw animated frequency bands
        repeat(currentBandCount.coerceAtLeast(5)) { index ->
            val bandProgress = (progress * currentBandCount - index).coerceIn(0f, 1f)
            val alpha = if (bandProgress > 0) 1f else 0.3f
            val height = (canvasHeight * 0.3f) + (canvasHeight * 0.4f * bandProgress * (0.5f + 0.5f * kotlin.math.sin(index.toFloat())))
            
            val x = (canvasWidth * (index + 1) / (currentBandCount + 1))
            
            drawLine(
                color = currentColor.copy(alpha = alpha),
                start = Offset(x, centerY + height / 2),
                end = Offset(x, centerY - height / 2),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        
        // Draw outer ring animation
        val ringRadius = kotlin.math.min(canvasWidth, canvasHeight) / 2.5f
        val ringProgress = (progress * 2).coerceAtMost(1f)
        
        drawCircle(
            color = currentColor.copy(alpha = 0.3f),
            radius = ringRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Draw progress arc
        drawArc(
            color = currentColor,
            startAngle = -90f,
            sweepAngle = 360f * ringProgress,
            useCenter = false,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
            topLeft = Offset(centerX - ringRadius, centerY - ringRadius),
            size = androidx.compose.ui.geometry.Size(ringRadius * 2, ringRadius * 2)
        )
    }
}

/**
 * Typewriter effect loading text
 */
@Composable
private fun TransitionLoadingText(
    fromMode: EQMode?,
    toMode: EQMode?,
    progress: Float
) {
    val messages = listOf(
        "Analyzing frequency response...",
        "Preserving gain settings...", 
        "Recalculating band mappings...",
        "Applying audio processing...",
        "Transition complete!"
    )
    
    val currentMessageIndex = (progress * messages.size).toInt().coerceAtMost(messages.size - 1)
    val currentMessage = messages[currentMessageIndex]
    
    var displayedText by remember { mutableStateOf("") }
    var messageIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(currentMessageIndex) {
        messageIndex = currentMessageIndex
        displayedText = ""
        
        // Typewriter effect
        currentMessage.forEachIndexed { index, char ->
            delay(30) // Typing speed
            displayedText = currentMessage.take(index + 1)
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Mode transition title
        AnimatedContent(
            targetState = "${fromMode?.displayName ?: "Current"} → ${toMode?.displayName ?: "Target"}",
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
            },
            label = "mode_transition_title"
        ) { title ->
            Text(
                text = title,
                fontSize = 16.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                color = toMode?.getModeColor() ?: SubcoderColors.Cyan,
                textAlign = TextAlign.Center
            )
        }
        
        // Typewriter message
        Text(
            text = displayedText,
            fontSize = 12.sp,
            color = SubcoderColors.LightGrey,
            textAlign = TextAlign.Center,
            minLines = 1
        )
    }
}

/**
 * Animated progress indicator with cyberpunk styling
 */
@Composable
private fun AnimatedProgressIndicator(
    progress: Float,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "progress_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Progress bar with glow effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(SubcoderColors.MediumGrey.copy(alpha = 0.3f))
        ) {
            // Progress fill
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.8f),
                                color,
                                color.copy(alpha = glowAlpha)
                            )
                        )
                    )
                    .clip(RoundedCornerShape(3.dp))
            )
        }
        
        // Progress percentage
        Text(
            text = "${(progress * 100).toInt()}%",
            fontSize = 14.sp,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Band count morphing animation
 */
@Composable
fun BandCountMorph(
    fromCount: Int,
    toCount: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val currentCount = (fromCount + (toCount - fromCount) * progress).toInt()
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(32) { index ->
            val isActive = index < currentCount
            val shouldAnimate = index < toCount
            
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.3f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                    visibilityThreshold = 0.01f
                ),
                label = "band_scale_$index"
            )
            
            val alpha by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.2f,
                animationSpec = tween(durationMillis = 200, delayMillis = index * 10),
                label = "band_alpha_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(3.dp, 16.dp)
                    .scale(scaleY = scale, scaleX = 1f)
                    .background(
                        color = SubcoderColors.Cyan.copy(alpha = alpha),
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

/**
 * Floating audio processing indicator
 */
@Composable
fun FloatingAudioIndicator(
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isProcessing,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = SubcoderColors.DarkGrey.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rotating audio icon
                val rotation by rememberInfiniteTransition(label = "audio_rotation").animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )
                
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = SubcoderColors.Cyan,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(rotation)
                )
                
                Text(
                    text = "Processing Audio...",
                    fontSize = 12.sp,
                    color = SubcoderColors.OffWhite,
                    fontFamily = OrbitronFontFamily
                )
            }
        }
    }
}

/**
 * Preset conversion animation
 */
@Composable
fun PresetConversionAnimation(
    isConverting: Boolean,
    fromMode: EQMode,
    toMode: EQMode,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isConverting,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = SubcoderColors.Orange.copy(alpha = 0.1f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, SubcoderColors.Orange.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Conversion icon animation
                val infiniteTransition = rememberInfiniteTransition(label = "conversion_animation")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "conversion_scale"
                )
                
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = SubcoderColors.Orange,
                    modifier = Modifier
                        .size(24.dp)
                        .scale(scale)
                )
                
                Column {
                    Text(
                        text = "Converting Preset",
                        fontSize = 14.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = SubcoderColors.Orange
                    )
                    Text(
                        text = "${fromMode.displayName} → ${toMode.displayName}",
                        fontSize = 12.sp,
                        color = SubcoderColors.LightGrey
                    )
                }
            }
        }
    }
}

/**
 * Extension function to get mode color (reused from other components)
 */
private fun EQMode.getModeColor(): Color {
    return when (this) {
        is EQMode.Simple5Band -> SubcoderColors.Orange
        is EQMode.Standard10Band -> SubcoderColors.Cyan
        is EQMode.Advanced20Band -> SubcoderColors.NeonGreen
        is EQMode.Pro32Band -> SubcoderColors.ElectricBlue
    }
}