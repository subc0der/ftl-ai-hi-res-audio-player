package com.ftl.hires.audioplayer.data.repository

import com.ftl.hires.audioplayer.data.database.entities.Album
import com.ftl.hires.audioplayer.data.database.entities.Artist
import com.ftl.hires.audioplayer.data.database.dao.LibraryStats
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    
    // Artist operations
    fun getAllArtists(): Flow<List<Artist>>
    suspend fun getArtistById(artistId: String): Artist?
    suspend fun getArtistByName(name: String): Artist?
    fun getFavoriteArtists(): Flow<List<Artist>>
    fun getRecentlyPlayedArtists(limit: Int = 20): Flow<List<Artist>>
    fun getRecentlyAddedArtists(limit: Int = 20): Flow<List<Artist>>
    fun getMostPlayedArtists(limit: Int = 20): Flow<List<Artist>>
    fun searchArtists(query: String): Flow<List<Artist>>
    suspend fun getArtistCount(): Int
    
    // Album operations  
    fun getAllAlbums(): Flow<List<Album>>
    suspend fun getAlbumById(albumId: String): Album?
    suspend fun getAlbumByTitleAndArtist(title: String, artistId: String?): Album?
    fun getAlbumsByArtist(artistId: String): Flow<List<Album>>
    fun getAlbumsByYear(year: Int): Flow<List<Album>>
    fun getAlbumsByGenre(genre: String): Flow<List<Album>>
    fun getHighResAlbums(): Flow<List<Album>>
    fun getFavoriteAlbums(): Flow<List<Album>>
    fun getRecentlyPlayedAlbums(limit: Int = 20): Flow<List<Album>>
    fun getRecentlyAddedAlbums(limit: Int = 20): Flow<List<Album>>
    fun getMostPlayedAlbums(limit: Int = 20): Flow<List<Album>>
    fun searchAlbums(query: String): Flow<List<Album>>
    suspend fun getAllYears(): List<Int>
    suspend fun getAlbumCount(): Int
    
    // Stats and analytics
    suspend fun incrementArtistPlayCount(artistId: String, timestamp: Long = System.currentTimeMillis())
    suspend fun updateArtistFavoriteStatus(artistId: String, isFavorite: Boolean)
    suspend fun updateArtistStats(artistId: String, timestamp: Long = System.currentTimeMillis())
    suspend fun incrementAlbumPlayCount(albumId: String, timestamp: Long = System.currentTimeMillis())
    suspend fun updateAlbumFavoriteStatus(albumId: String, isFavorite: Boolean)
    suspend fun updateAlbumStats(albumId: String, timestamp: Long = System.currentTimeMillis())
    
    // CRUD operations
    suspend fun insertArtist(artist: Artist)
    suspend fun insertArtists(artists: List<Artist>)
    suspend fun insertAlbum(album: Album)
    suspend fun insertAlbums(albums: List<Album>)
    suspend fun updateArtist(artist: Artist)
    suspend fun updateArtists(artists: List<Artist>)
    suspend fun updateAlbum(album: Album)
    suspend fun updateAlbums(albums: List<Album>)
    suspend fun deleteArtist(artist: Artist)
    suspend fun deleteArtistById(artistId: String)
    suspend fun deleteAlbum(album: Album)
    suspend fun deleteAlbumById(albumId: String)
    
    // Maintenance
    suspend fun cleanupOrphanedEntries()
    suspend fun getLibraryStats(): LibraryStats
}