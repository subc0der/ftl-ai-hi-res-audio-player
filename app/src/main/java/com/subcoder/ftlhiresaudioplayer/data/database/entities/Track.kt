package com.subcoder.ftlhiresaudioplayer.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Track Entity - Represents an audio track with hi-res audio support
 * 
 * Audiophile Features:
 * - Hi-res audio metadata (sample rate, bit depth, channels)
 * - Audio quality indicators (bitrate, format, replay gain)
 * - DSD format support detection
 * - User preferences (play count, favorites, EQ presets)
 * - File management (path, size, modification tracking)
 */
@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["path"], unique = true),
        Index(value = ["artist_id"]),
        Index(value = ["album_id"]),
        Index(value = ["title"]),
        Index(value = ["artist_name"]),
        Index(value = ["is_hi_res"]),
        Index(value = ["format"])
    ]
)
data class Track(
    @PrimaryKey
    val id: String,
    
    // Basic metadata
    val title: String,
    @ColumnInfo(name = "artist_name")
    val artistName: String,
    @ColumnInfo(name = "album_name") 
    val albumName: String,
    @ColumnInfo(name = "artist_id")
    val artistId: String? = null,
    @ColumnInfo(name = "album_id")
    val albumId: String? = null,
    
    // Track details
    @ColumnInfo(name = "track_number")
    val trackNumber: Int? = null,
    @ColumnInfo(name = "disc_number")
    val discNumber: Int? = null,
    val genre: String? = null,
    val year: Int? = null,
    val duration: Long, // milliseconds
    
    // File information
    val path: String,
    @ColumnInfo(name = "file_size")
    val fileSize: Long,
    @ColumnInfo(name = "date_added")
    val dateAdded: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "date_modified")
    val dateModified: Long,
    
    // Audio quality metadata
    val format: String, // FLAC, MP3, WAV, DSD, APE, etc.
    val codec: String? = null,
    val bitrate: Int, // kbps
    @ColumnInfo(name = "sample_rate")
    val sampleRate: Int, // Hz (44100, 48000, 96000, 192000, etc.)
    @ColumnInfo(name = "bit_depth")
    val bitDepth: Int, // 16, 24, 32 bits
    val channels: Int, // 1=mono, 2=stereo, 6=5.1, 8=7.1
    
    // Hi-res audio indicators
    @ColumnInfo(name = "is_hi_res")
    val isHiRes: Boolean, // 96kHz+ or 24bit+
    @ColumnInfo(name = "is_dsd")
    val isDSD: Boolean = false, // DSD format detection
    
    // Audio processing
    @ColumnInfo(name = "replay_gain")
    val replayGain: Float = 0.0f, // dB adjustment
    @ColumnInfo(name = "eq_preset")
    val eqPreset: String? = null,
    
    // User data
    @ColumnInfo(name = "play_count")
    val playCount: Int = 0,
    @ColumnInfo(name = "last_played")
    val lastPlayed: Long? = null,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    val rating: Float = 0.0f, // 0.0 to 5.0 stars
    
    // Artwork
    @ColumnInfo(name = "artwork_path")
    val artworkPath: String? = null,
    @ColumnInfo(name = "has_embedded_artwork")
    val hasEmbeddedArtwork: Boolean = false
)