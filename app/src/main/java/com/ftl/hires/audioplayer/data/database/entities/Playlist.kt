package com.ftl.hires.audioplayer.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.Junction
import androidx.room.Relation

@Entity(
    tableName = "playlists",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class Playlist(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "artwork_path")
    val artworkPath: String? = null,
    
    @ColumnInfo(name = "track_count")
    val trackCount: Int = 0,
    
    @ColumnInfo(name = "total_duration")
    val totalDuration: Long? = null,
    
    @ColumnInfo(name = "total_duration_ms")
    val totalDurationMs: Long = 0,
    
    @ColumnInfo(name = "play_count")
    val playCount: Long = 0,
    
    @ColumnInfo(name = "last_played")
    val lastPlayed: Long? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "date_created")
    val dateCreated: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_modified")
    val lastModified: Long? = null,
    
    @ColumnInfo(name = "date_modified")
    val dateModified: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "is_smart_playlist")
    val isSmartPlaylist: Boolean = false,
    
    @ColumnInfo(name = "smart_criteria")
    val smartCriteria: String? = null, // JSON string for smart playlist rules
    
    @ColumnInfo(name = "sort_order")
    val sortOrder: String? = "custom", // custom, title, artist, album, date_added, etc.
    
    @ColumnInfo(name = "is_system_playlist")
    val isSystemPlaylist: Boolean = false
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlist_id", "track_id", "position"],
    indices = [
        Index(value = ["playlist_id"]),
        Index(value = ["track_id"]),
        Index(value = ["position"])
    ]
)
data class PlaylistTrack(
    @ColumnInfo(name = "playlist_id")
    val playlistId: String,
    
    @ColumnInfo(name = "track_id")
    val trackId: String,
    
    @ColumnInfo(name = "position")
    val position: Int,
    
    @ColumnInfo(name = "date_added")
    val dateAdded: Long = System.currentTimeMillis()
)

data class PlaylistWithTracks(
    val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistTrack::class,
            parentColumn = "playlist_id",
            entityColumn = "track_id"
        )
    )
    val tracks: List<Track>
)

data class PlaylistWithTrackDetails(
    val playlist: Playlist,
    val tracks: List<TrackWithPosition>
)

data class TrackWithPosition(
    val track: Track,
    val position: Int,
    val dateAdded: Long
)