package com.ftl.hires.audioplayer.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import com.ftl.hires.audioplayer.presentation.theme.FTLAudioTypography
import com.ftl.hires.audioplayer.presentation.navigation.FTLDestination

/**
 * FTL Cyberpunk Bottom Navigation Component
 * 
 * Features:
 * - Cyberpunk styling with glowing borders and effects
 * - FTL-themed navigation labels with futuristic naming
 * - Animated icon transitions with pulsing effects
 * - Holographic background with gradient overlays
 * - Responsive to navigation state changes
 * - Haptic feedback for professional feel
 */

// Navigation items with FTL-themed data
data class FTLNavItem(
    val route: String,
    val label: String,
    val cyberpunkLabel: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val glowColor: Color,
    val description: String
)

// FTL Navigation items configuration
private val ftlNavItems = listOf(
    FTLNavItem(
        route = "command_center",
        label = "Command",
        cyberpunkLabel = "CMD.CTR",
        icon = Icons.Outlined.Dashboard,
        selectedIcon = Icons.Filled.Dashboard,
        glowColor = SubcoderColors.Cyan,
        description = "Main control hub"
    ),
    FTLNavItem(
        route = "audio_archive",
        label = "Archive",
        cyberpunkLabel = "AUD.ARC",
        icon = Icons.Outlined.LibraryMusic,
        selectedIcon = Icons.Filled.LibraryMusic,
        glowColor = SubcoderColors.ElectricBlue,
        description = "Music database"
    ),
    FTLNavItem(
        route = "audio_matrix",
        label = "Matrix",
        cyberpunkLabel = "AUD.MTX",
        icon = Icons.Outlined.GraphicEq,
        selectedIcon = Icons.Filled.GraphicEq,
        glowColor = SubcoderColors.Orange,
        description = "EQ control matrix"
    ),
    FTLNavItem(
        route = "system_config",
        label = "Config",
        cyberpunkLabel = "SYS.CFG",
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
        glowColor = SubcoderColors.NeonGreen,
        description = "System configuration"
    )
)

/**
 * Main FTL Bottom Navigation Component
 */
@Composable
fun FTLBottomNavigation(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    compactMode: Boolean = false
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    
    // Holographic background animation
    val infiniteTransition = rememberInfiniteTransition(label = "holographicBg")
    val holographicOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "holographicOffset"
    )
    
    // Navigation bar container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compactMode) 60.dp else 80.dp)
    ) {
        // Holographic background effect
        FTLHolographicBackground(
            modifier = Modifier.fillMaxSize(),
            offset = holographicOffset,
            alpha = 0.3f
        )
        
        // Main navigation content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            SubcoderColors.PureBlack.copy(alpha = 0.85f),
                            SubcoderColors.PureBlack.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ftlNavItems.forEach { item ->
                val isSelected = currentDestination == item.route
                
                FTLNavItem(
                    item = item,
                    isSelected = isSelected,
                    showLabel = showLabels,
                    compactMode = compactMode,
                    onClick = {
                        if (currentDestination != item.route) {
                            navController.navigate(item.route) {
                                // Pop up to the start destination
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                // Avoid multiple copies
                                launchSingleTop = true
                                // Restore state
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
        
        // Top border glow effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            SubcoderColors.Cyan.copy(alpha = 0.6f),
                            SubcoderColors.Orange.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

/**
 * Individual navigation item with cyberpunk styling
 */
@Composable
private fun FTLNavItem(
    item: FTLNavItem,
    isSelected: Boolean,
    showLabel: Boolean,
    compactMode: Boolean,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    
    // Animation states
    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.4f,
        animationSpec = tween(300),
        label = "glowAlpha"
    )
    
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "iconScale"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.7f,
        animationSpec = tween(200),
        label = "contentAlpha"
    )
    
    // Pulsing animation for selected item
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(
                horizontal = if (compactMode) 8.dp else 12.dp,
                vertical = if (compactMode) 4.dp else 6.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon container with glow effect
        Box(
            modifier = Modifier
                .size(if (compactMode) 28.dp else 32.dp)
                .drawBehind {
                    // Draw icon glow
                    drawFTLIconGlow(
                        color = item.glowColor,
                        alpha = glowAlpha * contentAlpha,
                        pulseAlpha = pulseAlpha * 0.3f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Glow background circle
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size((if (compactMode) 28.dp else 32.dp) * iconScale)
                        .background(
                            color = item.glowColor.copy(alpha = glowAlpha * 0.1f),
                            shape = RoundedCornerShape(50)
                        )
                )
            }
            
            // Main icon
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.icon,
                contentDescription = item.description,
                tint = if (isSelected) {
                    item.glowColor
                } else {
                    SubcoderColors.LightGrey.copy(alpha = contentAlpha)
                },
                modifier = Modifier
                    .size((if (compactMode) 20.dp else 24.dp) * iconScale)
            )
        }
        
        // Label text
        if (showLabel && !compactMode) {
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = item.cyberpunkLabel,
                color = if (isSelected) {
                    item.glowColor.copy(alpha = contentAlpha)
                } else {
                    SubcoderColors.LightGrey.copy(alpha = contentAlpha)
                },
                fontSize = 8.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.5.sp
            )
        }
        
        // Selection indicator line
        if (isSelected) {
            Spacer(modifier = Modifier.height(if (compactMode) 2.dp else 4.dp))
            Box(
                modifier = Modifier
                    .width(if (compactMode) 16.dp else 20.dp)
                    .height(2.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                item.glowColor.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

/**
 * Draw glow effect around icons
 */
private fun DrawScope.drawFTLIconGlow(
    color: Color,
    alpha: Float,
    pulseAlpha: Float
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val baseRadius = size.minDimension / 2
    
    // Multiple glow layers for depth
    for (i in 1..4) {
        val radius = baseRadius + (i * 3.dp.toPx())
        val layerAlpha = (alpha * (0.6f - i * 0.1f)).coerceAtLeast(0f)
        
        drawCircle(
            color = color.copy(alpha = layerAlpha + pulseAlpha * 0.2f),
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

/**
 * Holographic background effect
 */
@Composable
private fun FTLHolographicBackground(
    modifier: Modifier = Modifier,
    offset: Float,
    alpha: Float
) {
    Box(
        modifier = modifier
            .drawBehind {
                drawHolographicPattern(
                    offset = offset,
                    alpha = alpha
                )
            }
    )
}

/**
 * Draw holographic pattern effect
 */
private fun DrawScope.drawHolographicPattern(
    offset: Float,
    alpha: Float
) {
    val patternHeight = 4.dp.toPx()
    val totalOffset = offset * size.height
    
    // Animated holographic lines
    for (i in 0 until (size.height / patternHeight).toInt() + 2) {
        val y = (i * patternHeight + totalOffset) % (size.height + patternHeight)
        val lineAlpha = (alpha * (0.3f + kotlin.math.sin(offset * 2 * kotlin.math.PI + i * 0.5f) * 0.2f)).toFloat()
        
        drawLine(
            color = SubcoderColors.Cyan.copy(alpha = lineAlpha),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )
        
        // Secondary pattern with different color
        if (i % 3 == 0) {
            drawLine(
                color = SubcoderColors.Orange.copy(alpha = lineAlpha * 0.5f),
                start = Offset(0f, y + patternHeight / 2),
                end = Offset(size.width, y + patternHeight / 2),
                strokeWidth = 0.5.dp.toPx()
            )
        }
    }
}

/**
 * Compact version for landscape or limited space
 */
@Composable
fun FTLBottomNavigationCompact(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    FTLBottomNavigation(
        navController = navController,
        currentRoute = currentRoute,
        modifier = modifier,
        showLabels = false,
        compactMode = true
    )
}

/**
 * Extension function to get current route safely
 */
private fun NavController.getCurrentRoute(): String? {
    return currentBackStackEntry?.destination?.route
}

/**
 * Preview helper for development
 */
object FTLBottomNavigationPreview {
    val SampleItems = ftlNavItems
}