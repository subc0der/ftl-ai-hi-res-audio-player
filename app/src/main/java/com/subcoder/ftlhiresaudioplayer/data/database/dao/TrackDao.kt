package com.subcoder.ftlhiresaudioplayer.data.database.dao

import androidx.room.*
import com.subcoder.ftlhiresaudioplayer.data.database.entities.Track
import kotlinx.coroutines.flow.Flow

/**
 * Track Data Access Object - Database operations for tracks
 */
@Dao
interface TrackDao {
    
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<Track>>
    
    @Query("SELECT * FROM tracks WHERE id = :id")
    fun getTrackById(id: String): Flow<Track?>
    
    @Query("SELECT * FROM tracks WHERE path = :path")
    fun getTrackByPath(path: String): Flow<Track?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<Track>)
    
    @Update
    suspend fun updateTrack(track: Track)
    
    @Delete
    suspend fun deleteTrack(track: Track)
    
    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrackById(id: String)
    
    @Query("SELECT * FROM tracks WHERE title LIKE '%' || :query || '%' OR artist_name LIKE '%' || :query || '%' OR album_name LIKE '%' || :query || '%'")
    fun searchTracks(query: String): Flow<List<Track>>
    
    @Query("SELECT * FROM tracks WHERE is_hi_res = 1 ORDER BY sample_rate DESC, bit_depth DESC")
    fun getHiResTracks(): Flow<List<Track>>
    
    @Query("SELECT * FROM tracks WHERE is_favorite = 1 ORDER BY title ASC")
    fun getFavoriteTracks(): Flow<List<Track>>
    
    @Query("UPDATE tracks SET is_favorite = NOT is_favorite WHERE id = :trackId")
    suspend fun toggleFavorite(trackId: String)
    
    @Query("UPDATE tracks SET play_count = play_count + 1 WHERE id = :trackId")
    suspend fun incrementPlayCount(trackId: String)
    
    @Query("UPDATE tracks SET last_played = :timestamp WHERE id = :trackId")
    suspend fun updateLastPlayed(trackId: String, timestamp: Long)
    
    @Query("UPDATE tracks SET eq_preset = :presetId WHERE id = :trackId")
    suspend fun updateEqPreset(trackId: String, presetId: String)
    
    @Query("SELECT * FROM tracks ORDER BY play_count DESC LIMIT :limit")
    fun getMostPlayedTracks(limit: Int): Flow<List<Track>>
    
    @Query("SELECT * FROM tracks WHERE last_played IS NOT NULL ORDER BY last_played DESC LIMIT :limit")
    fun getRecentlyPlayedTracks(limit: Int): Flow<List<Track>>
    
    @Query("SELECT COUNT(*) FROM tracks")
    fun getTrackCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM tracks WHERE is_hi_res = 1")
    fun getHiResTrackCount(): Flow<Int>
}