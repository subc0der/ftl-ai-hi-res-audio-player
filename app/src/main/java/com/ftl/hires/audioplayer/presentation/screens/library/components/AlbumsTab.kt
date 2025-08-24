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
import com.ftl.hires.audioplayer.data.database.entities.Album
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import com.ftl.hires.audioplayer.presentation.theme.FTLAudioTypography

/**
 * Albums Tab - Audio Archives Collection
 * 
 * Features:
 * - List view of all albums with artwork
 * - Album metadata and statistics
 * - Year and genre information
 * - Hi-res content indicators
 * - Track count and duration display
 * - Cyberpunk visual styling
 */
@Composable
fun AlbumsTab(
    albums: List<Album>,
    onAlbumSelected: (Album) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (albums.isEmpty() && !isLoading) {
            item {
                EmptyAlbumsState()
            }
        } else {
            items(
                items = albums,
                key = { it.id }
            ) { album ->
                AlbumItem(
                    album = album,
                    onAlbumSelected = { onAlbumSelected(album) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

/**
 * Individual album item
 */
@Composable
private fun AlbumItem(
    album: Album,
    onAlbumSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    CyberpunkPanel(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAlbumSelected() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album artwork placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SubcoderColors.Orange.copy(alpha = 0.3f),
                                SubcoderColors.ElectricBlue.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .drawBehind {
                        drawRoundRect(
                            color = SubcoderColors.Orange.copy(alpha = 0.5f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // TODO: Replace with actual album artwork when available
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = "${album.title} artwork",
                    tint = SubcoderColors.Orange,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Album info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Album title with hi-res indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = album.title,
                        style = FTLAudioTypography.trackTitleMedium,
                        color = SubcoderColors.OffWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (album.hasHiResContent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        HiResIndicator()
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Artist name
                album.artistName?.let { artistName ->
                    Text(
                        text = artistName,
                        style = FTLAudioTypography.trackArtist,
                        color = SubcoderColors.Cyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                
                // Album details row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Year
                    album.year?.let { year ->
                        YearBadge(year.toString())
                    }
                    
                    // Genre
                    album.genre?.let { genre ->
                        GenreBadge(genre)
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Statistics row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Track count
                    StatBadge(
                        value = "${album.trackCount} tracks",
                        color = SubcoderColors.ElectricBlue
                    )
                    
                    // Duration
                    album.totalDuration?.let { duration ->
                        StatBadge(
                            value = formatDuration(duration),
                            color = SubcoderColors.LightGrey
                        )
                    }
                    
                    // Play count if > 0
                    if (album.playCount > 0) {
                        StatBadge(
                            value = "${album.playCount} plays",
                            color = SubcoderColors.NeonGreen
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Navigation and last played
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View album",
                    tint = SubcoderColors.LightGrey.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                
                album.lastPlayed?.let { lastPlayed ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatRelativeTime(lastPlayed),
                        style = FTLAudioTypography.formatBadgeSmall,
                        color = SubcoderColors.LightGrey.copy(alpha = 0.6f),
                        fontFamily = OrbitronFontFamily
                    )
                }
            }
        }
    }
}

/**
 * Year badge
 */
@Composable
private fun YearBadge(year: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SubcoderColors.Cyan.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = year,
            style = FTLAudioTypography.formatBadgeSmall,
            color = SubcoderColors.Cyan
        )
    }
}

/**
 * Genre badge
 */
@Composable
private fun GenreBadge(genre: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SubcoderColors.Orange.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = genre.uppercase(),
            style = FTLAudioTypography.formatBadgeSmall,
            color = SubcoderColors.Orange
        )
    }
}

/**
 * Statistics badge
 */
@Composable
private fun StatBadge(
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = value,
            style = FTLAudioTypography.formatBadgeSmall,
            color = color
        )
    }
}

/**
 * Hi-res content indicator
 */
@Composable
private fun HiResIndicator() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        SubcoderColors.Orange,
                        SubcoderColors.Orange.copy(alpha = 0.8f)
                    )
                )
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
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
 * Empty state for albums
 */
@Composable
private fun EmptyAlbumsState() {
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
                imageVector = Icons.Default.AlbumOff,
                contentDescription = null,
                tint = SubcoderColors.LightGrey.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NO ALBUMS DETECTED",
                style = FTLAudioTypography.settingTitle,
                color = SubcoderColors.LightGrey.copy(alpha = 0.7f),
                fontFamily = OrbitronFontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No audio archives found in your collection.\nAdd organized music albums to populate this archive.",
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
                    color = SubcoderColors.Orange.copy(alpha = 0.2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 0.5.dp.toPx())
                )
            }
    ) {
        content()
    }
}

/**
 * Format duration from milliseconds
 */
private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    
    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}

/**
 * Format relative time
 */
private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> "${diff / 604800_000}w ago"
    }
}