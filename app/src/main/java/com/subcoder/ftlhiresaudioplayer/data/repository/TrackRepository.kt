package com.subcoder.ftlhiresaudioplayer.data.repository

import com.subcoder.ftlhiresaudioplayer.data.database.entities.Track
import kotlinx.coroutines.flow.Flow

/**
 * Track Repository Interface - Contract for track data operations
 * 
 * Features:
 * - CRUD operations with reactive Flow responses
 * - Hi-res audio filtering and search
 * - Statistics tracking and analytics
 * - User preferences management
 */
interface TrackRepository {
    
    // Basic CRUD operations
    suspend fun insertTrack(track: Track)
    suspend fun insertTracks(tracks: List<Track>)
    suspend fun updateTrack(track: Track)
    suspend fun deleteTrack(track: Track)
    suspend fun deleteTrackById(id: String)
    
    // Retrieval operations
    fun getAllTracks(): Flow<List<Track>>
    fun getTrackById(id: String): Flow<Track?>
    fun getTrackByPath(path: String): Flow<Track?>
    
    // Search and filtering
    fun searchTracks(query: String): Flow<List<Track>>
    fun getTracksByArtist(artistName: String): Flow<List<Track>>
    fun getTracksByAlbum(albumName: String): Flow<List<Track>>
    fun getTracksByGenre(genre: String): Flow<List<Track>>
    fun getTracksByFormat(format: String): Flow<List<Track>>
    
    // Hi-res audio filtering
    fun getHiResTracks(): Flow<List<Track>>
    fun getDSDTracks(): Flow<List<Track>>
    fun getTracksBySampleRate(sampleRate: Int): Flow<List<Track>>
    fun getTracksByBitDepth(bitDepth: Int): Flow<List<Track>>
    
    // User preferences
    fun getFavoriteTracks(): Flow<List<Track>>
    suspend fun toggleFavorite(trackId: String)
    suspend fun updateRating(trackId: String, rating: Float)
    suspend fun updateEqPreset(trackId: String, presetId: String)
    
    // Statistics
    suspend fun incrementPlayCount(trackId: String)
    suspend fun updateLastPlayed(trackId: String, timestamp: Long)
    fun getMostPlayedTracks(limit: Int = 50): Flow<List<Track>>
    fun getRecentlyPlayedTracks(limit: Int = 50): Flow<List<Track>>
    fun getRecentlyAddedTracks(limit: Int = 50): Flow<List<Track>>
    
    // Analytics
    fun getTrackCount(): Flow<Int>
    fun getHiResTrackCount(): Flow<Int>
    fun getTotalDuration(): Flow<Long>
    fun getFormatDistribution(): Flow<Map<String, Int>>
}