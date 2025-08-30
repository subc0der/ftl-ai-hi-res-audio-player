package com.ftl.hires.audioplayer.audio

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.ftl.hires.audioplayer.data.database.entities.Track
import com.ftl.hires.audioplayer.service.AudioService
import com.ftl.hires.audioplayer.audio.equalizer.EqualizerBand
import com.ftl.hires.audioplayer.audio.equalizer.EqualizerPreset
import com.ftl.hires.audioplayer.audio.equalizer.EQMode
import com.ftl.hires.audioplayer.audio.equalizer.EQConfiguration
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Audio Controller - Bridge between UI and AudioService
 * 
 * Provides a clean interface for controlling audio playback
 * and observing playback state changes.
 */
@Singleton
class AudioController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val equalizerProcessor: FTLEqualizerProcessor
) {
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Playback state flows
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()
    
    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()
    
    private val _playbackDuration = MutableStateFlow(0L)
    val playbackDuration: StateFlow<Long> = _playbackDuration.asStateFlow()
    
    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()
    
    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()
    
    // Queue management
    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()
    
    private val _queuePosition = MutableStateFlow(-1)
    val queuePosition: StateFlow<Int> = _queuePosition.asStateFlow()
    
    private val _hasNext = MutableStateFlow(false)
    val hasNext: StateFlow<Boolean> = _hasNext.asStateFlow()
    
    private val _hasPrevious = MutableStateFlow(false)
    val hasPrevious: StateFlow<Boolean> = _hasPrevious.asStateFlow()
    
    private val _volume = MutableStateFlow(0.75f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    // Equalizer state flows
    val equalizerBands: StateFlow<List<EqualizerBand>> = equalizerProcessor.currentBands
    val activeEqualizerPreset: StateFlow<EqualizerPreset?> = equalizerProcessor.activePreset
    val isEqualizerEnabled: StateFlow<Boolean> = equalizerProcessor.isEnabled
    val currentEQMode: StateFlow<EQMode> = equalizerProcessor.currentMode
    val currentEQConfiguration: StateFlow<EQConfiguration> = equalizerProcessor.currentConfiguration

    enum class RepeatMode {
        OFF, ONE, ALL
    }

    init {
        connectToService()
    }

    private fun connectToService() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, AudioService::class.java)
        )
        
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                try {
                    mediaController = controllerFuture?.get()
                    setupPlayerListener()
                    startProgressUpdates()
                    // EQ is now initialized in AudioService with the actual ExoPlayer
                    Timber.d("Connected to AudioService")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to connect to AudioService")
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState()
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let {
                    updateCurrentTrack(it)
                }
            }
            
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _shuffleEnabled.value = shuffleModeEnabled
            }
            
            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.value = when (repeatMode) {
                    Player.REPEAT_MODE_OFF -> RepeatMode.OFF
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
            }
        })
    }

    private fun updatePlaybackState() {
        mediaController?.let { controller ->
            _isPlaying.value = controller.isPlaying
            _isBuffering.value = controller.playbackState == Player.STATE_BUFFERING
            _playbackDuration.value = if (controller.duration > 0) controller.duration else 0L
            _hasNext.value = controller.hasNextMediaItem()
            _hasPrevious.value = controller.hasPreviousMediaItem()
        }
    }

    private fun updateCurrentTrack(mediaItem: MediaItem) {
        val metadata = mediaItem.mediaMetadata
        // Create a basic Track from metadata
        _currentTrack.value = Track(
            id = mediaItem.mediaId ?: "",
            title = metadata.title?.toString() ?: "Unknown",
            artistId = null,
            artistName = metadata.artist?.toString() ?: "Unknown Artist",
            albumId = null,
            albumName = metadata.albumTitle?.toString() ?: "Unknown Album",
            durationMs = mediaController?.duration ?: 0L,
            filePath = mediaItem.requestMetadata.mediaUri?.toString() ?: "",
            fileSize = 0,
            format = "audio",
            bitrate = null,
            sampleRate = null,
            bitDepth = null,
            channels = null,
            trackNumber = metadata.trackNumber,
            discNumber = null,
            year = metadata.recordingYear,
            genre = metadata.genre?.toString(),
            artworkPath = metadata.artworkUri?.toString(),
            playCount = 0,
            lastPlayed = null,
            dateAdded = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis(),
            isFavorite = false
        )
    }

    private fun startProgressUpdates() {
        scope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _playbackPosition.value = controller.currentPosition
                    }
                }
                delay(100) // Update every 100ms
            }
        }
    }

    /**
     * Play a single track
     */
    fun playTrack(track: Track) {
        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(track.filePath)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artistName)
                    .setAlbumTitle(track.albumName)
                    .setArtworkUri(track.artworkPath?.let { android.net.Uri.parse(it) })
                    .build()
            )
            .build()
        
        mediaController?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
        
        _currentTrack.value = track
        _queue.value = listOf(track)
        _queuePosition.value = 0
    }

    /**
     * Play a queue of tracks
     */
    fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        
        val mediaItems = tracks.map { track ->
            MediaItem.Builder()
                .setMediaId(track.id)
                .setUri(track.filePath)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artistName)
                        .setAlbumTitle(track.albumName)
                        .setArtworkUri(track.artworkPath?.let { android.net.Uri.parse(it) })
                        .build()
                )
                .build()
        }
        
        mediaController?.apply {
            setMediaItems(mediaItems, startIndex, 0)
            prepare()
            play()
        }
        
        _queue.value = tracks
        _queuePosition.value = startIndex
        if (startIndex < tracks.size) {
            _currentTrack.value = tracks[startIndex]
        }
    }

    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                if (controller.playbackState == Player.STATE_IDLE) {
                    controller.prepare()
                }
                controller.play()
            }
        }
    }

    /**
     * Play
     */
    fun play() {
        mediaController?.apply {
            if (playbackState == Player.STATE_IDLE) {
                prepare()
            }
            play()
        }
    }

    /**
     * Pause
     */
    fun pause() {
        mediaController?.pause()
    }

    /**
     * Skip to next track
     */
    fun skipToNext() {
        mediaController?.seekToNextMediaItem()
    }

    /**
     * Skip to previous track
     */
    fun skipToPrevious() {
        mediaController?.let { controller ->
            if (controller.currentPosition > 3000) {
                controller.seekTo(0)
            } else {
                controller.seekToPreviousMediaItem()
            }
        }
    }

    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    /**
     * Toggle shuffle
     */
    fun toggleShuffle() {
        mediaController?.let { controller ->
            controller.shuffleModeEnabled = !controller.shuffleModeEnabled
        }
    }

    /**
     * Cycle repeat mode
     */
    fun cycleRepeatMode() {
        mediaController?.let { controller ->
            val nextMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
                else -> Player.REPEAT_MODE_OFF
            }
            controller.repeatMode = nextMode
        }
    }

    /**
     * Set repeat mode
     */
    fun setRepeatMode(mode: RepeatMode) {
        mediaController?.let { controller ->
            controller.repeatMode = when (mode) {
                RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            }
        }
    }

    /**
     * Add track to queue
     */
    fun addToQueue(track: Track) {
        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(track.filePath)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artistName)
                    .setAlbumTitle(track.albumName)
                    .setArtworkUri(track.artworkPath?.let { android.net.Uri.parse(it) })
                    .build()
            )
            .build()
        
        mediaController?.addMediaItem(mediaItem)
        _queue.value = _queue.value + track
    }

    /**
     * Remove track from queue
     */
    fun removeFromQueue(index: Int) {
        if (index in 0 until (_queue.value.size)) {
            mediaController?.removeMediaItem(index)
            _queue.value = _queue.value.filterIndexed { i, _ -> i != index }
        }
    }

    /**
     * Clear queue
     */
    fun clearQueue() {
        mediaController?.clearMediaItems()
        _queue.value = emptyList()
        _currentTrack.value = null
        _queuePosition.value = -1
    }

    /**
     * Stop playback
     */
    fun stop() {
        mediaController?.stop()
        clearQueue()
    }
    
    /**
     * Set volume (0.0 to 1.0)
     */
    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        _volume.value = clampedVolume
        mediaController?.volume = clampedVolume
    }
    
    /**
     * Get current volume
     */
    fun getVolume(): Float {
        return mediaController?.volume ?: _volume.value
    }
    
    // === EQUALIZER METHODS ===
    // Note: Equalizer is now initialized in AudioService with the actual ExoPlayer
    
    /**
     * Update a single EQ band in real-time
     */
    fun updateEqualizerBand(bandId: Int, gain: Float) {
        equalizerProcessor.updateBand(bandId, gain)
    }
    
    /**
     * Apply an EQ preset
     */
    fun applyEqualizerPreset(preset: EqualizerPreset) {
        equalizerProcessor.applyPreset(preset)
    }
    
    /**
     * Reset equalizer to flat response (all bands to 0dB)
     */
    fun resetEqualizer() {
        equalizerProcessor.resetToFlat()
    }
    
    /**
     * Enable/disable the equalizer
     */
    fun setEqualizerEnabled(enabled: Boolean) {
        equalizerProcessor.setEnabled(enabled)
    }
    
    /**
     * Get current equalizer bands for UI updates
     */
    fun getCurrentEqualizerBands(): List<EqualizerBand> {
        return equalizerProcessor.getCurrentBands()
    }
    
    /**
     * Check if equalizer is ready and functional
     */
    fun isEqualizerReady(): Boolean {
        return equalizerProcessor.isReady()
    }
    
    /**
     * Switch EQ mode with intelligent gain preservation
     */
    fun switchEQMode(mode: EQMode) {
        equalizerProcessor.switchMode(mode)
    }
    
    /**
     * Get available EQ modes
     */
    fun getAvailableEQModes(): List<EQMode> {
        return equalizerProcessor.getAvailableModes()
    }
    
    /**
     * Get current EQ mode
     */
    fun getCurrentEQMode(): EQMode {
        return equalizerProcessor.getCurrentMode()
    }
    
    /**
     * Get current EQ configuration
     */
    fun getCurrentEQConfiguration(): EQConfiguration {
        return equalizerProcessor.getCurrentConfiguration()
    }
    
    /**
     * Get detailed equalizer information for debugging
     */
    fun getEqualizerInfo(): FTLEqualizerProcessor.EqualizerInfo? {
        return equalizerProcessor.getEqualizerInfo()
    }

    fun release() {
        equalizerProcessor.release()
        mediaController?.release()
        controllerFuture?.cancel(true)
    }
}