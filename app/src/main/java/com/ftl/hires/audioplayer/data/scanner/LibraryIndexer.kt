package com.ftl.hires.audioplayer.data.scanner

import com.ftl.hires.audioplayer.data.database.dao.LibraryDao
import com.ftl.hires.audioplayer.data.database.dao.TrackDao
import com.ftl.hires.audioplayer.data.database.entities.Album
import com.ftl.hires.audioplayer.data.database.entities.Artist
import com.ftl.hires.audioplayer.data.database.entities.Track
import com.ftl.hires.audioplayer.data.repository.MediaScannerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryIndexer @Inject constructor(
    private val trackDao: TrackDao,
    private val libraryDao: LibraryDao,
    private val metadataExtractor: MetadataExtractor,
    private val artworkExtractor: ArtworkExtractor
) {

    /**
     * Index a batch of audio files and update the database
     */
    suspend fun indexAudioFiles(audioFiles: List<MediaScannerRepository.AudioFileInfo>) {
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Starting to index ${audioFiles.size} audio files")
                
                val tracks = mutableListOf<Track>()
                val artists = mutableMapOf<String, Artist>()
                val albums = mutableMapOf<String, Album>()

                audioFiles.forEach { audioFileInfo ->
                    try {
                        // Create track entity
                        val track = createTrackFromAudioInfo(audioFileInfo)
                        tracks.add(track)

                        // Create or update artist
                        val artistName = audioFileInfo.artist ?: "Unknown Artist"
                        val artist = artists.getOrPut(artistName) {
                            createArtistFromAudioInfo(artistName, audioFileInfo)
                        }.let { existingArtist ->
                            // Update artist stats
                            existingArtist.copy(
                                trackCount = existingArtist.trackCount + 1,
                                hasHiResContent = existingArtist.hasHiResContent || 
                                    metadataExtractor.isHighResFormat(audioFileInfo),
                                playCount = existingArtist.playCount,
                                lastPlayed = existingArtist.lastPlayed
                            )
                        }
                        artists[artistName] = artist

                        // Create or update album
                        val albumKey = "${audioFileInfo.album ?: "Unknown Album"}|${artistName}"
                        val album = albums.getOrPut(albumKey) {
                            createAlbumFromAudioInfo(audioFileInfo, artistName)
                        }.let { existingAlbum ->
                            // Update album stats
                            existingAlbum.copy(
                                trackCount = existingAlbum.trackCount + 1,
                                totalDuration = (existingAlbum.totalDuration ?: 0L) + audioFileInfo.duration,
                                hasHiResContent = existingAlbum.hasHiResContent || 
                                    metadataExtractor.isHighResFormat(audioFileInfo),
                                averageSampleRate = calculateAverageSampleRate(
                                    existingAlbum.averageSampleRate,
                                    existingAlbum.trackCount,
                                    audioFileInfo.sampleRate
                                )
                            )
                        }
                        albums[albumKey] = album

                    } catch (e: Exception) {
                        Timber.e(e, "Failed to process audio file: ${audioFileInfo.filePath}")
                    }
                }

                // Batch insert/update database
                insertOrUpdateArtists(artists.values.toList())
                insertOrUpdateAlbums(albums.values.toList())
                insertOrUpdateTracks(tracks)

                Timber.i("Successfully indexed ${tracks.size} tracks, ${artists.size} artists, ${albums.size} albums")

            } catch (e: Exception) {
                Timber.e(e, "Failed to index audio files")
                throw e
            }
        }
    }

    /**
     * Create Track entity from AudioFileInfo
     */
    private suspend fun createTrackFromAudioInfo(audioInfo: MediaScannerRepository.AudioFileInfo): Track {
        val artworkPath = try {
            artworkExtractor.extractArtwork(audioInfo.filePath)
        } catch (e: Exception) {
            Timber.w(e, "Failed to extract artwork for: ${audioInfo.filePath}")
            null
        }

        return Track(
            id = generateTrackId(audioInfo.filePath), // Generate consistent ID from file path
            title = audioInfo.title ?: extractTitleFromFilename(audioInfo.filePath),
            artist = audioInfo.artist ?: "Unknown Artist",
            album = audioInfo.album ?: "Unknown Album",
            duration = audioInfo.duration,
            filePath = audioInfo.filePath,
            fileSize = audioInfo.fileSize,
            mimeType = getMimeTypeFromFormat(audioInfo.format),
            trackNumber = audioInfo.trackNumber,
            discNumber = audioInfo.discNumber ?: 1,
            year = audioInfo.year,
            genre = audioInfo.genre,
            artworkPath = artworkPath,
            addedAt = System.currentTimeMillis(),
            lastPlayed = null,
            // Hi-res audio properties
            isHighRes = metadataExtractor.isHighResFormat(audioInfo),
            sampleRate = audioInfo.sampleRate,
            bitDepth = audioInfo.bitDepth,
            format = audioInfo.format,
            playCount = 0,
            isFavorite = false,
            eqPreset = null
        )
    }

    /**
     * Create Artist entity from audio information
     */
    private fun createArtistFromAudioInfo(artistName: String, audioInfo: MediaScannerRepository.AudioFileInfo): Artist {
        return Artist(
            id = generateArtistId(artistName), // Generate consistent ID
            name = artistName,
            hasHiResContent = metadataExtractor.isHighResFormat(audioInfo),
            trackCount = 0, // Will be updated during indexing
            playCount = 0,
            lastPlayed = null
        )
    }

    /**
     * Create Album entity from audio information  
     */
    private fun createAlbumFromAudioInfo(audioInfo: MediaScannerRepository.AudioFileInfo, artistName: String): Album {
        val albumTitle = audioInfo.album ?: "Unknown Album"
        return Album(
            id = generateAlbumId(albumTitle, artistName), // Generate consistent ID
            title = albumTitle,
            artist = artistName,
            year = audioInfo.year,
            genre = audioInfo.genre,
            artworkPath = null, // Will be set during artwork extraction
            hasHiResContent = metadataExtractor.isHighResFormat(audioInfo),
            trackCount = 0, // Will be updated during indexing
            totalDuration = 0L, // Will be updated during indexing
            playCount = 0,
            averageSampleRate = audioInfo.sampleRate
        )
    }

    /**
     * Calculate average sample rate for album
     */
    private fun calculateAverageSampleRate(
        currentAverage: Int?,
        currentTrackCount: Int,
        newSampleRate: Int?
    ): Int? {
        if (newSampleRate == null) return currentAverage
        if (currentAverage == null) return newSampleRate
        
        return ((currentAverage * currentTrackCount) + newSampleRate) / (currentTrackCount + 1)
    }

    /**
     * Batch insert or update artists
     */
    private suspend fun insertOrUpdateArtists(artists: List<Artist>) {
        try {
            artists.forEach { artist ->
                val existing = libraryDao.getArtistByName(artist.name)
                if (existing != null) {
                    // Update existing artist
                    val updated = existing.copy(
                        hasHiResContent = existing.hasHiResContent || artist.hasHiResContent,
                        trackCount = artist.trackCount,
                        playCount = existing.playCount,
                        lastPlayed = existing.lastPlayed
                    )
                    libraryDao.updateArtist(updated)
                } else {
                    // Insert new artist
                    libraryDao.insertArtist(artist)
                }
            }
            Timber.d("Updated ${artists.size} artists in database")
        } catch (e: Exception) {
            Timber.e(e, "Failed to insert/update artists")
            throw e
        }
    }

    /**
     * Batch insert or update albums
     */
    private suspend fun insertOrUpdateAlbums(albums: List<Album>) {
        try {
            albums.forEach { album ->
                val existing = libraryDao.getAlbumByTitleAndArtist(album.title, album.artist)
                if (existing != null) {
                    // Update existing album
                    val updated = existing.copy(
                        hasHiResContent = existing.hasHiResContent || album.hasHiResContent,
                        trackCount = album.trackCount,
                        totalDuration = album.totalDuration,
                        playCount = existing.playCount,
                        averageSampleRate = album.averageSampleRate,
                        artworkPath = existing.artworkPath ?: album.artworkPath
                    )
                    libraryDao.updateAlbum(updated)
                } else {
                    // Insert new album
                    libraryDao.insertAlbum(album)
                }
            }
            Timber.d("Updated ${albums.size} albums in database")
        } catch (e: Exception) {
            Timber.e(e, "Failed to insert/update albums")
            throw e
        }
    }

    /**
     * Batch insert or update tracks
     */
    private suspend fun insertOrUpdateTracks(tracks: List<Track>) {
        try {
            tracks.forEach { track ->
                val existing = trackDao.getTrackByPath(track.filePath)
                if (existing != null) {
                    // Update existing track (preserve play count and favorites)
                    val updated = track.copy(
                        id = existing.id,
                        playCount = existing.playCount,
                        isFavorite = existing.isFavorite,
                        lastPlayed = existing.lastPlayed,
                        eqPreset = existing.eqPreset,
                        addedAt = existing.addedAt
                    )
                    trackDao.updateTrack(updated)
                } else {
                    // Insert new track
                    trackDao.insertTrack(track)
                }
            }
            Timber.d("Updated ${tracks.size} tracks in database")
        } catch (e: Exception) {
            Timber.e(e, "Failed to insert/update tracks")
            throw e
        }
    }

    /**
     * Remove tracks from database that no longer exist on disk
     */
    suspend fun removeDeletedTracks(): Int {
        return withContext(Dispatchers.IO) {
            try {
                val batchSize = 500
                var offset = 0
                var deletedCount = 0
                var batch: List<Track>

                do {
                    batch = trackDao.getTracksBatch(offset, batchSize)
                    if (batch.isEmpty()) break

                    batch.forEach { track ->
                        val file = File(track.filePath)
                        if (!file.exists()) {
                            trackDao.deleteTrack(track)
                            deletedCount++
                            Timber.d("Removed deleted track: ${track.filePath}")
                        }
                    }

                    offset += batchSize
                } while (batch.size == batchSize)
                // Clean up orphaned artists and albums
                cleanupOrphanedEntries()

                Timber.i("Removed $deletedCount deleted tracks from database")
                deletedCount
            } catch (e: Exception) {
                Timber.e(e, "Failed to remove deleted tracks")
                0
            }
        }
    }

    /**
     * Remove artists and albums that no longer have any tracks
     */
    private suspend fun cleanupOrphanedEntries() {
        try {
            // Remove artists with no tracks
            val orphanedArtists = libraryDao.getArtistsWithNoTracks()
            orphanedArtists.forEach { artist ->
                libraryDao.deleteArtist(artist)
                Timber.d("Removed orphaned artist: ${artist.name}")
            }

            // Remove albums with no tracks
            val orphanedAlbums = libraryDao.getAlbumsWithNoTracks()
            orphanedAlbums.forEach { album ->
                libraryDao.deleteAlbum(album)
                Timber.d("Removed orphaned album: ${album.title} by ${album.artist}")
            }

            Timber.d("Cleaned up ${orphanedArtists.size} orphaned artists and ${orphanedAlbums.size} orphaned albums")
        } catch (e: Exception) {
            Timber.e(e, "Failed to cleanup orphaned entries")
        }
    }

    /**
     * Check for tracks that have been modified since last scan
     */
    suspend fun findModifiedTracks(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val batchSize = 500
                var offset = 0
                val modifiedPaths = mutableListOf<String>()
                var batch: List<Track>
                do {
                    batch = trackDao.getTracksBatch(offset, batchSize)
                    batch.forEach { track ->
                        val file = File(track.filePath)
                        if (file.exists() && file.lastModified() > track.addedAt) {
                            modifiedPaths.add(track.filePath)
                        }
                    }
                    offset += batchSize
                } while (batch.isNotEmpty())

                Timber.d("Found ${modifiedPaths.size} modified tracks")
                modifiedPaths
            } catch (e: Exception) {
                Timber.e(e, "Failed to find modified tracks")
                emptyList()
            }
        }
    }

    // Helper methods

    private fun extractTitleFromFilename(filePath: String): String {
        val filename = File(filePath).nameWithoutExtension
        return filename
            .replace(Regex("^\\d+[\\s\\-\\.]*"), "") // Remove leading track numbers
            .replace(Regex("\\[.*?\\]"), "") // Remove brackets
            .replace(Regex("\\(.*?\\)"), "") // Remove parentheses
            .replace("_", " ")
            .trim()
            .takeIf { it.isNotBlank() } ?: filename
    }

    private fun getMimeTypeFromFormat(format: String): String {
        return when (format.uppercase()) {
            "MP3" -> "audio/mpeg"
            "FLAC" -> "audio/flac"
            "WAV" -> "audio/wav"
            "AAC", "M4A" -> "audio/aac"
            "OGG" -> "audio/ogg"
            "WMA" -> "audio/x-ms-wma"
            "APE" -> "audio/x-ape"
            "AIFF" -> "audio/aiff"
            "ALAC" -> "audio/alac"
            "DSD", "DSF", "DFF" -> "audio/dsd"
            "OPUS" -> "audio/opus"
            else -> "audio/*"
        }
    }

    // Helper methods for ID generation
    private fun generateTrackId(filePath: String): String {
        // Use file path hash as consistent ID
        return "track_" + filePath.hashCode().toString().replace("-", "0")
    }

    private fun generateArtistId(artistName: String): String {
        return "artist_" + artistName.hashCode().toString().replace("-", "0") 
    }

    private fun generateAlbumId(albumTitle: String, artistName: String): String {
        val key = "$albumTitle|$artistName"
        return "album_" + key.hashCode().toString().replace("-", "0")
    }
}