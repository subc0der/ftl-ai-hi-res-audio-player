package com.ftl.hires.audioplayer.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ftl.hires.audioplayer.audio.AudioController
import com.ftl.hires.audioplayer.data.database.entities.Track
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import com.ftl.hires.audioplayer.presentation.theme.InterFontFamily
import com.ftl.hires.audioplayer.presentation.theme.Pixel9ProDimensions
import kotlinx.coroutines.flow.StateFlow

/**
 * Now Playing Bar - Persistent audio controls at the bottom of the app
 * 
 * Features:
 * - Play/Pause/Stop controls
 * - Volume slider
 * - Track info display
 * - Progress bar
 * - Skip controls
 */
@Composable
fun NowPlayingBar(
    audioController: AudioController,
    onExpandClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentTrack by audioController.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by audioController.isPlaying.collectAsStateWithLifecycle()
    val playbackPosition by audioController.playbackPosition.collectAsStateWithLifecycle()
    val playbackDuration by audioController.playbackDuration.collectAsStateWithLifecycle()
    val isBuffering by audioController.isBuffering.collectAsStateWithLifecycle()
    val volume by audioController.volume.collectAsStateWithLifecycle()
    val dimensions = Pixel9ProDimensions.default
    
    // Volume UI state
    var showVolumeSlider by remember { mutableStateOf(false) }
    
    AnimatedVisibility(
        visible = currentTrack != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.nowPlayingBarHeight),
            colors = CardDefaults.cardColors(
                containerColor = SubcoderColors.DarkGrey.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                SubcoderColors.ElectricBlue.copy(alpha = 0.1f),
                                SubcoderColors.DarkGrey.copy(alpha = 0.95f)
                            )
                        )
                    )
            ) {
                // Progress bar
                if (playbackDuration > 0) {
                    LinearProgressIndicator(
                        progress = playbackPosition.toFloat() / playbackDuration.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color = SubcoderColors.ElectricBlue,
                        trackColor = SubcoderColors.DarkGrey
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Track info section
                    currentTrack?.let { track ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onExpandClick() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Album art placeholder
                            Card(
                                modifier = Modifier.size(44.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = SubcoderColors.ElectricBlue.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = SubcoderColors.ElectricBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Track details
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = track.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SubcoderColors.OffWhite,
                                    fontFamily = InterFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = track.artistName ?: "Unknown Artist",
                                    fontSize = 12.sp,
                                    color = SubcoderColors.LightGrey,
                                    fontFamily = InterFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    // Playback controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Previous
                        IconButton(
                            onClick = { audioController.skipToPrevious() },
                            modifier = Modifier.size(dimensions.smallIconButtonSize)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = SubcoderColors.Cyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // Play/Pause
                        FilledIconButton(
                            onClick = { audioController.togglePlayPause() },
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = SubcoderColors.ElectricBlue
                            )
                        ) {
                            if (isBuffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = SubcoderColors.OffWhite,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = SubcoderColors.OffWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        // Stop
                        IconButton(
                            onClick = { audioController.stop() },
                            modifier = Modifier.size(dimensions.smallIconButtonSize)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = SubcoderColors.WarningOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // Next
                        IconButton(
                            onClick = { audioController.skipToNext() },
                            modifier = Modifier.size(dimensions.smallIconButtonSize)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = SubcoderColors.Cyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // Volume control
                        IconButton(
                            onClick = { showVolumeSlider = !showVolumeSlider },
                            modifier = Modifier.size(dimensions.smallIconButtonSize)
                        ) {
                            Icon(
                                imageVector = when {
                                    volume == 0f -> Icons.Default.VolumeOff
                                    volume < 0.5f -> Icons.Default.VolumeDown
                                    else -> Icons.Default.VolumeUp
                                },
                                contentDescription = "Volume",
                                tint = SubcoderColors.Orange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Volume slider popup - positioned above the Now Playing bar
        if (showVolumeSlider && currentTrack != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-140).dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                VolumeSliderPopup(
                    volume = volume,
                    onVolumeChange = { audioController.setVolume(it) },
                    onDismiss = { showVolumeSlider = false }
                )
            }
        }
    }
}

/**
 * Volume slider popup overlay
 */
@Composable
private fun VolumeSliderPopup(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .padding(end = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SubcoderColors.DarkGrey.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "VOLUME",
                fontSize = 12.sp,
                fontFamily = OrbitronFontFamily,
                color = SubcoderColors.Orange,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeDown,
                    contentDescription = null,
                    tint = SubcoderColors.LightGrey,
                    modifier = Modifier.size(16.dp)
                )
                
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = SubcoderColors.Orange,
                        activeTrackColor = SubcoderColors.Orange,
                        inactiveTrackColor = SubcoderColors.DarkGrey
                    )
                )
                
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = SubcoderColors.LightGrey,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${(volume * 100).toInt()}%",
                fontSize = 14.sp,
                fontFamily = OrbitronFontFamily,
                color = SubcoderColors.OffWhite
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { onVolumeChange(0f) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = SubcoderColors.WarningOrange
                    )
                ) {
                    Text(
                        text = "MUTE",
                        fontSize = 11.sp,
                        fontFamily = OrbitronFontFamily
                    )
                }
                
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = SubcoderColors.Cyan
                    )
                ) {
                    Text(
                        text = "CLOSE",
                        fontSize = 11.sp,
                        fontFamily = OrbitronFontFamily
                    )
                }
            }
        }
    }
}