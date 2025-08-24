package com.subcoder.ftlhiresaudioplayer.data.repository

import com.subcoder.ftlhiresaudioplayer.data.database.entities.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Playlist Repository Implementation - Simple in-memory queue management
 * 
 * This is a simplified implementation for the audio service
 */
@Singleton
class PlaylistRepositoryImpl @Inject constructor() : PlaylistRepository {
    
    private val _currentQueue = MutableStateFlow<List<Track>>(emptyList())
    
    override suspend fun getCurrentQueue(): List<Track> {
        return _currentQueue.value
    }
    
    override suspend fun setCurrentQueue(tracks: List<Track>) {
        _currentQueue.value = tracks
    }
    
    override suspend fun addToQueue(track: Track) {
        _currentQueue.value = _currentQueue.value + track
    }
    
    override suspend fun removeFromQueue(trackId: String) {
        _currentQueue.value = _currentQueue.value.filter { it.id != trackId }
    }
    
    override suspend fun clearQueue() {
        _currentQueue.value = emptyList()
    }
    
    override fun getQueueFlow(): Flow<List<Track>> {
        return _currentQueue.asStateFlow()
    }
    
    override suspend fun reorderQueue(fromPosition: Int, toPosition: Int) {
        val currentList = _currentQueue.value.toMutableList()
        if (fromPosition in currentList.indices && toPosition in currentList.indices) {
            val item = currentList.removeAt(fromPosition)
            currentList.add(toPosition, item)
            _currentQueue.value = currentList
        }
    }
}