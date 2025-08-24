package com.ftl.hires.audioplayer.presentation.screens.library.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ftl.hires.audioplayer.data.database.entities.Track
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import com.ftl.hires.audioplayer.presentation.theme.FTLAudioTypography

/**
 * Tracks Tab - Hi-Res Audio Track List
 * 
 * Features:
 * - List view of all tracks with metadata
 * - Hi-res format indicators
 * - Favorite toggle functionality
 * - Play count and duration display
 * - Cyberpunk visual styling
 */
@Composable
fun TracksTab(
    tracks: List<Track>,
    onTrackSelected: (Track) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (tracks.isEmpty() && !isLoading) {
            item {
                EmptyTracksState()
            }
        } else {
            items(
                items = tracks,
                key = { it.id }
            ) { track ->
                TrackItem(
                    track = track,
                    onTrackSelected = { onTrackSelected(track) },
                    onToggleFavorite = { onToggleFavorite(track) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

/**
 * Individual track item with hi-res indicators
 */
@Composable
private fun TrackItem(
    track: Track,
    onTrackSelected: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    CyberpunkPanel(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTrackSelected() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track number or play icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (track.playCount > 0) {
                            SubcoderColors.Cyan.copy(alpha = 0.2f)
                        } else {
                            SubcoderColors.DarkGrey.copy(alpha = 0.3f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (track.trackNumber != null) {
                    Text(
                        text = track.trackNumber.toString(),
                        style = FTLAudioTypography.eqValueDisplay,
                        color = if (track.playCount > 0) SubcoderColors.Cyan else SubcoderColors.LightGrey,
                        fontFamily = OrbitronFontFamily
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (track.playCount > 0) SubcoderColors.Cyan else SubcoderColors.LightGrey,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Track info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title with hi-res indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.title,
                        style = FTLAudioTypography.trackTitleMedium,
                        color = SubcoderColors.OffWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (track.isHighRes) {
                        Spacer(modifier = Modifier.width(8.dp))
                        HiResIndicator()
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Artist and album
                Row {
                    track.artistName?.let { artist ->
                        Text(
                            text = artist,
                            style = FTLAudioTypography.trackArtist,
                            color = SubcoderColors.Cyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        track.albumName?.let { album ->
                            Text(
                                text = " • $album",
                                style = FTLAudioTypography.trackArtist,
                                color = SubcoderColors.LightGrey,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Format and technical info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormatBadge(track.format)
                    
                    track.sampleRate?.let { sampleRate ->
                        track.bitDepth?.let { bitDepth ->
                            QualityBadge("${sampleRate}Hz/${bitDepth}bit")
                        }
                    }
                    
                    if (track.playCount > 0) {
                        PlayCountBadge(track.playCount)
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Duration and favorite
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatDuration(track.durationMs),
                    style = FTLAudioTypography.eqValueDisplay,
                    color = SubcoderColors.LightGrey,
                    fontFamily = OrbitronFontFamily
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (track.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (track.isFavorite) SubcoderColors.Orange else SubcoderColors.LightGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Hi-res indicator badge
 */
@Composable
private fun HiResIndicator() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        SubcoderColors.Orange,
                        SubcoderColors.Orange.copy(alpha = 0.8f)
                    )
                )
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = "HI-RES",
            style = FTLAudioTypography.formatBadgeSmall,
            color = SubcoderColors.PureBlack,
            fontWeight = FontWeight.Bold
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
            text = format.uppercase(),
            style = FTLAudioTypography.formatBadgeSmall,
            color = SubcoderColors.Cyan
        )
    }
}

/**
 * Audio quality badge
 */
@Composable
private fun QualityBadge(quality: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SubcoderColors.ElectricBlue.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = quality,
            style = FTLAudioTypography.formatBadgeSmall,
            color = SubcoderColors.ElectricBlue
        )
    }
}

/**
 * Play count badge
 */
@Composable
private fun PlayCountBadge(playCount: Long) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SubcoderColors.NeonGreen.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "${playCount}x",
            style = FTLAudioTypography.formatBadgeSmall,
            color = SubcoderColors.NeonGreen
        )
    }
}

/**
 * Empty state when no tracks found
 */
@Composable
private fun EmptyTracksState() {
    CyberpunkPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.MusicOff,
                contentDescription = null,
                tint = SubcoderColors.LightGrey.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NO AUDIO FILES DETECTED",
                style = FTLAudioTypography.settingTitle,
                color = SubcoderColors.LightGrey.copy(alpha = 0.7f),
                fontFamily = OrbitronFontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your audio archive appears to be empty.\nAdd music files to begin your sonic journey.",
                style = FTLAudioTypography.settingDescription,
                color = SubcoderColors.LightGrey.copy(alpha = 0.5f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Cyberpunk panel wrapper
 */
@Composable
private fun CyberpunkPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SubcoderColors.DarkGrey.copy(alpha = 0.2f),
                        SubcoderColors.PureBlack.copy(alpha = 0.6f)
                    )
                )
            )
            .drawBehind {
                drawRoundRect(
                    color = SubcoderColors.Cyan.copy(alpha = 0.2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 0.5.dp.toPx())
                )
            }
    ) {
        content()
    }
}

/**
 * Format duration from milliseconds to readable string
 */
private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}