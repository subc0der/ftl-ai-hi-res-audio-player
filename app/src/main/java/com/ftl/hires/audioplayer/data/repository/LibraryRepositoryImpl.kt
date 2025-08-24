package com.ftl.hires.audioplayer.data.repository

import com.ftl.hires.audioplayer.data.database.dao.LibraryDao
import com.ftl.hires.audioplayer.data.database.dao.TrackDao
import com.ftl.hires.audioplayer.data.database.entities.Album
import com.ftl.hires.audioplayer.data.database.entities.Artist
import com.ftl.hires.audioplayer.data.database.dao.LibraryStats
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LibraryRepositoryImpl @Inject constructor(
    private val libraryDao: LibraryDao,
    private val trackDao: TrackDao
) : LibraryRepository {
    
    // Artist operations
    override fun getAllArtists(): Flow<List<Artist>> = libraryDao.getAllArtists()
    
    override suspend fun getArtistById(artistId: String): Artist? = 
        libraryDao.getArtistById(artistId)
    
    override suspend fun getArtistByName(name: String): Artist? = 
        libraryDao.getArtistByName(name)
    
    override fun getFavoriteArtists(): Flow<List<Artist>> = libraryDao.getFavoriteArtists()
    
    override fun getRecentlyPlayedArtists(limit: Int): Flow<List<Artist>> = 
        libraryDao.getRecentlyPlayedArtists(limit)
    
    override fun getRecentlyAddedArtists(limit: Int): Flow<List<Artist>> = 
        libraryDao.getRecentlyAddedArtists(limit)
    
    override fun getMostPlayedArtists(limit: Int): Flow<List<Artist>> = 
        libraryDao.getMostPlayedArtists(limit)
    
    override fun searchArtists(query: String): Flow<List<Artist>> = 
        libraryDao.searchArtists(query)
    
    override suspend fun getArtistCount(): Int = libraryDao.getArtistCount()
    
    // Album operations
    override fun getAllAlbums(): Flow<List<Album>> = libraryDao.getAllAlbums()
    
    override suspend fun getAlbumById(albumId: String): Album? = 
        libraryDao.getAlbumById(albumId)
    
    override suspend fun getAlbumByTitleAndArtist(title: String, artistId: String?): Album? = 
        libraryDao.getAlbumByTitleAndArtist(title, artistId)
    
    override fun getAlbumsByArtist(artistId: String): Flow<List<Album>> = 
        libraryDao.getAlbumsByArtist(artistId)
    
    override fun getAlbumsByYear(year: Int): Flow<List<Album>> = 
        libraryDao.getAlbumsByYear(year)
    
    override fun getAlbumsByGenre(genre: String): Flow<List<Album>> = 
        libraryDao.getAlbumsByGenre(genre)
    
    override fun getHighResAlbums(): Flow<List<Album>> = libraryDao.getHighResAlbums()
    
    override fun getFavoriteAlbums(): Flow<List<Album>> = libraryDao.getFavoriteAlbums()
    
    override fun getRecentlyPlayedAlbums(limit: Int): Flow<List<Album>> = 
        libraryDao.getRecentlyPlayedAlbums(limit)
    
    override fun getRecentlyAddedAlbums(limit: Int): Flow<List<Album>> = 
        libraryDao.getRecentlyAddedAlbums(limit)
    
    override fun getMostPlayedAlbums(limit: Int): Flow<List<Album>> = 
        libraryDao.getMostPlayedAlbums(limit)
    
    override fun searchAlbums(query: String): Flow<List<Album>> = 
        libraryDao.searchAlbums(query)
    
    override suspend fun getAllYears(): List<Int> = libraryDao.getAllYears()
    
    override suspend fun getAlbumCount(): Int = libraryDao.getAlbumCount()
    
    // Stats and analytics
    override suspend fun incrementArtistPlayCount(artistId: String, timestamp: Long) {
        libraryDao.incrementArtistPlayCount(artistId, timestamp)
    }
    
    override suspend fun updateArtistFavoriteStatus(artistId: String, isFavorite: Boolean) {
        libraryDao.updateArtistFavoriteStatus(artistId, isFavorite)
    }
    
    override suspend fun updateArtistStats(artistId: String, timestamp: Long) {
        libraryDao.updateArtistStats(artistId, timestamp)
    }
    
    override suspend fun incrementAlbumPlayCount(albumId: String, timestamp: Long) {
        libraryDao.incrementAlbumPlayCount(albumId, timestamp)
    }
    
    override suspend fun updateAlbumFavoriteStatus(albumId: String, isFavorite: Boolean) {
        libraryDao.updateAlbumFavoriteStatus(albumId, isFavorite)
    }
    
    override suspend fun updateAlbumStats(albumId: String, timestamp: Long) {
        libraryDao.updateAlbumStats(albumId, timestamp)
    }
    
    // CRUD operations
    override suspend fun insertArtist(artist: Artist) = libraryDao.insertArtist(artist)
    
    override suspend fun insertArtists(artists: List<Artist>) = libraryDao.insertArtists(artists)
    
    override suspend fun insertAlbum(album: Album) = libraryDao.insertAlbum(album)
    
    override suspend fun insertAlbums(albums: List<Album>) = libraryDao.insertAlbums(albums)
    
    override suspend fun updateArtist(artist: Artist) = libraryDao.updateArtist(artist)
    
    override suspend fun updateArtists(artists: List<Artist>) = libraryDao.updateArtists(artists)
    
    override suspend fun updateAlbum(album: Album) = libraryDao.updateAlbum(album)
    
    override suspend fun updateAlbums(albums: List<Album>) = libraryDao.updateAlbums(albums)
    
    override suspend fun deleteArtist(artist: Artist) = libraryDao.deleteArtist(artist)
    
    override suspend fun deleteArtistById(artistId: String) = libraryDao.deleteArtistById(artistId)
    
    override suspend fun deleteAlbum(album: Album) = libraryDao.deleteAlbum(album)
    
    override suspend fun deleteAlbumById(albumId: String) = libraryDao.deleteAlbumById(albumId)
    
    // Maintenance
    override suspend fun cleanupOrphanedEntries() = libraryDao.cleanupOrphanedEntries()
    
    override suspend fun getLibraryStats(): LibraryStats = libraryDao.getLibraryStats()
}