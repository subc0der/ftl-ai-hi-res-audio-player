package com.ftl.hires.audioplayer.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily

/**
 * FTL Cyberpunk Button Component
 * 
 * Features:
 * - Glowing cyan/orange borders matching FTL game aesthetic
 * - Orbitron font for futuristic text
 * - Animated hover and press effects
 * - Subcoder Cipher Matrix styling
 * - Multiple variants (Primary, Secondary, Danger, etc.)
 * - Icon support with proper spacing
 * - Haptic feedback integration
 */

enum class FTLButtonVariant {
    PRIMARY,    // Cyan glow - main actions
    SECONDARY,  // Orange glow - secondary actions  
    DANGER,     // Red glow - destructive actions
    SUCCESS,    // Green glow - confirmation actions
    GHOST,      // Transparent with border only
    DISABLED    // Muted colors, no interaction
}

enum class FTLButtonSize {
    SMALL,      // Compact buttons for toolbars
    MEDIUM,     // Standard UI buttons
    LARGE,      // Prominent action buttons
    EXTRA_LARGE // Hero buttons
}

/**
 * Main FTL Button Component
 */
@Composable
fun FTLButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: FTLButtonVariant = FTLButtonVariant.PRIMARY,
    size: FTLButtonSize = FTLButtonSize.MEDIUM,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.START,
    fullWidth: Boolean = false,
    glowIntensity: Float = 1.0f,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    // Animation states
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animated values
    val glowAlpha by animateFloatAsState(
        targetValue = when {
            !enabled -> 0.3f
            isPressed -> 1.0f
            isHovered -> 0.8f
            else -> 0.6f
        },
        animationSpec = tween(200),
        label = "glowAlpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = tween(100),
        label = "buttonScale"
    )
    
    val borderWidth by animateFloatAsState(
        targetValue = when {
            !enabled -> 1.dp.value
            isPressed -> 3.dp.value
            isHovered -> 2.5f.dp.value
            else -> 2.dp.value
        },
        animationSpec = tween(200),
        label = "borderWidth"
    )
    
    // Colors based on variant
    val colors = getButtonColors(variant, enabled)
    
    // Size configuration
    val sizeConfig = getButtonSize(size)
    
    Box(
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .scale(scale)
            .height(sizeConfig.height)
            .clip(RoundedCornerShape(sizeConfig.cornerRadius))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colors.background.copy(alpha = 0.1f),
                        colors.background.copy(alpha = 0.2f),
                        colors.background.copy(alpha = 0.1f)
                    )
                )
            )
            .drawBehind {
                drawFTLButtonBorder(
                    colors = colors,
                    cornerRadius = sizeConfig.cornerRadius,
                    borderWidth = borderWidth.dp,
                    glowAlpha = glowAlpha * glowIntensity
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = sizeConfig.horizontalPadding, vertical = sizeConfig.verticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Leading icon
            if (icon != null && iconPosition == IconPosition.START) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.content,
                    modifier = Modifier.size(sizeConfig.iconSize)
                )
                Spacer(modifier = Modifier.width(sizeConfig.iconSpacing))
            }
            
            // Button text
            Text(
                text = text.uppercase(),
                color = colors.content,
                fontSize = sizeConfig.fontSize,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                letterSpacing = 1.sp
            )
            
            // Trailing icon
            if (icon != null && iconPosition == IconPosition.END) {
                Spacer(modifier = Modifier.width(sizeConfig.iconSpacing))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.content,
                    modifier = Modifier.size(sizeConfig.iconSize)
                )
            }
        }
        
        // Animated scanning line effect for primary buttons
        if (variant == FTLButtonVariant.PRIMARY && enabled && (isHovered || isPressed)) {
            FTLScanningLine(
                color = colors.glow,
                alpha = glowAlpha * 0.7f,
                height = sizeConfig.height,
                cornerRadius = sizeConfig.cornerRadius
            )
        }
    }
}

/**
 * Draw the cyberpunk border with glow effect
 */
private fun DrawScope.drawFTLButtonBorder(
    colors: FTLButtonColors,
    cornerRadius: Dp,
    borderWidth: Dp,
    glowAlpha: Float
) {
    val borderWidthPx = borderWidth.toPx()
    val cornerRadiusPx = cornerRadius.toPx()
    val glowRadius = borderWidthPx * 3
    
    // Outer glow effect
    for (i in 0..5) {
        val alpha = (glowAlpha * (0.8f - i * 0.15f)).coerceIn(0f, 1f)
        val width = borderWidthPx + (i * 2)
        
        drawRoundRect(
            color = colors.glow.copy(alpha = alpha),
            topLeft = Offset(-width / 2, -width / 2),
            size = Size(size.width + width, size.height + width),
            cornerRadius = CornerRadius(cornerRadiusPx + width / 2),
            style = Stroke(width = width / 2)
        )
    }
    
    // Main border
    drawRoundRect(
        color = colors.border,
        cornerRadius = CornerRadius(cornerRadiusPx),
        style = Stroke(width = borderWidthPx)
    )
    
    // Inner highlight
    drawRoundRect(
        color = colors.glow.copy(alpha = glowAlpha * 0.3f),
        topLeft = Offset(borderWidthPx, borderWidthPx),
        size = Size(size.width - borderWidthPx * 2, size.height - borderWidthPx * 2),
        cornerRadius = CornerRadius(cornerRadiusPx - borderWidthPx),
        style = Stroke(width = 1.dp.toPx())
    )
}

/**
 * Animated scanning line effect
 */
@Composable
private fun FTLScanningLine(
    color: Color,
    alpha: Float,
    height: Dp,
    cornerRadius: Dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanningLine")
    
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLineOffset"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(cornerRadius))
            .drawBehind {
                val gradient = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = alpha),
                        color.copy(alpha = alpha * 0.5f),
                        Color.Transparent
                    ),
                    startX = offsetX,
                    endX = offsetX + 100f
                )
                
                drawRect(
                    brush = gradient,
                    size = size
                )
            }
    )
}

/**
 * Get button colors based on variant
 */
private fun getButtonColors(variant: FTLButtonVariant, enabled: Boolean): FTLButtonColors {
    return when (variant) {
        FTLButtonVariant.PRIMARY -> FTLButtonColors(
            background = if (enabled) SubcoderColors.Cyan else SubcoderColors.LightGrey,
            border = if (enabled) SubcoderColors.Cyan else SubcoderColors.LightGrey,
            content = if (enabled) SubcoderColors.PureBlack else SubcoderColors.MediumGrey,
            glow = if (enabled) SubcoderColors.Cyan else SubcoderColors.LightGrey
        )
        FTLButtonVariant.SECONDARY -> FTLButtonColors(
            background = if (enabled) SubcoderColors.Orange else SubcoderColors.LightGrey,
            border = if (enabled) SubcoderColors.Orange else SubcoderColors.LightGrey,
            content = if (enabled) SubcoderColors.PureBlack else SubcoderColors.MediumGrey,
            glow = if (enabled) SubcoderColors.Orange else SubcoderColors.LightGrey
        )
        FTLButtonVariant.DANGER -> FTLButtonColors(
            background = if (enabled) SubcoderColors.ErrorRed else SubcoderColors.LightGrey,
            border = if (enabled) SubcoderColors.ErrorRed else SubcoderColors.LightGrey,
            content = if (enabled) SubcoderColors.White else SubcoderColors.MediumGrey,
            glow = if (enabled) SubcoderColors.ErrorRed else SubcoderColors.LightGrey
        )
        FTLButtonVariant.SUCCESS -> FTLButtonColors(
            background = if (enabled) SubcoderColors.SuccessGreen else SubcoderColors.LightGrey,
            border = if (enabled) SubcoderColors.SuccessGreen else SubcoderColors.LightGrey,
            content = if (enabled) SubcoderColors.PureBlack else SubcoderColors.MediumGrey,
            glow = if (enabled) SubcoderColors.SuccessGreen else SubcoderColors.LightGrey
        )
        FTLButtonVariant.GHOST -> FTLButtonColors(
            background = Color.Transparent,
            border = if (enabled) SubcoderColors.Cyan else SubcoderColors.LightGrey,
            content = if (enabled) SubcoderColors.Cyan else SubcoderColors.LightGrey,
            glow = if (enabled) SubcoderColors.Cyan else SubcoderColors.LightGrey
        )
        FTLButtonVariant.DISABLED -> FTLButtonColors(
            background = SubcoderColors.LightGrey.copy(alpha = 0.1f),
            border = SubcoderColors.LightGrey,
            content = SubcoderColors.MediumGrey,
            glow = SubcoderColors.LightGrey
        )
    }
}

/**
 * Get button size configuration
 */
private fun getButtonSize(size: FTLButtonSize): FTLButtonSize.Config {
    return when (size) {
        FTLButtonSize.SMALL -> FTLButtonSize.Config(
            height = 32.dp,
            horizontalPadding = 12.dp,
            verticalPadding = 6.dp,
            fontSize = 10.sp,
            iconSize = 16.dp,
            iconSpacing = 4.dp,
            cornerRadius = 6.dp
        )
        FTLButtonSize.MEDIUM -> FTLButtonSize.Config(
            height = 40.dp,
            horizontalPadding = 16.dp,
            verticalPadding = 8.dp,
            fontSize = 12.sp,
            iconSize = 18.dp,
            iconSpacing = 6.dp,
            cornerRadius = 8.dp
        )
        FTLButtonSize.LARGE -> FTLButtonSize.Config(
            height = 48.dp,
            horizontalPadding = 20.dp,
            verticalPadding = 12.dp,
            fontSize = 14.sp,
            iconSize = 20.dp,
            iconSpacing = 8.dp,
            cornerRadius = 10.dp
        )
        FTLButtonSize.EXTRA_LARGE -> FTLButtonSize.Config(
            height = 56.dp,
            horizontalPadding = 24.dp,
            verticalPadding = 16.dp,
            fontSize = 16.sp,
            iconSize = 24.dp,
            iconSpacing = 10.dp,
            cornerRadius = 12.dp
        )
    }
}

/**
 * Button size configuration
 */
private data class FTLButtonSize.Config(
    val height: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val fontSize: androidx.compose.ui.unit.TextUnit,
    val iconSize: Dp,
    val iconSpacing: Dp,
    val cornerRadius: Dp
)

/**
 * Button color configuration
 */
private data class FTLButtonColors(
    val background: Color,
    val border: Color,
    val content: Color,
    val glow: Color
)

/**
 * Icon position enum
 */
enum class IconPosition {
    START, END
}

/**
 * Specialized button variants for common use cases
 */

@Composable
fun FTLPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    FTLButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        variant = FTLButtonVariant.PRIMARY,
        enabled = enabled,
        icon = icon,
        fullWidth = fullWidth
    )
}

@Composable
fun FTLSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    FTLButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        variant = FTLButtonVariant.SECONDARY,
        enabled = enabled,
        icon = icon,
        fullWidth = fullWidth
    )
}

@Composable
fun FTLDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    FTLButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        variant = FTLButtonVariant.DANGER,
        enabled = enabled,
        icon = icon,
        fullWidth = fullWidth
    )
}

@Composable
fun FTLGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    FTLButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        variant = FTLButtonVariant.GHOST,
        enabled = enabled,
        icon = icon,
        fullWidth = fullWidth
    )
}