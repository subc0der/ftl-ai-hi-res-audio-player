package com.ftl.hires.audioplayer.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["album_id"]),
        Index(value = ["artist_id"]),
        Index(value = ["file_path"], unique = true)
    ]
)
data class Track(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "artist_id")
    val artistId: String?,
    
    @ColumnInfo(name = "artist_name")
    val artistName: String?,
    
    @ColumnInfo(name = "album_id")
    val albumId: String?,
    
    @ColumnInfo(name = "album_name")
    val albumName: String?,
    
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    
    @ColumnInfo(name = "file_path")
    val filePath: String,
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long,
    
    @ColumnInfo(name = "format")
    val format: String,
    
    @ColumnInfo(name = "bitrate")
    val bitrate: Int?,
    
    @ColumnInfo(name = "sample_rate")
    val sampleRate: Int?,
    
    @ColumnInfo(name = "bit_depth")
    val bitDepth: Int?,
    
    @ColumnInfo(name = "channels")
    val channels: Int?,
    
    @ColumnInfo(name = "track_number")
    val trackNumber: Int?,
    
    @ColumnInfo(name = "disc_number")
    val discNumber: Int?,
    
    @ColumnInfo(name = "year")
    val year: Int?,
    
    @ColumnInfo(name = "genre")
    val genre: String?,
    
    @ColumnInfo(name = "artwork_path")
    val artworkPath: String?,
    
    @ColumnInfo(name = "play_count")
    val playCount: Long = 0,
    
    @ColumnInfo(name = "last_played")
    val lastPlayed: Long? = null,
    
    @ColumnInfo(name = "date_added")
    val dateAdded: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "date_modified")
    val dateModified: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "is_high_res")
    val isHighRes: Boolean = false,
    
    @ColumnInfo(name = "eq_preset")
    val eqPreset: String? = null,
    
    @ColumnInfo(name = "replay_gain")
    val replayGain: Float? = null,
    
    @ColumnInfo(name = "peak_amplitude")
    val peakAmplitude: Float? = null
)

