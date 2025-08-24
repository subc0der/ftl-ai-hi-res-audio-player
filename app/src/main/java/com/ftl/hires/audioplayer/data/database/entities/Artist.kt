package com.ftl.hires.audioplayer.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

@Entity(
    tableName = "artists",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class Artist(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "sort_name")
    val sortName: String? = null,
    
    @ColumnInfo(name = "artwork_path")
    val artworkPath: String? = null,
    
    @ColumnInfo(name = "biography")
    val biography: String? = null,
    
    @ColumnInfo(name = "genre")
    val genre: String? = null,
    
    @ColumnInfo(name = "country")
    val country: String? = null,
    
    @ColumnInfo(name = "formed_year")
    val formedYear: Int? = null,
    
    @ColumnInfo(name = "track_count")
    val trackCount: Int = 0,
    
    @ColumnInfo(name = "album_count")
    val albumCount: Int = 0,
    
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
    
    @ColumnInfo(name = "has_hi_res_content")
    val hasHiResContent: Boolean = false
)