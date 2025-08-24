package com.ftl.hires.audioplayer.data.database.dao

import androidx.room.*
import com.ftl.hires.audioplayer.data.database.entities.Playlist
import com.ftl.hires.audioplayer.data.database.entities.PlaylistTrack
import com.ftl.hires.audioplayer.data.database.entities.PlaylistWithTracks
import com.ftl.hires.audioplayer.data.database.entities.PlaylistWithTrackDetails
import com.ftl.hires.audioplayer.data.database.entities.Track
import com.ftl.hires.audioplayer.data.database.entities.TrackWithPosition
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    
    @Query("SELECT * FROM playlists ORDER BY date_created DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: String): Playlist?
    
    @Query("SELECT * FROM playlists WHERE name = :name")
    suspend fun getPlaylistByName(name: String): Playlist?
    
    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistWithTracks(playlistId: String): PlaylistWithTracks?
    
    @Query("""
        SELECT tracks.*, playlist_tracks.position, playlist_tracks.date_added as date_added_to_playlist
        FROM tracks
        INNER JOIN playlist_tracks ON tracks.id = playlist_tracks.track_id
        WHERE playlist_tracks.playlist_id = :playlistId
        ORDER BY playlist_tracks.position ASC
    """)
    suspend fun getPlaylistTracksWithDetails(playlistId: String): List<TrackWithPosition>
    
    @Query("SELECT * FROM playlists WHERE is_favorite = 1 ORDER BY date_modified DESC")
    fun getFavoritePlaylists(): Flow<List<Playlist>>
    
    @Query("SELECT * FROM playlists WHERE is_smart_playlist = 1 ORDER BY name ASC")
    fun getSmartPlaylists(): Flow<List<Playlist>>
    
    @Query("SELECT * FROM playlists WHERE is_smart_playlist = 0 ORDER BY name ASC")
    fun getRegularPlaylists(): Flow<List<Playlist>>
    
    @Query("SELECT * FROM playlists ORDER BY last_played DESC LIMIT :limit")
    fun getRecentlyPlayedPlaylists(limit: Int = 20): Flow<List<Playlist>>
    
    @Query("""
        SELECT * FROM playlists 
        WHERE name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN name LIKE :query || '%' THEN 1
                ELSE 2
            END,
            name ASC
    """)
    fun searchPlaylists(query: String): Flow<List<Playlist>>
    
    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int
    
    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlist_id = :playlistId")
    suspend fun getTrackCountInPlaylist(playlistId: String): Int
    
    @Query("SELECT MAX(position) FROM playlist_tracks WHERE playlist_id = :playlistId")
    suspend fun getMaxPositionInPlaylist(playlistId: String): Int?
    
    @Query("UPDATE playlists SET play_count = play_count + 1, last_played = :timestamp WHERE id = :playlistId")
    suspend fun incrementPlayCount(playlistId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE playlists SET is_favorite = :isFavorite WHERE id = :playlistId")
    suspend fun updateFavoriteStatus(playlistId: String, isFavorite: Boolean)
    
    @Query("""
        UPDATE playlists 
        SET track_count = (SELECT COUNT(*) FROM playlist_tracks WHERE playlist_id = :playlistId),
            total_duration_ms = (
                SELECT COALESCE(SUM(tracks.duration_ms), 0) 
                FROM playlist_tracks 
                INNER JOIN tracks ON playlist_tracks.track_id = tracks.id 
                WHERE playlist_tracks.playlist_id = :playlistId
            ),
            date_modified = :timestamp
        WHERE id = :playlistId
    """)
    suspend fun updatePlaylistStats(playlistId: String, timestamp: Long = System.currentTimeMillis())
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrack(playlistTrack: PlaylistTrack)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTracks(playlistTracks: List<PlaylistTrack>)
    
    @Update
    suspend fun updatePlaylist(playlist: Playlist)
    
    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
    
    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: String)
    
    @Delete
    suspend fun deletePlaylistTrack(playlistTrack: PlaylistTrack)
    
    @Query("DELETE FROM playlist_tracks WHERE playlist_id = :playlistId AND track_id = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String)
    
    @Query("DELETE FROM playlist_tracks WHERE playlist_id = :playlistId")
    suspend fun clearPlaylist(playlistId: String)
    
    @Query("""
        UPDATE playlist_tracks 
        SET position = position - 1 
        WHERE playlist_id = :playlistId AND position > :removedPosition
    """)
    suspend fun reorderPlaylistAfterRemoval(playlistId: String, removedPosition: Int)
    
    @Transaction
    suspend fun addTrackToPlaylist(playlistId: String, trackId: String) {
        val maxPosition = getMaxPositionInPlaylist(playlistId) ?: -1
        val playlistTrack = PlaylistTrack(
            playlistId = playlistId,
            trackId = trackId,
            position = maxPosition + 1
        )
        insertPlaylistTrack(playlistTrack)
        updatePlaylistStats(playlistId)
    }
    
    @Transaction
    suspend fun addTracksToPlaylist(playlistId: String, trackIds: List<String>) {
        val maxPosition = getMaxPositionInPlaylist(playlistId) ?: -1
        val playlistTracks = trackIds.mapIndexed { index, trackId ->
            PlaylistTrack(
                playlistId = playlistId,
                trackId = trackId,
                position = maxPosition + 1 + index
            )
        }
        insertPlaylistTracks(playlistTracks)
        updatePlaylistStats(playlistId)
    }
    
    @Transaction
    suspend fun removeTrackFromPlaylistAtPosition(playlistId: String, trackId: String, position: Int) {
        removeTrackFromPlaylist(playlistId, trackId)
        reorderPlaylistAfterRemoval(playlistId, position)
        updatePlaylistStats(playlistId)
    }
    
    @Transaction
    suspend fun moveTrackInPlaylist(playlistId: String, fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        
        if (fromPosition < toPosition) {
            // Moving down - shift tracks up
            updateTracksPositionRange(playlistId, fromPosition + 1, toPosition, -1)
        } else {
            // Moving up - shift tracks down
            updateTracksPositionRange(playlistId, toPosition, fromPosition - 1, 1)
        }
        
        // Update the moved track's position
        updateTrackPosition(playlistId, fromPosition, toPosition)
        updatePlaylistStats(playlistId)
    }
    
    @Query("""
        UPDATE playlist_tracks 
        SET position = position + :offset 
        WHERE playlist_id = :playlistId AND position >= :startPosition AND position <= :endPosition
    """)
    suspend fun updateTracksPositionRange(playlistId: String, startPosition: Int, endPosition: Int, offset: Int)
    
    @Query("""
        UPDATE playlist_tracks 
        SET position = :newPosition 
        WHERE playlist_id = :playlistId AND position = :oldPosition
    """)
    suspend fun updateTrackPosition(playlistId: String, oldPosition: Int, newPosition: Int)
}