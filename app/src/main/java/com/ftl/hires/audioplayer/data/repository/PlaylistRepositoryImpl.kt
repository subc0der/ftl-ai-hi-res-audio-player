package com.ftl.hires.audioplayer.data.repository

import com.ftl.hires.audioplayer.data.database.dao.PlaylistDao
import com.ftl.hires.audioplayer.data.database.dao.TrackDao
import com.ftl.hires.audioplayer.data.database.entities.Playlist
import com.ftl.hires.audioplayer.data.database.entities.PlaylistWithTracks
import com.ftl.hires.audioplayer.data.database.entities.TrackWithPosition
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val trackDao: TrackDao
) : PlaylistRepository {
    
    override fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    
    override suspend fun getPlaylistById(playlistId: String): Playlist? = 
        playlistDao.getPlaylistById(playlistId)
    
    override suspend fun getPlaylistByName(name: String): Playlist? = 
        playlistDao.getPlaylistByName(name)
    
    override suspend fun getPlaylistWithTracks(playlistId: String): PlaylistWithTracks? = 
        playlistDao.getPlaylistWithTracks(playlistId)
    
    override suspend fun getPlaylistTracksWithDetails(playlistId: String): List<TrackWithPosition> = 
        playlistDao.getPlaylistTracksWithDetails(playlistId)
    
    override fun getFavoritePlaylists(): Flow<List<Playlist>> = playlistDao.getFavoritePlaylists()
    
    override fun getSmartPlaylists(): Flow<List<Playlist>> = playlistDao.getSmartPlaylists()
    
    override fun getRegularPlaylists(): Flow<List<Playlist>> = playlistDao.getRegularPlaylists()
    
    override fun getRecentlyPlayedPlaylists(limit: Int): Flow<List<Playlist>> = 
        playlistDao.getRecentlyPlayedPlaylists(limit)
    
    override fun searchPlaylists(query: String): Flow<List<Playlist>> = 
        playlistDao.searchPlaylists(query)
    
    override suspend fun getPlaylistCount(): Int = playlistDao.getPlaylistCount()
    
    override suspend fun getTrackCountInPlaylist(playlistId: String): Int = 
        playlistDao.getTrackCountInPlaylist(playlistId)
    
    override suspend fun createPlaylist(name: String, description: String?): String {
        val playlistId = UUID.randomUUID().toString()
        val playlist = Playlist(
            id = playlistId,
            name = name,
            description = description
        )
        playlistDao.insertPlaylist(playlist)
        return playlistId
    }
    
    override suspend fun addTrackToPlaylist(playlistId: String, trackId: String) {
        playlistDao.addTrackToPlaylist(playlistId, trackId)
    }
    
    override suspend fun addTracksToPlaylist(playlistId: String, trackIds: List<String>) {
        playlistDao.addTracksToPlaylist(playlistId, trackIds)
    }
    
    override suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String, position: Int) {
        playlistDao.removeTrackFromPlaylistAtPosition(playlistId, trackId, position)
    }
    
    override suspend fun moveTrackInPlaylist(playlistId: String, fromPosition: Int, toPosition: Int) {
        playlistDao.moveTrackInPlaylist(playlistId, fromPosition, toPosition)
    }
    
    override suspend fun clearPlaylist(playlistId: String) {
        playlistDao.clearPlaylist(playlistId)
        playlistDao.updatePlaylistStats(playlistId)
    }
    
    override suspend fun incrementPlayCount(playlistId: String, timestamp: Long) {
        playlistDao.incrementPlayCount(playlistId, timestamp)
    }
    
    override suspend fun updateFavoriteStatus(playlistId: String, isFavorite: Boolean) {
        playlistDao.updateFavoriteStatus(playlistId, isFavorite)
    }
    
    override suspend fun updatePlaylistStats(playlistId: String, timestamp: Long) {
        playlistDao.updatePlaylistStats(playlistId, timestamp)
    }
    
    override suspend fun insertPlaylist(playlist: Playlist) {
        playlistDao.insertPlaylist(playlist)
    }
    
    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlist)
    }
    
    override suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }
    
    override suspend fun deletePlaylistById(playlistId: String) {
        playlistDao.deletePlaylistById(playlistId)
    }
}