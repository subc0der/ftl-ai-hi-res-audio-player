package com.ftl.hires.audioplayer.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ftl.hires.audioplayer.data.database.entities.Album
import com.ftl.hires.audioplayer.data.database.entities.Artist
import com.ftl.hires.audioplayer.data.database.entities.Track
import com.ftl.hires.audioplayer.data.database.entities.Playlist
import com.ftl.hires.audioplayer.data.database.dao.LibraryStats
import com.ftl.hires.audioplayer.data.repository.TrackRepository
import com.ftl.hires.audioplayer.data.repository.LibraryRepository
import com.ftl.hires.audioplayer.data.repository.PlaylistRepository
import com.ftl.hires.audioplayer.data.repository.MediaScannerRepository
import com.ftl.hires.audioplayer.service.MediaScannerService
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val libraryRepository: LibraryRepository,
    private val playlistRepository: PlaylistRepository,
    private val mediaScannerRepository: MediaScannerRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(LibraryTab.TRACKS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    // Scanner progress tracking
    private val _scanProgress = MutableStateFlow<MediaScannerRepository.ScanProgress?>(null)
    val scanProgress: StateFlow<MediaScannerRepository.ScanProgress?> = _scanProgress.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // Combined flow for filtered tracks based on search
    val filteredTracks: StateFlow<List<Track>> = combine(
        trackRepository.getAllTracks(),
        _searchQuery
    ) { tracks, query ->
        if (query.isBlank()) {
            tracks
        } else {
            tracks.filter { track ->
                track.title.contains(query, ignoreCase = true) ||
                track.artistName?.contains(query, ignoreCase = true) == true ||
                track.albumName?.contains(query, ignoreCase = true) == true ||
                track.genre?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Combined flow for filtered artists
    val filteredArtists: StateFlow<List<Artist>> = combine(
        libraryRepository.getAllArtists(),
        _searchQuery
    ) { artists, query ->
        if (query.isBlank()) {
            artists
        } else {
            artists.filter { artist ->
                artist.name.contains(query, ignoreCase = true) ||
                artist.genre?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Combined flow for filtered albums
    val filteredAlbums: StateFlow<List<Album>> = combine(
        libraryRepository.getAllAlbums(),
        _searchQuery
    ) { albums, query ->
        if (query.isBlank()) {
            albums
        } else {
            albums.filter { album ->
                album.title.contains(query, ignoreCase = true) ||
                album.artistName?.contains(query, ignoreCase = true) == true ||
                album.genre?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Combined flow for filtered playlists
    val filteredPlaylists: StateFlow<List<Playlist>> = combine(
        playlistRepository.getAllPlaylists(),
        _searchQuery
    ) { playlists, query ->
        if (query.isBlank()) {
            playlists
        } else {
            playlists.filter { playlist ->
                playlist.name.contains(query, ignoreCase = true) ||
                playlist.description?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // High-res tracks flow
    val hiResTrackCount: StateFlow<Int> = trackRepository.getHighResTracks()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Library stats
    val libraryStats: StateFlow<LibraryStatsExtended> = combine(
        trackRepository.getAllTracks(),
        libraryRepository.getAllArtists(),
        libraryRepository.getAllAlbums(),
        playlistRepository.getAllPlaylists()
    ) { tracks, artists, albums, playlists ->
        LibraryStatsExtended(
            totalTracks = tracks.size,
            totalArtists = artists.size,
            totalAlbums = albums.size,
            totalPlaylists = playlists.size,
            hiResCount = tracks.count { it.isHighRes },
            totalDuration = tracks.sumOf { it.durationMs },
            formats = tracks.map { it.format }.distinct().sorted()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryStatsExtended()
    )

    init {
        loadLibraryData()
    }

    private fun loadLibraryData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Load initial data - repositories handle reactive updates via Flow
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = null
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load library: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTabSelected(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun onTrackSelected(track: Track) {
        viewModelScope.launch {
            try {
                // Increment play count
                trackRepository.incrementPlayCount(track.id)
                
                // Add to current playlist queue
                playlistRepository.addToQueue(track)
                
                // Navigate to player would be handled by the UI
                _uiState.update { 
                    it.copy(selectedTrack = track) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to play track: ${e.message}") 
                }
            }
        }
    }

    fun onArtistSelected(artist: Artist) {
        viewModelScope.launch {
            try {
                // Load tracks by artist
                libraryRepository.incrementArtistPlayCount(artist.id)
                _uiState.update { 
                    it.copy(selectedArtist = artist) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to load artist: ${e.message}") 
                }
            }
        }
    }

    fun onAlbumSelected(album: Album) {
        viewModelScope.launch {
            try {
                // Load tracks by album
                libraryRepository.incrementAlbumPlayCount(album.id)
                _uiState.update { 
                    it.copy(selectedAlbum = album) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to load album: ${e.message}") 
                }
            }
        }
    }

    fun onPlaylistSelected(playlist: Playlist) {
        viewModelScope.launch {
            try {
                // Load playlist tracks
                _uiState.update { 
                    it.copy(selectedPlaylist = playlist) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to load playlist: ${e.message}") 
                }
            }
        }
    }

    fun toggleTrackFavorite(track: Track) {
        viewModelScope.launch {
            try {
                trackRepository.updateFavoriteStatus(track.id, !track.isFavorite)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to update favorite: ${e.message}") 
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSelection() {
        _uiState.update { 
            it.copy(
                selectedTrack = null,
                selectedArtist = null,
                selectedAlbum = null,
                selectedPlaylist = null
            ) 
        }
    }

    // Scanner functions
    fun startLibraryScan() {
        viewModelScope.launch {
            try {
                _isScanning.value = true
                _uiState.update { it.copy(error = null) }
                
                // Start scanner service
                MediaScannerService.startFullScan(context)
                
                // Monitor scan progress
                mediaScannerRepository.scanMusicLibrary().collect { progress ->
                    _scanProgress.value = progress
                    
                    if (progress.isComplete || progress.error != null) {
                        _isScanning.value = false
                        if (progress.error != null) {
                            _uiState.update { 
                                it.copy(error = "Scan failed: ${progress.error}") 
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _isScanning.value = false
                _uiState.update { 
                    it.copy(error = "Failed to start scan: ${e.message}") 
                }
            }
        }
    }

    fun startQuickScan() {
        viewModelScope.launch {
            try {
                _isScanning.value = true
                _uiState.update { it.copy(error = null) }
                
                // Start quick scanner service
                MediaScannerService.startQuickScan(context)
                
                // Monitor scan progress
                mediaScannerRepository.quickScan().collect { progress ->
                    _scanProgress.value = progress
                    
                    if (progress.isComplete || progress.error != null) {
                        _isScanning.value = false
                        if (progress.error != null) {
                            _uiState.update { 
                                it.copy(error = "Quick scan failed: ${progress.error}") 
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _isScanning.value = false
                _uiState.update { 
                    it.copy(error = "Failed to start quick scan: ${e.message}") 
                }
            }
        }
    }

    fun stopScan() {
        MediaScannerService.stopScan(context)
        _isScanning.value = false
        _scanProgress.value = null
    }

    fun checkLibraryEmpty(): Boolean {
        return try {
            // This would need to be implemented synchronously or cached
            // For now, return false as placeholder
            false
        } catch (e: Exception) {
            false
        }
    }
}

data class LibraryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTrack: Track? = null,
    val selectedArtist: Artist? = null,
    val selectedAlbum: Album? = null,
    val selectedPlaylist: Playlist? = null
)

data class LibraryStatsExtended(
    val totalTracks: Int = 0,
    val totalArtists: Int = 0,
    val totalAlbums: Int = 0,
    val totalPlaylists: Int = 0,
    val hiResCount: Int = 0,
    val totalDuration: Long = 0,
    val formats: List<String> = emptyList()
)

enum class LibraryTab(val displayName: String, val cyberpunkName: String) {
    TRACKS("Tracks", "AUDIO FILES"),
    ARTISTS("Artists", "AUDIO SOURCES"),
    ALBUMS("Albums", "AUDIO ARCHIVES"),
    PLAYLISTS("Playlists", "AUDIO SEQUENCES")
}