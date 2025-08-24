package com.ftl.hires.audioplayer.data.repository

import com.ftl.hires.audioplayer.data.database.entities.Track
import com.ftl.hires.audioplayer.data.database.entities.TrackWithRelations
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    
    // Basic operations
    fun getAllTracks(): Flow<List<Track>>
    suspend fun getTrackById(trackId: String): Track?
    suspend fun getTrackByFilePath(filePath: String): Track?
    suspend fun getTrackWithRelationsById(trackId: String): TrackWithRelations?
    
    // Filtered tracks
    fun getTracksByAlbum(albumId: String): Flow<List<Track>>
    fun getTracksByArtist(artistId: String): Flow<List<Track>>
    fun getTracksByGenre(genre: String): Flow<List<Track>>
    fun getHighResTracks(): Flow<List<Track>>
    fun getFavoriteTracks(): Flow<List<Track>>
    
    // Recently played/added/popular
    fun getRecentlyPlayedTracks(limit: Int = 50): Flow<List<Track>>
    fun getRecentlyAddedTracks(limit: Int = 50): Flow<List<Track>>
    fun getMostPlayedTracks(limit: Int = 50): Flow<List<Track>>
    
    // Search
    fun searchTracks(query: String): Flow<List<Track>>
    
    // Metadata
    suspend fun getAllFormats(): List<String>
    suspend fun getAllGenres(): List<String>
    suspend fun getTrackCount(): Int
    suspend fun getTotalDuration(): Long?
    suspend fun getTotalSize(): Long?
    
    // Updates
    suspend fun incrementPlayCount(trackId: String, timestamp: Long = System.currentTimeMillis())
    suspend fun updateFavoriteStatus(trackId: String, isFavorite: Boolean)
    suspend fun updateEqPreset(trackId: String, eqPreset: String?)
    
    // CRUD
    suspend fun insertTrack(track: Track)
    suspend fun insertTracks(tracks: List<Track>)
    suspend fun updateTrack(track: Track)
    suspend fun updateTracks(tracks: List<Track>)
    suspend fun deleteTrack(track: Track)
    suspend fun deleteTrackById(trackId: String)
    suspend fun deleteOrphanedTracks()
    suspend fun deleteAllTracks()
}