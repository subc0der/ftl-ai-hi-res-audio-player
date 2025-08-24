package com.ftl.hires.audioplayer.data.database.dao

import androidx.room.*
import com.ftl.hires.audioplayer.data.database.entities.Track
import com.ftl.hires.audioplayer.data.database.entities.TrackWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    suspend fun getAllTracksSync(): List<Track>
    
    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: String): Track?
    
    @Query("SELECT * FROM tracks WHERE file_path = :filePath")
    suspend fun getTrackByFilePath(filePath: String): Track?

    @Query("SELECT * FROM tracks WHERE file_path = :filePath")  
    suspend fun getTrackByPath(filePath: String): Track?
    
    @Query("""
        SELECT tracks.*, artists.name as artist_name, albums.title as album_name 
        FROM tracks 
        LEFT JOIN artists ON tracks.artist_id = artists.id 
        LEFT JOIN albums ON tracks.album_id = albums.id 
        WHERE tracks.id = :trackId
    """)
    suspend fun getTrackWithRelationsById(trackId: String): TrackWithRelations?
    
    @Query("SELECT * FROM tracks WHERE album_id = :albumId ORDER BY disc_number ASC, track_number ASC")
    fun getTracksByAlbum(albumId: String): Flow<List<Track>>
    
    @Query("SELECT * FROM tracks WHERE artist_id = :artistId ORDER BY album_name ASC, track_number ASC")
    fun getTracksByArtist(artistId: String): Flow<List<Track>>
    
    @Query("SELECT * FROM tracks WHERE genre = :genre ORDER BY artist_name ASC, album_name ASC, track_number ASC")
    fun getTracksByGenre(genre: String): Flow<List<Track>>
    
    @Query("SELECT * FROM tracks WHERE is_high_res = 1 ORDER BY bit_depth DESC, sample_rate DESC")
    fun getHighResTracks(): Flow<List<Track>>
    
    @Query("SELECT * FROM tracks WHERE is_favorite = 1 ORDER BY date_modified DESC")
    fun getFavoriteTracks(): Flow<List<Track>>
    
    @Query("SELECT * FROM tracks ORDER BY last_played DESC LIMIT :limit")
    fun getRecentlyPlayedTracks(limit: Int = 50): Flow<List<Track>>
    
    @Query("SELECT * FROM tracks ORDER BY date_added DESC LIMIT :limit")
    fun getRecentlyAddedTracks(limit: Int = 50): Flow<List<Track>>
    
    @Query("SELECT * FROM tracks ORDER BY play_count DESC LIMIT :limit")
    fun getMostPlayedTracks(limit: Int = 50): Flow<List<Track>>
    
    @Query("""
        SELECT * FROM tracks 
        WHERE title LIKE '%' || :query || '%' 
        OR artist_name LIKE '%' || :query || '%' 
        OR album_name LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN title LIKE :query || '%' THEN 1
                WHEN artist_name LIKE :query || '%' THEN 2
                WHEN album_name LIKE :query || '%' THEN 3
                ELSE 4
            END,
            title ASC
    """)
    fun searchTracks(query: String): Flow<List<Track>>
    
    @Query("SELECT DISTINCT format FROM tracks ORDER BY format ASC")
    suspend fun getAllFormats(): List<String>
    
    @Query("SELECT DISTINCT genre FROM tracks WHERE genre IS NOT NULL ORDER BY genre ASC")
    suspend fun getAllGenres(): List<String>
    
    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int
    
    @Query("SELECT SUM(duration_ms) FROM tracks")
    suspend fun getTotalDuration(): Long?
    
    @Query("SELECT SUM(file_size) FROM tracks")
    suspend fun getTotalSize(): Long?
    
    @Query("UPDATE tracks SET play_count = play_count + 1, last_played = :timestamp WHERE id = :trackId")
    suspend fun incrementPlayCount(trackId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE tracks SET is_favorite = :isFavorite WHERE id = :trackId")
    suspend fun updateFavoriteStatus(trackId: String, isFavorite: Boolean)
    
    @Query("UPDATE tracks SET eq_preset = :eqPreset WHERE id = :trackId")
    suspend fun updateEqPreset(trackId: String, eqPreset: String?)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<Track>)
    
    @Update
    suspend fun updateTrack(track: Track)
    
    @Update
    suspend fun updateTracks(tracks: List<Track>)
    
    @Delete
    suspend fun deleteTrack(track: Track)
    
    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteTrackById(trackId: String)
    
    @Query("DELETE FROM tracks WHERE file_path NOT IN (SELECT file_path FROM tracks WHERE file_path IS NOT NULL)")
    suspend fun deleteOrphanedTracks()
    
    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()
}