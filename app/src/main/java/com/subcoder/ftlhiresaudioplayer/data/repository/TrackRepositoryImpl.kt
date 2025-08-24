package com.subcoder.ftlhiresaudioplayer.data.repository

import com.subcoder.ftlhiresaudioplayer.data.database.dao.TrackDao
import com.subcoder.ftlhiresaudioplayer.data.database.entities.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Track Repository Implementation - Concrete implementation of track data operations
 */
@Singleton
class TrackRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao
) : TrackRepository {
    
    override suspend fun insertTrack(track: Track) {
        trackDao.insertTrack(track)
    }
    
    override suspend fun insertTracks(tracks: List<Track>) {
        trackDao.insertTracks(tracks)
    }
    
    override suspend fun updateTrack(track: Track) {
        trackDao.updateTrack(track)
    }
    
    override suspend fun deleteTrack(track: Track) {
        trackDao.deleteTrack(track)
    }
    
    override suspend fun deleteTrackById(id: String) {
        trackDao.deleteTrackById(id)
    }
    
    override fun getAllTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks()
    }
    
    override fun getTrackById(id: String): Flow<Track?> {
        return trackDao.getTrackById(id)
    }
    
    override fun getTrackByPath(path: String): Flow<Track?> {
        return trackDao.getTrackByPath(path)
    }
    
    override fun searchTracks(query: String): Flow<List<Track>> {
        return trackDao.searchTracks(query)
    }
    
    override fun getTracksByArtist(artistName: String): Flow<List<Track>> {
        return trackDao.searchTracks(artistName)
    }
    
    override fun getTracksByAlbum(albumName: String): Flow<List<Track>> {
        return trackDao.searchTracks(albumName)
    }
    
    override fun getTracksByGenre(genre: String): Flow<List<Track>> {
        return trackDao.searchTracks(genre)
    }
    
    override fun getTracksByFormat(format: String): Flow<List<Track>> {
        return trackDao.getAllTracks().map { tracks ->
            tracks.filter { it.format.equals(format, ignoreCase = true) }
        }
    }
    
    override fun getHiResTracks(): Flow<List<Track>> {
        return trackDao.getHiResTracks()
    }
    
    override fun getDSDTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks().map { tracks ->
            tracks.filter { it.isDSD }
        }
    }
    
    override fun getTracksBySampleRate(sampleRate: Int): Flow<List<Track>> {
        return trackDao.getAllTracks().map { tracks ->
            tracks.filter { it.sampleRate == sampleRate }
        }
    }
    
    override fun getTracksByBitDepth(bitDepth: Int): Flow<List<Track>> {
        return trackDao.getAllTracks().map { tracks ->
            tracks.filter { it.bitDepth == bitDepth }
        }
    }
    
    override fun getFavoriteTracks(): Flow<List<Track>> {
        return trackDao.getFavoriteTracks()
    }
    
    override suspend fun toggleFavorite(trackId: String) {
        trackDao.toggleFavorite(trackId)
    }
    
    override suspend fun updateRating(trackId: String, rating: Float) {
        // Implementation would update rating field
    }
    
    override suspend fun updateEqPreset(trackId: String, presetId: String) {
        trackDao.updateEqPreset(trackId, presetId)
    }
    
    override suspend fun incrementPlayCount(trackId: String) {
        trackDao.incrementPlayCount(trackId)
    }
    
    override suspend fun updateLastPlayed(trackId: String, timestamp: Long) {
        trackDao.updateLastPlayed(trackId, timestamp)
    }
    
    override fun getMostPlayedTracks(limit: Int): Flow<List<Track>> {
        return trackDao.getMostPlayedTracks(limit)
    }
    
    override fun getRecentlyPlayedTracks(limit: Int): Flow<List<Track>> {
        return trackDao.getRecentlyPlayedTracks(limit)
    }
    
    override fun getRecentlyAddedTracks(limit: Int): Flow<List<Track>> {
        return trackDao.getAllTracks().map { tracks ->
            tracks.sortedByDescending { it.dateAdded }.take(limit)
        }
    }
    
    override fun getTrackCount(): Flow<Int> {
        return trackDao.getTrackCount()
    }
    
    override fun getHiResTrackCount(): Flow<Int> {
        return trackDao.getHiResTrackCount()
    }
    
    override fun getTotalDuration(): Flow<Long> {
        return trackDao.getAllTracks().map { tracks ->
            tracks.sumOf { it.duration }
        }
    }
    
    override fun getFormatDistribution(): Flow<Map<String, Int>> {
        return trackDao.getAllTracks().map { tracks ->
            tracks.groupingBy { it.format }.eachCount()
        }
    }
}