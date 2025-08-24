package com.ftl.hires.audioplayer.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.ForeignKey

@Entity(
    tableName = "albums",
    indices = [
        Index(value = ["artist_id"]),
        Index(value = ["title", "artist_id"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Album(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "artist_id")
    val artistId: String?,
    
    @ColumnInfo(name = "artist_name")
    val artistName: String?,
    
    @ColumnInfo(name = "sort_title")
    val sortTitle: String? = null,
    
    @ColumnInfo(name = "year")
    val year: Int? = null,
    
    @ColumnInfo(name = "genre")
    val genre: String? = null,
    
    @ColumnInfo(name = "artwork_path")
    val artworkPath: String? = null,
    
    @ColumnInfo(name = "album_type")
    val albumType: String? = null, // album, single, ep, compilation
    
    @ColumnInfo(name = "record_label")
    val recordLabel: String? = null,
    
    @ColumnInfo(name = "catalog_number")
    val catalogNumber: String? = null,
    
    @ColumnInfo(name = "track_count")
    val trackCount: Int = 0,
    
    @ColumnInfo(name = "total_tracks")
    val totalTracks: Int = 0,
    
    @ColumnInfo(name = "total_discs")
    val totalDiscs: Int = 1,
    
    @ColumnInfo(name = "total_duration")
    val totalDuration: Long? = null,
    
    @ColumnInfo(name = "total_duration_ms")
    val totalDurationMs: Long = 0,
    
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
    
    @ColumnInfo(name = "average_bitrate")
    val averageBitrate: Int? = null,
    
    @ColumnInfo(name = "average_sample_rate")
    val averageSampleRate: Int? = null,
    
    @ColumnInfo(name = "is_compilation")
    val isCompilation: Boolean = false,
    
    @ColumnInfo(name = "has_hi_res_content")
    val hasHiResContent: Boolean = false
)