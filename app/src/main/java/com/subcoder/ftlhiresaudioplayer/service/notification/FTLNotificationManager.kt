package com.subcoder.ftlhiresaudioplayer.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import com.subcoder.ftlhiresaudioplayer.MainActivity
import com.subcoder.ftlhiresaudioplayer.R
import com.subcoder.ftlhiresaudioplayer.service.FTLAudioService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * FTL Notification Manager - Custom notification for audiophile playback
 * 
 * Features:
 * - Rich media notifications with album artwork
 * - Hi-res audio format display in notification
 * - Custom audiophile controls (EQ, Replay Gain, Format Info)
 * - Support for Android Auto and Wear OS
 * - Compact and expanded notification layouts
 * - Real-time audio format updates
 */
@UnstableApi
class FTLNotificationManager(
    private val context: Context,
    private val mediaSession: MediaSession
) {
    
    private val notificationManager = NotificationManagerCompat.from(context)
    private var currentNotificationId: Int = NOTIFICATION_ID
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "FTL_AUDIO_PLAYBACK"
        const val NOTIFICATION_ID = 1001
        
        // Custom action IDs
        const val ACTION_EQ_SETTINGS = "com.ftl.audio.EQ_SETTINGS"
        const val ACTION_AUDIO_INFO = "com.ftl.audio.AUDIO_INFO"
        const val ACTION_REPLAY_GAIN = "com.ftl.audio.REPLAY_GAIN"
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "FTL Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for FTL Hi-Res Audio Player"
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    fun buildNotification(): android.app.Notification {
        val player = mediaSession.player
        val metadata = player.currentMediaItem?.mediaMetadata
        
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(metadata?.title ?: "Unknown Track")
            .setContentText(metadata?.artist ?: "Unknown Artist")
            .setSubText(getAudioFormatText(metadata))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(createContentIntent())
            .setDeleteIntent(createDeleteIntent())
            .setOngoing(player.isPlaying)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        
        // Add album artwork
        loadAndSetArtwork(builder, metadata?.artworkUri)
        
        // Configure media style
        val mediaStyle = androidx.media3.session.MediaStyleNotificationHelper.MediaStyle(mediaSession)
            .setShowActionsInCompactView(0, 1, 2) // Previous, Play/Pause, Next
            .setShowCancelButton(true)
            .setCancelButtonIntent(createDeleteIntent())
        
        builder.setStyle(mediaStyle)
        
        // Add playback actions
        addPlaybackActions(builder, player)
        
        // Add custom audiophile actions
        addAudiophileActions(builder, metadata)
        
        return builder.build()
    }
    
    private fun getAudioFormatText(metadata: MediaMetadata?): String {
        val extras = metadata?.extras
        return if (extras != null) {
            val sampleRate = extras.getInt("sampleRate", 44100)
            val bitDepth = extras.getInt("bitDepth", 16)
            val format = extras.getString("format") ?: "Unknown"
            val isHiRes = extras.getBoolean("isHiRes", false)
            val isDSD = extras.getBoolean("isDSD", false)
            
            buildString {
                append(format)
                if (isDSD) {
                    append(" DSD")
                } else {
                    append(" ${sampleRate}Hz/${bitDepth}bit")
                }
                if (isHiRes) append(" • Hi-Res")
            }
        } else {
            "Standard Quality"
        }
    }
    
    private fun loadAndSetArtwork(
        builder: NotificationCompat.Builder,
        artworkUri: Uri?
    ) {
        if (artworkUri != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bitmap = loadArtworkBitmap(artworkUri)
                    withContext(Dispatchers.Main) {
                        builder.setLargeIcon(bitmap)
                    }
                } catch (e: Exception) {
                    // Use default artwork
                    withContext(Dispatchers.Main) {
                        builder.setLargeIcon(getDefaultArtwork())
                    }
                }
            }
        } else {
            builder.setLargeIcon(getDefaultArtwork())
        }
    }
    
    private fun loadArtworkBitmap(uri: Uri): Bitmap? {
        return try {
            when (uri.scheme) {
                "file" -> {
                    val file = File(uri.path!!)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    } else null
                }
                "content" -> {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getDefaultArtwork(): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.ic_default_album_art)
    }
    
    private fun addPlaybackActions(
        builder: NotificationCompat.Builder,
        player: Player
    ) {
        // Previous track
        builder.addAction(
            NotificationCompat.Action.Builder(
                R.drawable.ic_skip_previous,
                "Previous",
                createCustomActionPendingIntent("PREVIOUS")
            ).build()
        )
        
        // Play/Pause
        val playPauseIcon = if (player.isPlaying) {
            R.drawable.ic_pause
        } else {
            R.drawable.ic_play
        }
        
        val playPauseAction = if (player.isPlaying) "PAUSE" else "PLAY"
        
        builder.addAction(
            NotificationCompat.Action.Builder(
                playPauseIcon,
                if (player.isPlaying) "Pause" else "Play",
                createCustomActionPendingIntent(playPauseAction)
            ).build()
        )
        
        // Next track
        builder.addAction(
            NotificationCompat.Action.Builder(
                R.drawable.ic_skip_next,
                "Next",
                createCustomActionPendingIntent("NEXT")
            ).build()
        )
    }
    
    private fun addAudiophileActions(
        builder: NotificationCompat.Builder,
        metadata: MediaMetadata?
    ) {
        // EQ Settings action
        val eqIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_EQ_SETTINGS
            putExtra("open_equalizer", true)
        }
        
        builder.addAction(
            NotificationCompat.Action.Builder(
                R.drawable.ic_equalizer,
                "EQ",
                PendingIntent.getActivity(
                    context,
                    0,
                    eqIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            ).build()
        )
        
        // Audio info action
        val infoIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_AUDIO_INFO
            putExtra("show_audio_info", true)
        }
        
        builder.addAction(
            NotificationCompat.Action.Builder(
                R.drawable.ic_info,
                "Info",
                PendingIntent.getActivity(
                    context,
                    1,
                    infoIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            ).build()
        )
    }
    
    private fun createContentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createDeleteIntent(): PendingIntent {
        return createCustomActionPendingIntent("STOP")
    }
    
    private fun createCustomActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(context, FTLAudioService::class.java).apply {
            this.action = action
        }
        
        return PendingIntent.getService(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    fun updateNotification() {
        val notification = buildNotification()
        notificationManager.notify(currentNotificationId, notification)
    }
    
    fun cancelNotification() {
        notificationManager.cancel(currentNotificationId)
    }
}