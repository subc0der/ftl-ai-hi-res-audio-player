package com.ftl.hires.audioplayer.presentation.screens.library

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import com.ftl.hires.audioplayer.presentation.theme.FTLAudioTypography
import com.ftl.hires.audioplayer.presentation.components.FTLSearchBar
import com.ftl.hires.audioplayer.presentation.screens.library.components.*
import com.ftl.hires.audioplayer.data.repository.MediaScannerRepository

/**
 * AudioArchive Screen - FTL Hi-Res Audio Library
 * 
 * Cyberpunk-themed music library with:
 * - Tabbed navigation (Tracks/Artists/Albums/Playlists)
 * - Advanced search with format filtering
 * - Hi-res audio indicators throughout
 * - Statistics and analytics
 * - Neural network visual effects
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioArchiveScreen(
    onTrackSelected: (String) -> Unit,
    onPlaylistSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val libraryStats by viewModel.libraryStats.collectAsStateWithLifecycle()
    
    val tracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
    val artists by viewModel.filteredArtists.collectAsStateWithLifecycle()
    val albums by viewModel.filteredAlbums.collectAsStateWithLifecycle()
    val playlists by viewModel.filteredPlaylists.collectAsStateWithLifecycle()
    
    // Scanner states
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    
    // Check if library is empty
    val isLibraryEmpty = tracks.isEmpty() && artists.isEmpty() && albums.isEmpty() && !uiState.isLoading

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SubcoderColors.PureBlack)
    ) {
        // Audio Archive Header with Search
        AudioArchiveHeader(
            searchQuery = searchQuery,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            libraryStats = libraryStats,
            modifier = Modifier.padding(16.dp)
        )

        // Tab Navigation
        AudioArchiveTabRow(
            selectedTab = selectedTab,
            onTabSelected = viewModel::onTabSelected,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Content based on selected tab
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (isLibraryEmpty && !isScanning) {
                // Show empty state with scan options
                LibraryEmptyState(
                    isScanning = isScanning,
                    scanProgress = scanProgress,
                    onStartScan = viewModel::startLibraryScan,
                    onStartQuickScan = viewModel::startQuickScan,
                    onStopScan = viewModel::stopScan,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                when (selectedTab) {
                    LibraryTab.TRACKS -> {
                        TracksTab(
                            tracks = tracks,
                            onTrackSelected = { track ->
                                viewModel.onTrackSelected(track)
                                onTrackSelected(track.id)
                            },
                            onToggleFavorite = viewModel::toggleTrackFavorite,
                            isLoading = uiState.isLoading,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LibraryTab.ARTISTS -> {
                        ArtistsTab(
                            artists = artists,
                            onArtistSelected = viewModel::onArtistSelected,
                            isLoading = uiState.isLoading,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LibraryTab.ALBUMS -> {
                        AlbumsTab(
                            albums = albums,
                            onAlbumSelected = viewModel::onAlbumSelected,
                            isLoading = uiState.isLoading,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LibraryTab.PLAYLISTS -> {
                        PlaylistsTab(
                            playlists = playlists,
                            onPlaylistSelected = { playlist ->
                                viewModel.onPlaylistSelected(playlist)
                                onPlaylistSelected(playlist.id)
                            },
                            isLoading = uiState.isLoading,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                // Show scan progress overlay when scanning with existing content
                if (isScanning && !isLibraryEmpty) {
                    ScanProgressOverlay(
                        scanProgress = scanProgress,
                        onStopScan = viewModel::stopScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            // Loading overlay
            if (uiState.isLoading) {
                LoadingOverlay(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Handle errors
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or error dialog
            // For now, just clear the error after a delay
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }
}

/**
 * Audio Archive header with search and stats
 */
@Composable
private fun AudioArchiveHeader(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    libraryStats: LibraryStatsExtended,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Title and stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "AUDIO ARCHIVE",
                    style = FTLAudioTypography.librarySection,
                    color = SubcoderColors.ElectricBlue,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Digital music collection matrix",
                    style = FTLAudioTypography.settingDescription,
                    color = SubcoderColors.LightGrey
                )
            }
            
            LibraryStatsCompact(
                stats = libraryStats
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search bar
        FTLSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChanged,
            placeholder = "Search audio archive...",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Cyberpunk tab row for library navigation
 */
@Composable
private fun AudioArchiveTabRow(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LibraryTab.values().forEach { tab ->
            AudioArchiveTab(
                tab = tab,
                isSelected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual library tab with cyberpunk styling
 */
@Composable
private fun AudioArchiveTab(
    tab: LibraryTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                SubcoderColors.Cyan.copy(alpha = 0.2f),
                SubcoderColors.ElectricBlue.copy(alpha = 0.1f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                SubcoderColors.DarkGrey.copy(alpha = 0.3f),
                SubcoderColors.PureBlack.copy(alpha = 0.8f)
            )
        )
    }
    
    val textColor = if (isSelected) SubcoderColors.Cyan else SubcoderColors.LightGrey
    val iconColor = if (isSelected) SubcoderColors.Cyan else SubcoderColors.LightGrey

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getTabIcon(tab),
                contentDescription = tab.displayName,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tab.displayName.uppercase(),
                style = FTLAudioTypography.eqBandLabel,
                color = textColor,
                textAlign = TextAlign.Center,
                fontFamily = OrbitronFontFamily
            )
        }
    }
}

/**
 * Get appropriate icon for each tab
 */
private fun getTabIcon(tab: LibraryTab): ImageVector {
    return when (tab) {
        LibraryTab.TRACKS -> Icons.Outlined.MusicNote
        LibraryTab.ARTISTS -> Icons.Outlined.Person
        LibraryTab.ALBUMS -> Icons.Outlined.Album
        LibraryTab.PLAYLISTS -> Icons.Outlined.PlaylistPlay
    }
}

/**
 * Compact library statistics display
 */
@Composable
private fun LibraryStatsCompact(
    stats: LibraryStatsExtended,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatItem(
            value = stats.totalTracks,
            label = "TRACKS",
            color = SubcoderColors.Cyan
        )
        StatItem(
            value = stats.hiResCount,
            label = "HI-RES",
            color = SubcoderColors.Orange
        )
        StatItem(
            value = stats.totalArtists,
            label = "ARTISTS",
            color = SubcoderColors.ElectricBlue
        )
    }
}

/**
 * Individual stat item
 */
@Composable
private fun StatItem(
    value: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = value.toString(),
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
 * Scan progress overlay for when scanning with existing content
 */
@Composable
private fun ScanProgressOverlay(
    scanProgress: MediaScannerRepository.ScanProgress?,
    onStopScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = SubcoderColors.DarkGrey.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = SubcoderColors.ElectricBlue,
                    strokeWidth = 2.dp,
                    progress = scanProgress?.let { progress ->
                        if (progress.totalFiles > 0) {
                            progress.filesScanned.toFloat() / progress.totalFiles
                        } else 0f
                    } ?: 0f
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Scanning library...",
                        style = FTLAudioTypography.bodyMedium,
                        color = SubcoderColors.White,
                        fontWeight = FontWeight.Medium
                    )
                    
                    scanProgress?.let { progress ->
                        Text(
                            text = if (progress.totalFiles > 0) {
                                "${progress.filesScanned}/${progress.totalFiles} files"
                            } else {
                                "${progress.filesScanned} files processed"
                            },
                            style = FTLAudioTypography.bodySmall,
                            color = SubcoderColors.LightGrey
                        )
                    }
                }
            }
            
            IconButton(
                onClick = onStopScan
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Stop scan",
                    tint = SubcoderColors.WarningOrange
                )
            }
        }
    }
}

/**
 * Loading overlay with cyberpunk animation
 */
@Composable
private fun LoadingOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(SubcoderColors.PureBlack.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = SubcoderColors.Cyan,
                strokeWidth = 2.dp,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "LOADING AUDIO ARCHIVE...",
                style = FTLAudioTypography.settingDescription,
                color = SubcoderColors.Cyan,
                fontFamily = OrbitronFontFamily
            )
        }
    }
}