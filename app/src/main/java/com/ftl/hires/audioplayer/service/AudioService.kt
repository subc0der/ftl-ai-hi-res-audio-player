package com.ftl.hires.audioplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.ftl.hires.audioplayer.MainActivity
import com.ftl.hires.audioplayer.R
import com.ftl.hires.audioplayer.audio.FTLEqualizerProcessor
import com.ftl.hires.audioplayer.data.database.entities.Track
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * FTL Audio Service - High-Resolution Audio Playback Engine
 * 
 * Core audio playback service using Media3 ExoPlayer for:
 * - Hi-res audio playback (FLAC, DSD, WAV)
 * - Gapless playback
 * - Audio focus management
 * - Media session integration
 * - Background playback with notification
 */
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class AudioService : MediaSessionService() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "ftl_audio_playback"
        private const val NOTIFICATION_ID = 1001
        
        // Service actions
        const val ACTION_PLAY = "com.ftl.hires.audioplayer.PLAY"
        const val ACTION_PAUSE = "com.ftl.hires.audioplayer.PAUSE"
        const val ACTION_NEXT = "com.ftl.hires.audioplayer.NEXT"
        const val ACTION_PREVIOUS = "com.ftl.hires.audioplayer.PREVIOUS"
        const val ACTION_STOP = "com.ftl.hires.audioplayer.STOP"
        
        // Intent extras
        const val EXTRA_TRACK_ID = "track_id"
        const val EXTRA_PLAYLIST_ID = "playlist_id"
        const val EXTRA_QUEUE_TRACKS = "queue_tracks"
    }

    @Inject
    lateinit var exoPlayer: ExoPlayer
    
    @Inject
    lateinit var equalizerProcessor: FTLEqualizerProcessor
    
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressUpdateJob: Job? = null
    
    // Audio session ID for EQ
    private val _audioSessionId = MutableStateFlow(-1)
    val audioSessionId: StateFlow<Int> = _audioSessionId.asStateFlow()
    
    // Playback state flows
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()
    
    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()
    
    private val _playbackDuration = MutableStateFlow(0L)
    val playbackDuration: StateFlow<Long> = _playbackDuration.asStateFlow()
    
    // Queue management
    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()
    
    private val _queuePosition = MutableStateFlow(0)
    val queuePosition: StateFlow<Int> = _queuePosition.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        Timber.d("AudioService created")
        
        createNotificationChannel()
        initializePlayer()
        initializeMediaSession()
        startProgressUpdates()
    }

    private fun initializePlayer() {
        // Configure audio attributes for music playback
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        
        exoPlayer.setAudioAttributes(audioAttributes, true)
        exoPlayer.setHandleAudioBecomingNoisy(true)
        exoPlayer.setWakeMode(C.WAKE_MODE_NETWORK)
        
        // Initialize equalizer with the actual ExoPlayer's audio session
        initializeEqualizer()
        
        // Add player listener
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState()
                when (playbackState) {
                    Player.STATE_READY -> {
                        Timber.d("Player ready")
                        _playbackDuration.value = exoPlayer.duration
                        // Reinitialize EQ if audio session changed
                        if (exoPlayer.audioSessionId != _audioSessionId.value) {
                            initializeEqualizer()
                        }
                    }
                    Player.STATE_ENDED -> {
                        Timber.d("Playback ended")
                        handlePlaybackEnded()
                    }
                    Player.STATE_BUFFERING -> {
                        Timber.d("Buffering...")
                    }
                    Player.STATE_IDLE -> {
                        Timber.d("Player idle")
                    }
                }
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState()
                if (isPlaying) {
                    startProgressUpdates()
                } else {
                    stopProgressUpdates()
                }
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let {
                    updateCurrentTrack(it)
                }
            }
        })
    }
    
    private fun initializeEqualizer() {
        try {
            val sessionId = exoPlayer.audioSessionId
            if (sessionId != _audioSessionId.value && sessionId != C.AUDIO_SESSION_ID_UNSET) {
                _audioSessionId.value = sessionId
                equalizerProcessor.initialize(exoPlayer)
                Timber.d("Equalizer initialized with audio session ID: $sessionId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize equalizer")
        }
    }

    private fun initializeMediaSession() {
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(createPendingIntent())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_PLAY -> resume()
            ACTION_PAUSE -> pause()
            ACTION_NEXT -> skipToNext()
            ACTION_PREVIOUS -> skipToPrevious()
            ACTION_STOP -> stop()
            else -> {
                // Check for track/playlist to play
                intent.getStringExtra(EXTRA_TRACK_ID)?.let { trackId ->
                    loadAndPlayTrack(trackId)
                }
            }
        }
    }

    /**
     * Load and play a single track
     */
    fun loadAndPlayTrack(trackId: String) {
        serviceScope.launch {
            try {
                // TODO: Load track from repository
                // For now, create a sample MediaItem
                val mediaItem = MediaItem.Builder()
                    .setUri(Uri.parse("content://media/external/audio/media/$trackId"))
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle("Sample Track")
                            .setArtist("Sample Artist")
                            .build()
                    )
                    .build()
                
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
                
                Timber.d("Playing track: $trackId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load track: $trackId")
            }
        }
    }

    /**
     * Load and play a queue of tracks
     */
    fun loadQueue(tracks: List<Track>, startIndex: Int = 0) {
        serviceScope.launch {
            try {
                _queue.value = tracks
                _queuePosition.value = startIndex
                
                val mediaItems = tracks.map { track ->
                    MediaItem.Builder()
                        .setUri(Uri.parse(track.filePath))
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(track.title)
                                .setArtist(track.artistName)
                                .setAlbumTitle(track.albumName)
                                .setArtworkUri(track.artworkPath?.let { Uri.parse(it) })
                                .build()
                        )
                        .build()
                }
                
                exoPlayer.setMediaItems(mediaItems, startIndex, 0)
                exoPlayer.prepare()
                exoPlayer.play()
                
                Timber.d("Loaded queue with ${tracks.size} tracks, starting at index $startIndex")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load queue")
            }
        }
    }

    /**
     * Play/Resume playback
     */
    fun play() {
        if (exoPlayer.playbackState == Player.STATE_IDLE) {
            exoPlayer.prepare()
        }
        exoPlayer.play()
    }

    /**
     * Resume playback
     */
    fun resume() {
        exoPlayer.play()
    }

    /**
     * Pause playback
     */
    fun pause() {
        exoPlayer.pause()
    }

    /**
     * Stop playback
     */
    fun stop() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        stopSelf()
    }

    /**
     * Skip to next track
     */
    fun skipToNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else if (_playbackState.value.repeatMode == RepeatMode.ALL) {
            exoPlayer.seekTo(0, 0)
        }
    }

    /**
     * Skip to previous track
     */
    fun skipToPrevious() {
        if (exoPlayer.currentPosition > 3000 || !exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekTo(0)
        } else {
            exoPlayer.seekToPreviousMediaItem()
        }
    }

    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    /**
     * Toggle shuffle mode
     */
    fun toggleShuffle() {
        val newShuffleMode = !_playbackState.value.shuffleEnabled
        exoPlayer.shuffleModeEnabled = newShuffleMode
        _playbackState.update { it.copy(shuffleEnabled = newShuffleMode) }
    }

    /**
     * Set repeat mode
     */
    fun setRepeatMode(mode: RepeatMode) {
        val playerRepeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
        exoPlayer.repeatMode = playerRepeatMode
        _playbackState.update { it.copy(repeatMode = mode) }
    }

    private fun updatePlaybackState() {
        _playbackState.update { currentState ->
            currentState.copy(
                isPlaying = exoPlayer.isPlaying,
                isBuffering = exoPlayer.playbackState == Player.STATE_BUFFERING,
                playbackSpeed = exoPlayer.playbackParameters.speed
            )
        }
    }

    private fun updateCurrentTrack(mediaItem: MediaItem) {
        val metadata = mediaItem.mediaMetadata
        // TODO: Convert MediaItem to Track entity
        // For now, update with basic info
        Timber.d("Current track: ${metadata.title}")
    }

    private fun handlePlaybackEnded() {
        when (_playbackState.value.repeatMode) {
            RepeatMode.ONE -> exoPlayer.seekTo(0)
            RepeatMode.ALL -> {
                if (!exoPlayer.hasNextMediaItem()) {
                    exoPlayer.seekTo(0, 0)
                }
            }
            RepeatMode.OFF -> {
                if (!exoPlayer.hasNextMediaItem()) {
                    pause()
                }
            }
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressUpdateJob = serviceScope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    _playbackPosition.value = exoPlayer.currentPosition
                    _playbackDuration.value = exoPlayer.duration
                }
                delay(100) // Update every 100ms
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "FTL Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Audio playback controls"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        stopProgressUpdates()
        equalizerProcessor.release()
        mediaSession?.release()
        exoPlayer.release()
        super.onDestroy()
        Timber.d("AudioService destroyed")
    }

    // Playback state data class
    data class PlaybackState(
        val isPlaying: Boolean = false,
        val isBuffering: Boolean = false,
        val shuffleEnabled: Boolean = false,
        val repeatMode: RepeatMode = RepeatMode.OFF,
        val playbackSpeed: Float = 1.0f
    )

    enum class RepeatMode {
        OFF, ONE, ALL
    }

    // Extension function for MutableStateFlow update
    private inline fun <T> MutableStateFlow<T>.update(function: (T) -> T) {
        value = function(value)
    }
}