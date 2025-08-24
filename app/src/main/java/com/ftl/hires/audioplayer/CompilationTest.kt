package com.ftl.hires.audioplayer

// Simple compilation test to verify imports and dependencies work correctly
class CompilationTest {
    // Test that our entities can be instantiated
    fun testEntitiesCompile() {
        val track = com.ftl.hires.audioplayer.data.database.entities.Track(
            id = "test",
            title = "Test Track",
            artistId = null,
            artistName = "Test Artist",
            albumId = null,
            albumName = "Test Album",
            durationMs = 1000L,
            filePath = "/test/path",
            fileSize = 1000L,
            format = "mp3",
            bitrate = null,
            sampleRate = null,
            bitDepth = null,
            channels = null,
            trackNumber = null,
            discNumber = null,
            year = null,
            genre = null,
            artworkPath = null
        )
        
        val artist = com.ftl.hires.audioplayer.data.database.entities.Artist(
            id = "test",
            name = "Test Artist"
        )
        
        val album = com.ftl.hires.audioplayer.data.database.entities.Album(
            id = "test",
            title = "Test Album",
            artistId = "test",
            artistName = "Test Artist"
        )
        
        val playlist = com.ftl.hires.audioplayer.data.database.entities.Playlist(
            id = "test",
            name = "Test Playlist"
        )
        
        // Test relations
        val trackWithRelations = com.ftl.hires.audioplayer.data.database.entities.TrackWithRelations(
            track = track,
            artist = artist,
            album = album
        )
    }
}