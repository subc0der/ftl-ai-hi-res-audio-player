package com.ftl.hires.audioplayer.data.database.entities

data class TrackWithRelations(
    val track: Track,
    val artist: Artist?,
    val album: Album?
)