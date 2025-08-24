package com.ftl.hires.audioplayer.presentation.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import com.ftl.hires.audioplayer.presentation.theme.FTLAudioTypography
import com.ftl.hires.audioplayer.presentation.components.FTLButton
import com.ftl.hires.audioplayer.presentation.components.FTLButtonVariant
import com.ftl.hires.audioplayer.presentation.components.FTLButtonSize
import com.ftl.hires.audioplayer.presentation.viewmodel.MainViewModel
import kotlin.math.*
import kotlin.random.Random

/**
 * FTL Hi-Res Audio Player - Command Center (Home Screen)
 * 
 * Cyberpunk dashboard featuring:
 * - Neural network background animations
 * - Real-time audio visualizer with spectrum analysis
 * - Quick EQ controls with 32-band preview
 * - Now playing widget with hi-res indicators
 * - Library statistics with holographic displays
 * - System status monitoring
 * - FTL-themed UI with Orbitron fonts and cyan/orange accents
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val currentTrack by mainViewModel.currentTrack.collectAsStateWithLifecycle()
    val audioQuality by mainViewModel.audioQuality.collectAsStateWithLifecycle()
    val systemState by mainViewModel.systemState.collectAsStateWithLifecycle()
    
    // Neural network animation state
    val infiniteTransition = rememberInfiniteTransition(label = "neuralNetwork")
    val networkAnimationOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "networkOffset"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SubcoderColors.PureBlack)
    ) {
        // Neural network background animation
        NeuralNetworkBackground(
            modifier = Modifier.fillMaxSize(),
            animationOffset = networkAnimationOffset,
            alpha = 0.15f
        )
        
        // Main dashboard content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Command Center header
            item {
                CommandCenterHeader(
                    systemStatus = "ONLINE",
                    cpuUsage = uiState.cpuUsage,
                    memoryUsage = uiState.memoryUsage
                )
            }
            
            // Real-time audio visualizer
            item {
                AudioVisualizerSection(
                    isPlaying = uiState.isPlaying,
                    audioFormat = audioQuality.currentFormat.name,
                    sampleRate = audioQuality.sampleRate,
                    bitDepth = audioQuality.bitDepth
                )
            }
            
            // Now playing widget
            item {
                currentTrack?.let { track ->
                    NowPlayingWidget(
                        track = track,
                        audioQuality = audioQuality,
                        onNavigateToPlayer = {
                            navController.navigate("audio_bridge")
                        }
                    )
                } ?: NowPlayingPlaceholder(
                    onNavigateToLibrary = {
                        navController.navigate("audio_archive")
                    }
                )
            }
            
            // Quick controls row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quick EQ controls
                    QuickEQControls(
                        modifier = Modifier.weight(1f),
                        onNavigateToEQ = {
                            navController.navigate("audio_matrix")
                        }
                    )
                    
                    // Library stats
                    LibraryStatsWidget(
                        modifier = Modifier.weight(1f),
                        trackCount = uiState.libraryTrackCount ?: 0,
                        onNavigateToLibrary = {
                            navController.navigate("audio_archive")
                        }
                    )
                }
            }
            
            // System monitoring section
            item {
                SystemMonitoringSection(
                    isAudioServiceConnected = uiState.isAudioServiceConnected,
                    hasRequiredPermissions = uiState.hasRequiredPermissions,
                    onNavigateToSettings = {
                        navController.navigate("system_config")
                    }
                )
            }
        }
    }
}

/**
 * Command Center header with system status
 */
@Composable
private fun CommandCenterHeader(
    systemStatus: String,
    cpuUsage: Float,
    memoryUsage: Float
) {
    CyberpunkPanel(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "COMMAND CENTER",
                    style = FTLAudioTypography.librarySection,
                    color = SubcoderColors.Cyan,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "FTL Hi-Res Audio Control Hub",
                    style = FTLAudioTypography.settingDescription,
                    color = SubcoderColors.LightGrey
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                StatusIndicator(
                    label = "STATUS",
                    value = systemStatus,
                    color = SubcoderColors.SuccessGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusIndicator(
                        label = "CPU",
                        value = "${(cpuUsage * 100).toInt()}%",
                        color = if (cpuUsage > 0.8f) SubcoderColors.ErrorRed else SubcoderColors.Cyan
                    )
                    StatusIndicator(
                        label = "MEM",
                        value = "${(memoryUsage * 100).toInt()}%",
                        color = if (memoryUsage > 0.9f) SubcoderColors.ErrorRed else SubcoderColors.Orange
                    )
                }
            }
        }
    }
}

/**
 * Real-time audio visualizer section
 */
@Composable
private fun AudioVisualizerSection(
    isPlaying: Boolean,
    audioFormat: String,
    sampleRate: Int,
    bitDepth: Int
) {
    CyberpunkPanel(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AUDIO SPECTRUM ANALYZER",
                    style = FTLAudioTypography.librarySection,
                    color = SubcoderColors.Cyan,
                    fontFamily = OrbitronFontFamily
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormatBadge(audioFormat)
                    QualityBadge("${sampleRate}Hz/${bitDepth}bit")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Spectrum visualizer
            SpectrumVisualizer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                isPlaying = isPlaying
            )
        }
    }
}

/**
 * Now playing widget
 */
@Composable
private fun NowPlayingWidget(
    track: Any, // TrackInfo type would be defined elsewhere
    audioQuality: Any, // AudioQualityState type would be defined elsewhere
    onNavigateToPlayer: () -> Unit
) {
    CyberpunkPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToPlayer() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art placeholder with glow
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SubcoderColors.Cyan.copy(alpha = 0.3f),
                                SubcoderColors.PureBlack
                            )
                        )
                    )
                    .drawBehind {
                        drawRect(
                            color = SubcoderColors.Cyan,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = SubcoderColors.Cyan,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "NEURAL PATHWAY - ACTIVE",
                    style = FTLAudioTypography.trackTitleMedium,
                    color = SubcoderColors.Cyan,
                    maxLines = 1
                )
                Text(
                    text = "Subcoder • Cipher Matrix",
                    style = FTLAudioTypography.trackArtist,
                    color = SubcoderColors.LightGrey,
                    maxLines = 1
                )
                Text(
                    text = "Hi-Res FLAC • 48kHz/24bit",
                    style = FTLAudioTypography.bitDepthDisplay,
                    color = SubcoderColors.Orange,
                    maxLines = 1
                )
            }
            
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Navigate to player",
                tint = SubcoderColors.Cyan,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Now playing placeholder when nothing is playing
 */
@Composable
private fun NowPlayingPlaceholder(
    onNavigateToLibrary: () -> Unit
) {
    CyberpunkPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToLibrary() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.LibraryMusic,
                contentDescription = null,
                tint = SubcoderColors.LightGrey.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "NO ACTIVE AUDIO STREAM",
                style = FTLAudioTypography.settingTitle,
                color = SubcoderColors.LightGrey.copy(alpha = 0.7f),
                fontFamily = OrbitronFontFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Tap to access Audio Archive",
                style = FTLAudioTypography.settingDescription,
                color = SubcoderColors.LightGrey.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Quick EQ controls widget
 */
@Composable
private fun QuickEQControls(
    modifier: Modifier = Modifier,
    onNavigateToEQ: () -> Unit
) {
    CyberpunkPanel(
        modifier = modifier.clickable { onNavigateToEQ() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EQ MATRIX",
                    style = FTLAudioTypography.librarySection,
                    color = SubcoderColors.Orange,
                    fontFamily = OrbitronFontFamily
                )
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = null,
                    tint = SubcoderColors.Orange,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Mini EQ visualization
            MiniEQVisualizer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "32-BAND PARAMETRIC",
                style = FTLAudioTypography.settingDescription,
                color = SubcoderColors.LightGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Library statistics widget
 */
@Composable
private fun LibraryStatsWidget(
    modifier: Modifier = Modifier,
    trackCount: Int,
    onNavigateToLibrary: () -> Unit
) {
    CyberpunkPanel(
        modifier = modifier.clickable { onNavigateToLibrary() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIBRARY",
                    style = FTLAudioTypography.librarySection,
                    color = SubcoderColors.ElectricBlue,
                    fontFamily = OrbitronFontFamily
                )
                Icon(
                    imageVector = Icons.Filled.LibraryMusic,
                    contentDescription = null,
                    tint = SubcoderColors.ElectricBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = trackCount.toString(),
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = SubcoderColors.ElectricBlue
                )
                Text(
                    text = "AUDIO FILES",
                    style = FTLAudioTypography.settingDescription,
                    color = SubcoderColors.LightGrey,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormatBadge("FLAC")
                    FormatBadge("DSD")
                    FormatBadge("WAV")
                }
            }
        }
    }
}

/**
 * System monitoring section
 */
@Composable
private fun SystemMonitoringSection(
    isAudioServiceConnected: Boolean,
    hasRequiredPermissions: Boolean,
    onNavigateToSettings: () -> Unit
) {
    CyberpunkPanel(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "SYSTEM STATUS",
                style = FTLAudioTypography.librarySection,
                color = SubcoderColors.NeonGreen,
                fontFamily = OrbitronFontFamily
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SystemStatusItem(
                    label = "AUDIO SERVICE",
                    status = if (isAudioServiceConnected) "ONLINE" else "OFFLINE",
                    color = if (isAudioServiceConnected) SubcoderColors.SuccessGreen else SubcoderColors.ErrorRed
                )
                
                SystemStatusItem(
                    label = "PERMISSIONS",
                    status = if (hasRequiredPermissions) "GRANTED" else "REQUIRED",
                    color = if (hasRequiredPermissions) SubcoderColors.SuccessGreen else SubcoderColors.WarningYellow
                )
                
                SystemStatusItem(
                    label = "DSP ENGINE",
                    status = "ACTIVE",
                    color = SubcoderColors.SuccessGreen
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            FTLButton(
                text = "System Configuration",
                onClick = onNavigateToSettings,
                variant = FTLButtonVariant.GHOST,
                size = FTLButtonSize.SMALL,
                fullWidth = true,
                icon = Icons.Filled.Settings
            )
        }
    }
}

// Supporting composables and components would continue here...
// Including: NeuralNetworkBackground, CyberpunkPanel, SpectrumVisualizer, 
// MiniEQVisualizer, StatusIndicator, FormatBadge, QualityBadge, SystemStatusItem

/**
 * Neural network background animation
 */
@Composable
private fun NeuralNetworkBackground(
    modifier: Modifier = Modifier,
    animationOffset: Float,
    alpha: Float
) {
    Canvas(modifier = modifier) {
        drawNeuralNetwork(
            size = size,
            offset = animationOffset,
            alpha = alpha
        )
    }
}

/**
 * Draw neural network pattern
 */
private fun DrawScope.drawNeuralNetwork(
    size: Size,
    offset: Float,
    alpha: Float
) {
    val nodeCount = 20
    val nodes = remember {
        (0 until nodeCount).map {
            Offset(
                Random.nextFloat() * size.width,
                Random.nextFloat() * size.height
            )
        }
    }
    
    // Draw connections
    for (i in nodes.indices) {
        for (j in i + 1 until nodes.size) {
            val distance = (nodes[i] - nodes[j]).getDistance()
            if (distance < 200f) {
                val connectionAlpha = (alpha * (1 - distance / 200f) * 0.3f).coerceIn(0f, 1f)
                drawLine(
                    color = SubcoderColors.Cyan.copy(alpha = connectionAlpha),
                    start = nodes[i],
                    end = nodes[j],
                    strokeWidth = 0.5.dp.toPx()
                )
            }
        }
    }
    
    // Draw nodes
    nodes.forEachIndexed { index, node ->
        val nodeAlpha = alpha * (0.5f + sin(offset * 2 * PI + index * 0.5f) * 0.3f).toFloat()
        drawCircle(
            color = SubcoderColors.Orange.copy(alpha = nodeAlpha),
            radius = 2.dp.toPx(),
            center = node
        )
    }
}

/**
 * Cyberpunk panel wrapper with glow effects
 */
@Composable
private fun CyberpunkPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SubcoderColors.DarkGrey.copy(alpha = 0.3f),
                        SubcoderColors.PureBlack.copy(alpha = 0.8f)
                    )
                )
            )
            .drawBehind {
                drawRoundRect(
                    color = SubcoderColors.Cyan.copy(alpha = 0.3f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
    ) {
        content()
    }
}

/**
 * Spectrum visualizer component
 */
@Composable
private fun SpectrumVisualizer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean
) {
    Canvas(modifier = modifier) {
        drawSpectrumBars(
            size = size,
            isPlaying = isPlaying
        )
    }
}

/**
 * Draw spectrum analyzer bars
 */
private fun DrawScope.drawSpectrumBars(
    size: Size,
    isPlaying: Boolean
) {
    val barCount = 32
    val barWidth = size.width / barCount
    
    for (i in 0 until barCount) {
        val barHeight = if (isPlaying) {
            Random.nextFloat() * size.height * 0.8f
        } else {
            size.height * 0.1f
        }
        
        val x = i * barWidth
        val color = when {
            i < 8 -> SubcoderColors.Orange
            i < 24 -> SubcoderColors.Cyan
            else -> SubcoderColors.NeonGreen
        }
        
        drawRect(
            color = color.copy(alpha = 0.8f),
            topLeft = Offset(x, size.height - barHeight),
            size = Size(barWidth * 0.8f, barHeight)
        )
    }
}

/**
 * Mini EQ visualizer
 */
@Composable
private fun MiniEQVisualizer(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawMiniEQ(size = size)
    }
}

/**
 * Draw mini EQ representation
 */
private fun DrawScope.drawMiniEQ(size: Size) {
    val bandCount = 8
    val bandWidth = size.width / bandCount
    
    for (i in 0 until bandCount) {
        val bandHeight = size.height * (0.3f + Random.nextFloat() * 0.4f)
        val x = i * bandWidth
        
        drawRect(
            color = SubcoderColors.Orange.copy(alpha = 0.6f),
            topLeft = Offset(x, (size.height - bandHeight) / 2),
            size = Size(bandWidth * 0.7f, bandHeight)
        )
    }
}

/**
 * Status indicator component
 */
@Composable
private fun StatusIndicator(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = value,
            style = FTLAudioTypography.sampleRateDisplay,
            color = color,
            fontFamily = OrbitronFontFamily
        )
        Text(
            text = label,
            style = FTLAudioTypography.eqBandLabel,
            color = SubcoderColors.LightGrey
        )
    }
}

/**
 * Format badge component
 */
@Composable
private fun FormatBadge(format: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SubcoderColors.Cyan.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = format,
            style = FTLAudioTypography.formatBadgeSmall,
            color = SubcoderColors.Cyan
        )
    }
}

/**
 * Quality badge component
 */
@Composable
private fun QualityBadge(quality: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SubcoderColors.Orange.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = quality,
            style = FTLAudioTypography.formatBadgeSmall,
            color = SubcoderColors.Orange
        )
    }
}

/**
 * System status item component
 */
@Composable
private fun SystemStatusItem(
    label: String,
    status: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = status,
            style = FTLAudioTypography.eqValueDisplay,
            color = color,
            fontFamily = OrbitronFontFamily
        )
        Text(
            text = label,
            style = FTLAudioTypography.eqBandLabel,
            color = SubcoderColors.LightGrey
        )
    }
}