package com.ftl.hires.audioplayer.presentation.screens.equalizer.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ftl.hires.audioplayer.audio.equalizer.EQMode
import com.ftl.hires.audioplayer.audio.equalizer.getCyberpunkIcon
import com.ftl.hires.audioplayer.audio.equalizer.getRecommendedUse
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily

/**
 * Cyberpunk-styled EQ Mode Selector
 * 
 * Allows seamless switching between 5, 10, 20, and 32-band EQ modes
 * with cyberpunk aesthetic and smooth animations.
 */
@Composable
fun EQModeSelector(
    currentMode: EQMode,
    availableModes: List<EQMode>,
    onModeSelected: (EQMode) -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onExpandToggle: () -> Unit = {}
) {
    var isDetailExpanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SubcoderColors.DarkGrey.copy(alpha = 0.3f),
                        SubcoderColors.DarkGrey.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = currentMode.color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        // Header with current mode info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "EQ MODE",
                    fontSize = 10.sp,
                    color = SubcoderColors.LightGrey,
                    fontFamily = OrbitronFontFamily,
                    letterSpacing = 1.sp
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currentMode.displayName,
                        fontSize = 18.sp,
                        color = currentMode.color,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${currentMode.bandCount} BANDS",
                        fontSize = 12.sp,
                        color = SubcoderColors.LightGrey,
                        fontFamily = OrbitronFontFamily
                    )
                }
            }
            
            // Expand/Collapse Button
            IconButton(
                onClick = { isDetailExpanded = !isDetailExpanded },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isDetailExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ExpandMore,
                    contentDescription = if (isDetailExpanded) "Collapse" else "Expand",
                    tint = currentMode.color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Cyberpunk Mode Visual Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = currentMode.getCyberpunkIcon(),
                fontSize = 8.sp,
                color = currentMode.color.copy(alpha = 0.6f),
                fontFamily = OrbitronFontFamily,
                letterSpacing = 0.5.sp
            )
        }
        
        // Expandable Details
        AnimatedVisibility(
            visible = isDetailExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current Mode Description
                Text(
                    text = currentMode.description,
                    fontSize = 12.sp,
                    color = SubcoderColors.LightGrey,
                    fontFamily = OrbitronFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = currentMode.getRecommendedUse(),
                    fontSize = 10.sp,
                    color = SubcoderColors.LightGrey.copy(alpha = 0.8f),
                    fontFamily = OrbitronFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Mode Selection Grid
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableModes) { mode ->
                        EQModeCard(
                            mode = mode,
                            isSelected = mode == currentMode,
                            onClick = { onModeSelected(mode) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual EQ Mode Card
 */
@Composable
private fun EQModeCard(
    mode: EQMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        mode.color.copy(alpha = 0.2f)
    } else {
        SubcoderColors.DarkGrey.copy(alpha = 0.3f)
    }
    
    val borderColor = if (isSelected) {
        mode.color
    } else {
        SubcoderColors.LightGrey.copy(alpha = 0.3f)
    }
    
    Box(
        modifier = modifier
            .width(80.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Mode Icon
            Text(
                text = when (mode.bandCount) {
                    5 -> "▌▌▌▌▌"
                    10 -> "▌▌▌▌▌"
                    20 -> "████"
                    32 -> "■■■■"
                    else -> "▌▌▌"
                },
                fontSize = 10.sp,
                color = if (isSelected) mode.color else SubcoderColors.LightGrey,
                fontFamily = OrbitronFontFamily,
                textAlign = TextAlign.Center
            )
            
            // Band Count
            Text(
                text = "${mode.bandCount}",
                fontSize = 16.sp,
                color = if (isSelected) mode.color else SubcoderColors.LightGrey,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold
            )
            
            // Mode Name
            Text(
                text = mode.displayName,
                fontSize = 8.sp,
                color = if (isSelected) mode.color else SubcoderColors.LightGrey.copy(alpha = 0.8f),
                fontFamily = OrbitronFontFamily,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * Compact EQ Mode Switcher for header use
 */
@Composable
fun CompactEQModeSelector(
    currentMode: EQMode,
    availableModes: List<EQMode>,
    onModeSelected: (EQMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        // Current Mode Button
        Button(
            onClick = { isExpanded = !isExpanded },
            colors = ButtonDefaults.buttonColors(
                containerColor = currentMode.color.copy(alpha = 0.2f),
                contentColor = currentMode.color
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "${currentMode.bandCount}B",
                fontSize = 12.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Dropdown Menu
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.background(
                color = SubcoderColors.DarkGrey,
                shape = RoundedCornerShape(8.dp)
            )
        ) {
            availableModes.forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${mode.bandCount}",
                                fontSize = 14.sp,
                                color = mode.color,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mode.displayName,
                                fontSize = 12.sp,
                                color = SubcoderColors.LightGrey,
                                fontFamily = OrbitronFontFamily
                            )
                        }
                    },
                    onClick = {
                        onModeSelected(mode)
                        isExpanded = false
                    },
                    modifier = Modifier.background(
                        if (mode == currentMode) mode.color.copy(alpha = 0.1f) else Color.Transparent
                    )
                )
            }
        }
    }
}