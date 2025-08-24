package com.ftl.hires.audioplayer.data.database.dao

import androidx.room.*
import com.ftl.hires.audioplayer.data.database.entities.Album
import com.ftl.hires.audioplayer.data.database.entities.Artist
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    
    // Artist operations
    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtists(): Flow<List<Artist>>
    
    @Query("SELECT * FROM artists WHERE id = :artistId")
    suspend fun getArtistById(artistId: String): Artist?
    
    @Query("SELECT * FROM artists WHERE name = :name")
    suspend fun getArtistByName(name: String): Artist?
    
    @Query("SELECT * FROM artists WHERE is_favorite = 1 ORDER BY name ASC")
    fun getFavoriteArtists(): Flow<List<Artist>>
    
    @Query("SELECT * FROM artists ORDER BY last_played DESC LIMIT :limit")
    fun getRecentlyPlayedArtists(limit: Int = 20): Flow<List<Artist>>
    
    @Query("SELECT * FROM artists ORDER BY date_added DESC LIMIT :limit")
    fun getRecentlyAddedArtists(limit: Int = 20): Flow<List<Artist>>
    
    @Query("SELECT * FROM artists ORDER BY play_count DESC LIMIT :limit")
    fun getMostPlayedArtists(limit: Int = 20): Flow<List<Artist>>
    
    @Query("""
        SELECT * FROM artists 
        WHERE name LIKE '%' || :query || '%' 
        OR sort_name LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN name LIKE :query || '%' THEN 1
                WHEN sort_name LIKE :query || '%' THEN 2
                ELSE 3
            END,
            name ASC
    """)
    fun searchArtists(query: String): Flow<List<Artist>>
    
    @Query("SELECT COUNT(*) FROM artists")
    suspend fun getArtistCount(): Int
    
    @Query("UPDATE artists SET play_count = play_count + 1, last_played = :timestamp WHERE id = :artistId")
    suspend fun incrementArtistPlayCount(artistId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE artists SET is_favorite = :isFavorite WHERE id = :artistId")
    suspend fun updateArtistFavoriteStatus(artistId: String, isFavorite: Boolean)
    
    @Query("""
        UPDATE artists 
        SET track_count = (SELECT COUNT(*) FROM tracks WHERE artist_id = :artistId),
            album_count = (SELECT COUNT(*) FROM albums WHERE artist_id = :artistId),
            total_duration_ms = (SELECT COALESCE(SUM(duration_ms), 0) FROM tracks WHERE artist_id = :artistId),
            date_modified = :timestamp
        WHERE id = :artistId
    """)
    suspend fun updateArtistStats(artistId: String, timestamp: Long = System.currentTimeMillis())
    
    // Album operations
    @Query("SELECT * FROM albums ORDER BY title ASC")
    fun getAllAlbums(): Flow<List<Album>>
    
    @Query("SELECT * FROM albums WHERE id = :albumId")
    suspend fun getAlbumById(albumId: String): Album?
    
    @Query("SELECT * FROM albums WHERE title = :title AND artist = :artist")
    suspend fun getAlbumByTitleAndArtist(title: String, artist: String): Album?
    
    @Query("SELECT * FROM albums WHERE artist_id = :artistId ORDER BY year ASC, title ASC")
    fun getAlbumsByArtist(artistId: String): Flow<List<Album>>
    
    @Query("SELECT * FROM albums WHERE year = :year ORDER BY artist_name ASC, title ASC")
    fun getAlbumsByYear(year: Int): Flow<List<Album>>
    
    @Query("SELECT * FROM albums WHERE genre = :genre ORDER BY artist_name ASC, title ASC")
    fun getAlbumsByGenre(genre: String): Flow<List<Album>>
    
    @Query("SELECT * FROM albums WHERE is_high_res = 1 ORDER BY average_sample_rate DESC, title ASC")
    fun getHighResAlbums(): Flow<List<Album>>
    
    @Query("SELECT * FROM albums WHERE is_favorite = 1 ORDER BY date_modified DESC")
    fun getFavoriteAlbums(): Flow<List<Album>>
    
    @Query("SELECT * FROM albums ORDER BY last_played DESC LIMIT :limit")
    fun getRecentlyPlayedAlbums(limit: Int = 20): Flow<List<Album>>
    
    @Query("SELECT * FROM albums ORDER BY date_added DESC LIMIT :limit")
    fun getRecentlyAddedAlbums(limit: Int = 20): Flow<List<Album>>
    
    @Query("SELECT * FROM albums ORDER BY play_count DESC LIMIT :limit")
    fun getMostPlayedAlbums(limit: Int = 20): Flow<List<Album>>
    
    @Query("""
        SELECT * FROM albums 
        WHERE title LIKE '%' || :query || '%' 
        OR artist_name LIKE '%' || :query || '%'
        OR sort_title LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN title LIKE :query || '%' THEN 1
                WHEN artist_name LIKE :query || '%' THEN 2
                WHEN sort_title LIKE :query || '%' THEN 3
                ELSE 4
            END,
            title ASC
    """)
    fun searchAlbums(query: String): Flow<List<Album>>
    
    @Query("SELECT DISTINCT year FROM albums WHERE year IS NOT NULL ORDER BY year DESC")
    suspend fun getAllYears(): List<Int>
    
    @Query("SELECT COUNT(*) FROM albums")
    suspend fun getAlbumCount(): Int
    
    @Query("UPDATE albums SET play_count = play_count + 1, last_played = :timestamp WHERE id = :albumId")
    suspend fun incrementAlbumPlayCount(albumId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE albums SET is_favorite = :isFavorite WHERE id = :albumId")
    suspend fun updateAlbumFavoriteStatus(albumId: String, isFavorite: Boolean)
    
    @Query("""
        UPDATE albums 
        SET total_tracks = (SELECT COUNT(*) FROM tracks WHERE album_id = :albumId),
            total_duration_ms = (SELECT COALESCE(SUM(duration_ms), 0) FROM tracks WHERE album_id = :albumId),
            average_bitrate = (SELECT AVG(bitrate) FROM tracks WHERE album_id = :albumId AND bitrate IS NOT NULL),
            average_sample_rate = (SELECT AVG(sample_rate) FROM tracks WHERE album_id = :albumId AND sample_rate IS NOT NULL),
            is_high_res = (SELECT MAX(is_high_res) FROM tracks WHERE album_id = :albumId),
            date_modified = :timestamp
        WHERE id = :albumId
    """)
    suspend fun updateAlbumStats(albumId: String, timestamp: Long = System.currentTimeMillis())
    
    // Combined operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: Artist)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<Artist>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: Album)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<Album>)
    
    @Update
    suspend fun updateArtist(artist: Artist)
    
    @Update
    suspend fun updateArtists(artists: List<Artist>)
    
    @Update
    suspend fun updateAlbum(album: Album)
    
    @Update
    suspend fun updateAlbums(albums: List<Album>)
    
    @Delete
    suspend fun deleteArtist(artist: Artist)
    
    @Query("DELETE FROM artists WHERE id = :artistId")
    suspend fun deleteArtistById(artistId: String)
    
    @Delete
    suspend fun deleteAlbum(album: Album)
    
    @Query("DELETE FROM albums WHERE id = :albumId")
    suspend fun deleteAlbumById(albumId: String)
    
    // Cleanup operations - get orphaned entries
    @Query("""
        SELECT * FROM artists 
        WHERE name NOT IN (SELECT DISTINCT artist FROM tracks WHERE artist IS NOT NULL)
    """)
    suspend fun getArtistsWithNoTracks(): List<Artist>
    
    @Query("""
        SELECT * FROM albums 
        WHERE title || '|' || artist NOT IN (
            SELECT DISTINCT album || '|' || artist FROM tracks 
            WHERE album IS NOT NULL AND artist IS NOT NULL
        )
    """)
    suspend fun getAlbumsWithNoTracks(): List<Album>
    
    @Query("""
        DELETE FROM artists 
        WHERE name NOT IN (SELECT DISTINCT artist FROM tracks WHERE artist IS NOT NULL)
    """)
    suspend fun deleteOrphanedArtists()
    
    @Query("""
        DELETE FROM albums 
        WHERE title || '|' || artist NOT IN (
            SELECT DISTINCT album || '|' || artist FROM tracks 
            WHERE album IS NOT NULL AND artist IS NOT NULL
        )
    """)
    suspend fun deleteOrphanedAlbums()
    
    @Transaction
    suspend fun cleanupOrphanedEntries() {
        deleteOrphanedArtists()
        deleteOrphanedAlbums()
    }
    
    // Library statistics
    @Query("""
        SELECT 
            (SELECT COUNT(*) FROM tracks) as track_count,
            (SELECT COUNT(*) FROM albums) as album_count,
            (SELECT COUNT(*) FROM artists) as artist_count,
            (SELECT COALESCE(SUM(duration_ms), 0) FROM tracks) as total_duration,
            (SELECT COALESCE(SUM(file_size), 0) FROM tracks) as total_size
    """)
    suspend fun getLibraryStats(): LibraryStats
}

data class LibraryStats(
    val trackCount: Int,
    val albumCount: Int,
    val artistCount: Int,
    val totalDuration: Long,
    val totalSize: Long
)