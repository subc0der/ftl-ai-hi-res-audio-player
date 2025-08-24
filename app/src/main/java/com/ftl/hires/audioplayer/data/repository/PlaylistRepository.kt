package com.ftl.hires.audioplayer.data.repository

import com.ftl.hires.audioplayer.data.database.entities.Playlist
import com.ftl.hires.audioplayer.data.database.entities.PlaylistTrack
import com.ftl.hires.audioplayer.data.database.entities.PlaylistWithTracks
import com.ftl.hires.audioplayer.data.database.entities.TrackWithPosition
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    
    // Basic operations
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(playlistId: String): Playlist?
    suspend fun getPlaylistByName(name: String): Playlist?
    suspend fun getPlaylistWithTracks(playlistId: String): PlaylistWithTracks?
    suspend fun getPlaylistTracksWithDetails(playlistId: String): List<TrackWithPosition>
    
    // Filtered playlists
    fun getFavoritePlaylists(): Flow<List<Playlist>>
    fun getSmartPlaylists(): Flow<List<Playlist>>
    fun getRegularPlaylists(): Flow<List<Playlist>>
    fun getRecentlyPlayedPlaylists(limit: Int = 20): Flow<List<Playlist>>
    
    // Search
    fun searchPlaylists(query: String): Flow<List<Playlist>>
    
    // Metadata
    suspend fun getPlaylistCount(): Int
    suspend fun getTrackCountInPlaylist(playlistId: String): Int
    
    // Playlist management
    suspend fun createPlaylist(name: String, description: String? = null): String // Returns playlist ID
    suspend fun addTrackToPlaylist(playlistId: String, trackId: String)
    suspend fun addTracksToPlaylist(playlistId: String, trackIds: List<String>)
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String, position: Int)
    suspend fun moveTrackInPlaylist(playlistId: String, fromPosition: Int, toPosition: Int)
    suspend fun clearPlaylist(playlistId: String)
    
    // Updates
    suspend fun incrementPlayCount(playlistId: String, timestamp: Long = System.currentTimeMillis())
    suspend fun updateFavoriteStatus(playlistId: String, isFavorite: Boolean)
    suspend fun updatePlaylistStats(playlistId: String, timestamp: Long = System.currentTimeMillis())
    
    // CRUD
    suspend fun insertPlaylist(playlist: Playlist)
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlist: Playlist)
    suspend fun deletePlaylistById(playlistId: String)
}