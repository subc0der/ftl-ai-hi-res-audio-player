package com.subcoder.ftlhiresaudioplayer.data.repository

import com.subcoder.ftlhiresaudioplayer.data.database.entities.Track
import kotlinx.coroutines.flow.Flow

/**
 * Playlist Repository Interface - Contract for playlist data operations
 * 
 * Simplified version for service integration
 */
interface PlaylistRepository {
    
    // Basic queue operations for audio service
    suspend fun getCurrentQueue(): List<Track>
    suspend fun setCurrentQueue(tracks: List<Track>)
    suspend fun addToQueue(track: Track)
    suspend fun removeFromQueue(trackId: String)
    suspend fun clearQueue()
    
    // Queue management
    fun getQueueFlow(): Flow<List<Track>>
    suspend fun reorderQueue(fromPosition: Int, toPosition: Int)
}