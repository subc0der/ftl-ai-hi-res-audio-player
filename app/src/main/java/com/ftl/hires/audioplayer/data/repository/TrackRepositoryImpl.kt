package com.ftl.hires.audioplayer.data.repository

import com.ftl.hires.audioplayer.data.database.dao.TrackDao
import com.ftl.hires.audioplayer.data.database.entities.Track
import com.ftl.hires.audioplayer.data.database.entities.TrackWithRelations
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao
) : TrackRepository {
    
    override fun getAllTracks(): Flow<List<Track>> = trackDao.getAllTracks()
    
    override suspend fun getTrackById(trackId: String): Track? = trackDao.getTrackById(trackId)
    
    override suspend fun getTrackByFilePath(filePath: String): Track? = trackDao.getTrackByFilePath(filePath)
    
    override suspend fun getTrackWithRelationsById(trackId: String): TrackWithRelations? =
        trackDao.getTrackWithRelationsById(trackId)
    
    override fun getTracksByAlbum(albumId: String): Flow<List<Track>> = trackDao.getTracksByAlbum(albumId)
    
    override fun getTracksByArtist(artistId: String): Flow<List<Track>> = trackDao.getTracksByArtist(artistId)
    
    override fun getTracksByGenre(genre: String): Flow<List<Track>> = trackDao.getTracksByGenre(genre)
    
    override fun getHighResTracks(): Flow<List<Track>> = trackDao.getHighResTracks()
    
    override fun getFavoriteTracks(): Flow<List<Track>> = trackDao.getFavoriteTracks()
    
    override fun getRecentlyPlayedTracks(limit: Int): Flow<List<Track>> = 
        trackDao.getRecentlyPlayedTracks(limit)
    
    override fun getRecentlyAddedTracks(limit: Int): Flow<List<Track>> = 
        trackDao.getRecentlyAddedTracks(limit)
    
    override fun getMostPlayedTracks(limit: Int): Flow<List<Track>> = 
        trackDao.getMostPlayedTracks(limit)
    
    override fun searchTracks(query: String): Flow<List<Track>> = trackDao.searchTracks(query)
    
    override suspend fun getAllFormats(): List<String> = trackDao.getAllFormats()
    
    override suspend fun getAllGenres(): List<String> = trackDao.getAllGenres()
    
    override suspend fun getTrackCount(): Int = trackDao.getTrackCount()
    
    override suspend fun getTotalDuration(): Long? = trackDao.getTotalDuration()
    
    override suspend fun getTotalSize(): Long? = trackDao.getTotalSize()
    
    override suspend fun incrementPlayCount(trackId: String, timestamp: Long) = 
        trackDao.incrementPlayCount(trackId, timestamp)
    
    override suspend fun updateFavoriteStatus(trackId: String, isFavorite: Boolean) = 
        trackDao.updateFavoriteStatus(trackId, isFavorite)
    
    override suspend fun updateEqPreset(trackId: String, eqPreset: String?) = 
        trackDao.updateEqPreset(trackId, eqPreset)
    
    override suspend fun insertTrack(track: Track) = trackDao.insertTrack(track)
    
    override suspend fun insertTracks(tracks: List<Track>) = trackDao.insertTracks(tracks)
    
    override suspend fun updateTrack(track: Track) = trackDao.updateTrack(track)
    
    override suspend fun updateTracks(tracks: List<Track>) = trackDao.updateTracks(tracks)
    
    override suspend fun deleteTrack(track: Track) = trackDao.deleteTrack(track)
    
    override suspend fun deleteTrackById(trackId: String) = trackDao.deleteTrackById(trackId)
    
    override suspend fun deleteOrphanedTracks() = trackDao.deleteOrphanedTracks()
    
    override suspend fun deleteAllTracks() = trackDao.deleteAllTracks()
}