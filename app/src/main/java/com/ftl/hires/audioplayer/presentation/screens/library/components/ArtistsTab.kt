package com.ftl.hires.audioplayer.presentation.screens.library.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.ftl.hires.audioplayer.data.database.entities.Artist
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import com.ftl.hires.audioplayer.presentation.theme.FTLAudioTypography

/**
 * Artists Tab - Audio Source Directory
 * 
 * Features:
 * - Grid/List view of all artists
 * - Play count and track count statistics
 * - Artist artwork display
 * - Hi-res content indicators
 * - Cyberpunk visual styling
 */
@Composable
fun ArtistsTab(
    artists: List<Artist>,
    onArtistSelected: (Artist) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (artists.isEmpty() && !isLoading) {
            item {
                EmptyArtistsState()
            }
        } else {
            items(
                items = artists,
                key = { it.id }
            ) { artist ->
                ArtistItem(
                    artist = artist,
                    onArtistSelected = { onArtistSelected(artist) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

/**
 * Individual artist item
 */
@Composable
private fun ArtistItem(
    artist: Artist,
    onArtistSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    CyberpunkPanel(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onArtistSelected() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artist avatar/icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SubcoderColors.ElectricBlue.copy(alpha = 0.3f),
                                SubcoderColors.Cyan.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .drawBehind {
                        drawCircle(
                            color = SubcoderColors.ElectricBlue.copy(alpha = 0.5f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // TODO: Replace with actual artist image when available
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "${artist.name} avatar",
                    tint = SubcoderColors.ElectricBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Artist info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Artist name
                Text(
                    text = artist.name,
                    style = FTLAudioTypography.trackTitleMedium,
                    color = SubcoderColors.OffWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Genre info
                artist.genre?.let { genre ->
                    Text(
                        text = genre,
                        style = FTLAudioTypography.trackArtist,
                        color = SubcoderColors.Orange,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                
                // Stats row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Track count
                    StatBadge(
                        value = "${artist.trackCount} tracks",
                        color = SubcoderColors.Cyan
                    )
                    
                    // Play count if > 0
                    if (artist.playCount > 0) {
                        StatBadge(
                            value = "${artist.playCount} plays",
                            color = SubcoderColors.NeonGreen
                        )
                    }
                    
                    // Hi-res indicator if artist has hi-res content
                    if (artist.hasHiResContent) {
                        HiResIndicator()
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Navigation arrow and last played info
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View artist",
                    tint = SubcoderColors.LightGrey.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                
                artist.lastPlayed?.let { lastPlayed ->
                    Spacer(modifier = Modifier.height(4.dp))
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
 * Empty state for artists
 */
@Composable
private fun EmptyArtistsState() {
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
                imageVector = Icons.Default.PersonOff,
                contentDescription = null,
                tint = SubcoderColors.LightGrey.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NO ARTISTS FOUND",
                style = FTLAudioTypography.settingTitle,
                color = SubcoderColors.LightGrey.copy(alpha = 0.7f),
                fontFamily = OrbitronFontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No audio sources detected in your archive.\nAdd music files to populate the artist directory.",
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
                    color = SubcoderColors.ElectricBlue.copy(alpha = 0.2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 0.5.dp.toPx())
                )
            }
    ) {
        content()
    }
}

/**
 * Format relative time (e.g., "2 hours ago", "3 days ago")
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