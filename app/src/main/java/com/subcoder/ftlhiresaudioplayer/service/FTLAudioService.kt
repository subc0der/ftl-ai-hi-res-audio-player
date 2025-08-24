package com.subcoder.ftlhiresaudioplayer.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.subcoder.ftlhiresaudioplayer.MainActivity
import com.subcoder.ftlhiresaudioplayer.data.repository.TrackRepository
import com.subcoder.ftlhiresaudioplayer.data.repository.PlaylistRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FTL Audio Service - Main MediaSessionService for hi-res audio playback
 * 
 * Features:
 * - MediaBrowserService compatibility for Android Auto/Wear
 * - Hi-res audio format support (FLAC, DSD, 192kHz/32-bit)
 * - Background playback with proper lifecycle management
 * - Integration with database layer for track loading
 * - Custom notification with audiophile controls
 */
@AndroidEntryPoint
@UnstableApi
class FTLAudioService : MediaSessionService() {
    
    @Inject
    lateinit var trackRepository: TrackRepository
    
    @Inject
    lateinit var playlistRepository: PlaylistRepository
    
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "FTL_AUDIO_PLAYBACK"
        private const val NOTIFICATION_ID = 1001
    }
    
    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeMediaSession()
    }
    
    private fun initializePlayer() {
        // Create ExoPlayer with hi-res audio optimizations
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true // Handle audio focus
            )
            .setWakeMode(C.WAKE_MODE_LOCAL) // Keep CPU awake during playback
            .setHandleAudioBecomingNoisy(true) // Pause when headphones disconnected
            .build()
        
        // Set up player listeners
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                handlePlaybackStateChanged(playbackState)
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                handleMediaItemTransition(mediaItem, reason)
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                super.onPlayerError(error)
                handlePlayerError(error)
            }
        })
    }
    
    private fun initializeMediaSession() {
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        mediaSession = MediaSession.Builder(this, player!!)
            .setCallback(FTLMediaSessionCallback(
                trackRepository = trackRepository,
                playlistRepository = playlistRepository,
                serviceScope = serviceScope
            ))
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceScope.cancel()
        super.onDestroy()
    }
    
    private fun handlePlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                // Player is ready to play
            }
            Player.STATE_BUFFERING -> {
                // Player is buffering
            }
            Player.STATE_ENDED -> {
                // Playback ended - handle next track or repeat
                handlePlaybackEnded()
            }
            Player.STATE_IDLE -> {
                // Player is idle
            }
        }
    }
    
    private fun handleMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (mediaItem != null && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
            // Update play statistics when track changes automatically
            serviceScope.launch {
                val trackId = mediaItem.mediaId
                trackRepository.incrementPlayCount(trackId)
                trackRepository.updateLastPlayed(trackId, System.currentTimeMillis())
            }
        }
    }
    
    private fun handlePlayerError(error: androidx.media3.common.PlaybackException) {
        // Log error and attempt recovery
        when (error.errorCode) {
            androidx.media3.common.PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
                // File not found - skip to next track
                player?.seekToNext()
            }
            androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> {
                // Unsupported format - skip to next track
                player?.seekToNext()
            }
            else -> {
                // General error - attempt to restart playback
                player?.prepare()
            }
        }
    }
    
    private fun handlePlaybackEnded() {
        // Handle end of playback based on repeat mode
        when (player?.repeatMode) {
            Player.REPEAT_MODE_ONE -> {
                // Repeat current track
                player?.seekTo(0)
                player?.play()
            }
            Player.REPEAT_MODE_ALL -> {
                // Continue to next track (handled automatically by ExoPlayer)
            }
            else -> {
                // Stop playback
                player?.pause()
            }
        }
    }
}