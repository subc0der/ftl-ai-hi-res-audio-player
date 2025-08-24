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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ftl.hires.audioplayer.data.database.entities.Playlist
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import com.ftl.hires.audioplayer.presentation.theme.FTLAudioTypography

/**
 * Playlists Tab - Audio Sequences Collection
 * 
 * Features:
 * - List view of all playlists
 * - System and user playlists
 * - Playlist statistics and metadata
 * - Creation date and last modified info
 * - Track count and duration display
 * - Cyberpunk visual styling
 */
@Composable
fun PlaylistsTab(
    playlists: List<Playlist>,
    onPlaylistSelected: (Playlist) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (playlists.isEmpty() && !isLoading) {
            item {
                EmptyPlaylistsState()
            }
        } else {
            // System playlists first
            val systemPlaylists = playlists.filter { it.isSystemPlaylist }
            val userPlaylists = playlists.filter { !it.isSystemPlaylist }
            
            if (systemPlaylists.isNotEmpty()) {
                item {
                    SectionHeader("SYSTEM SEQUENCES")
                }
                
                items(
                    items = systemPlaylists,
                    key = { it.id }
                ) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        onPlaylistSelected = { onPlaylistSelected(playlist) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                
                if (userPlaylists.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader("USER SEQUENCES")
                    }
                }
            }
            
            items(
                items = userPlaylists,
                key = { it.id }
            ) { playlist ->
                PlaylistItem(
                    playlist = playlist,
                    onPlaylistSelected = { onPlaylistSelected(playlist) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

/**
 * Section header for playlist categories
 */
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = FTLAudioTypography.librarySection,
        color = SubcoderColors.NeonGreen,
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * Individual playlist item
 */
@Composable
private fun PlaylistItem(
    playlist: Playlist,
    onPlaylistSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    CyberpunkPanel(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPlaylistSelected() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Playlist icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                getPlaylistColor(playlist).copy(alpha = 0.3f),
                                getPlaylistColor(playlist).copy(alpha = 0.1f)
                            )
                        )
                    )
                    .drawBehind {
                        drawRoundRect(
                            color = getPlaylistColor(playlist).copy(alpha = 0.5f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getPlaylistIcon(playlist),
                    contentDescription = "${playlist.name} icon",
                    tint = getPlaylistColor(playlist),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Playlist info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Playlist name
                Text(
                    text = playlist.name,
                    style = FTLAudioTypography.trackTitleMedium,
                    color = SubcoderColors.OffWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Description or auto-generated info
                val description = playlist.description ?: generatePlaylistDescription(playlist)
                Text(
                    text = description,
                    style = FTLAudioTypography.trackArtist,
                    color = SubcoderColors.LightGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Statistics row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Track count
                    StatBadge(
                        value = "${playlist.trackCount} tracks",
                        color = getPlaylistColor(playlist)
                    )
                    
                    // Duration
                    playlist.totalDuration?.let { duration ->
                        StatBadge(
                            value = formatDuration(duration),
                            color = SubcoderColors.LightGrey
                        )
                    }
                    
                    // System playlist indicator
                    if (playlist.isSystemPlaylist) {
                        SystemBadge()
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Navigation and timestamps
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View playlist",
                    tint = SubcoderColors.LightGrey.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Last modified or created
                val timestamp = playlist.lastModified ?: playlist.createdAt
                Text(
                    text = formatRelativeTime(timestamp),
                    style = FTLAudioTypography.formatBadgeSmall,
                    color = SubcoderColors.LightGrey.copy(alpha = 0.6f),
                    fontFamily = OrbitronFontFamily
                )
            }
        }
    }
}

/**
 * Get appropriate icon for playlist type
 */
private fun getPlaylistIcon(playlist: Playlist): ImageVector {
    return when {
        playlist.isSystemPlaylist -> when (playlist.name.lowercase()) {
            "favorites", "favourites" -> Icons.Default.Favorite
            "recently played" -> Icons.Default.History
            "most played" -> Icons.Default.TrendingUp
            "recently added" -> Icons.Default.NewReleases
            "hi-res" -> Icons.Default.HighQuality
            else -> Icons.Default.PlaylistPlay
        }
        else -> Icons.Outlined.PlaylistPlay
    }
}

/**
 * Get color for playlist based on type
 */
private fun getPlaylistColor(playlist: Playlist): androidx.compose.ui.graphics.Color {
    return when {
        playlist.isSystemPlaylist -> when (playlist.name.lowercase()) {
            "favorites", "favourites" -> SubcoderColors.Orange
            "recently played" -> SubcoderColors.ElectricBlue
            "most played" -> SubcoderColors.NeonGreen
            "recently added" -> SubcoderColors.Cyan
            "hi-res" -> SubcoderColors.Orange
            else -> SubcoderColors.NeonGreen
        }
        else -> SubcoderColors.ElectricBlue
    }
}

/**
 * Generate description for system playlists
 */
private fun generatePlaylistDescription(playlist: Playlist): String {
    return when {
        playlist.isSystemPlaylist -> when (playlist.name.lowercase()) {
            "favorites", "favourites" -> "Your favorited audio tracks"
            "recently played" -> "Recently played audio files"
            "most played" -> "Most frequently played tracks"
            "recently added" -> "Newly added to archive"
            "hi-res" -> "High-resolution audio collection"
            else -> "System-generated playlist"
        }
        else -> "Custom audio sequence"
    }
}

/**
 * System playlist badge
 */
@Composable
private fun SystemBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SubcoderColors.NeonGreen.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "SYSTEM",
            style = FTLAudioTypography.formatBadgeSmall,
            color = SubcoderColors.NeonGreen,
            fontWeight = FontWeight.Bold
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
 * Empty state for playlists
 */
@Composable
private fun EmptyPlaylistsState() {
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
                imageVector = Icons.Outlined.PlaylistAdd,
                contentDescription = null,
                tint = SubcoderColors.LightGrey.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NO AUDIO SEQUENCES",
                style = FTLAudioTypography.settingTitle,
                color = SubcoderColors.LightGrey.copy(alpha = 0.7f),
                fontFamily = OrbitronFontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No playlist sequences detected.\nCreate custom playlists to organize your audio experience.",
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
                    color = SubcoderColors.NeonGreen.copy(alpha = 0.2f),
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