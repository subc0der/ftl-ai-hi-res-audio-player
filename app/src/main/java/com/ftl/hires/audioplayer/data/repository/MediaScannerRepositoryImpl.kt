package com.ftl.hires.audioplayer.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import com.ftl.hires.audioplayer.data.database.dao.TrackDao
import com.ftl.hires.audioplayer.data.database.dao.LibraryDao
import com.ftl.hires.audioplayer.data.database.entities.Track
import com.ftl.hires.audioplayer.data.database.entities.Album
import com.ftl.hires.audioplayer.data.database.entities.Artist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.*
import javax.inject.Inject

class MediaScannerRepositoryImpl @Inject constructor(
    private val context: Context,
    private val trackDao: TrackDao,
    private val libraryDao: LibraryDao
) : MediaScannerRepository {
    
    private val supportedFormats = listOf(
        "mp3", "flac", "wav", "ogg", "m4a", "aac", "wma", "ape", "dsd", "dsf", "dff"
    )
    
    private val highResFormats = listOf("flac", "wav", "ape", "dsd", "dsf", "dff")
    private val highResSampleRates = listOf(88200, 96000, 176400, 192000, 352800, 384000)
    private val highResBitDepths = listOf(24, 32)
    
    override suspend fun scanMusicLibrary(): Flow<MediaScannerRepository.ScanProgress> = flow {
        try {
            val musicDirs = getScanDirectories()
            val allFiles = mutableListOf<File>()
            
            // Collect all audio files
            musicDirs.forEach { dir ->
                val directory = File(dir)
                if (directory.exists() && directory.isDirectory) {
                    collectAudioFiles(directory, allFiles)
                }
            }
            
            val totalFiles = allFiles.size
            val scanResults = mutableListOf<MediaScannerRepository.AudioFileInfo>()
            
            allFiles.forEachIndexed { index, file ->
                emit(MediaScannerRepository.ScanProgress(
                    currentFile = file.name,
                    filesScanned = index,
                    totalFiles = totalFiles,
                    isComplete = false
                ))
                
                extractMetadata(file.absolutePath)?.let { audioInfo ->
                    scanResults.add(audioInfo)
                }
            }
            
            // Update database with scan results
            updateLibraryFromScan(scanResults)
            
            emit(MediaScannerRepository.ScanProgress(
                currentFile = "",
                filesScanned = totalFiles,
                totalFiles = totalFiles,
                isComplete = true
            ))
            
        } catch (e: Exception) {
            emit(MediaScannerRepository.ScanProgress(
                currentFile = "",
                filesScanned = 0,
                totalFiles = 0,
                isComplete = false,
                error = e.message
            ))
        }
    }
    
    override suspend fun scanSpecificFolder(folderPath: String): Flow<MediaScannerRepository.ScanProgress> = flow {
        try {
            val directory = File(folderPath)
            if (!directory.exists() || !directory.isDirectory) {
                emit(MediaScannerRepository.ScanProgress(
                    currentFile = "",
                    filesScanned = 0,
                    totalFiles = 0,
                    isComplete = false,
                    error = "Directory does not exist: $folderPath"
                ))
                return@flow
            }
            
            val files = mutableListOf<File>()
            collectAudioFiles(directory, files)
            
            val totalFiles = files.size
            val scanResults = mutableListOf<MediaScannerRepository.AudioFileInfo>()
            
            files.forEachIndexed { index, file ->
                emit(MediaScannerRepository.ScanProgress(
                    currentFile = file.name,
                    filesScanned = index,
                    totalFiles = totalFiles,
                    isComplete = false
                ))
                
                extractMetadata(file.absolutePath)?.let { audioInfo ->
                    scanResults.add(audioInfo)
                }
            }
            
            updateLibraryFromScan(scanResults)
            
            emit(MediaScannerRepository.ScanProgress(
                currentFile = "",
                filesScanned = totalFiles,
                totalFiles = totalFiles,
                isComplete = true
            ))
            
        } catch (e: Exception) {
            emit(MediaScannerRepository.ScanProgress(
                currentFile = "",
                filesScanned = 0,
                totalFiles = 0,
                isComplete = false,
                error = e.message
            ))
        }
    }
    
    override suspend fun scanFile(filePath: String): MediaScannerRepository.AudioFileInfo? {
        return extractMetadata(filePath)
    }
    
    override suspend fun quickScan(): Flow<MediaScannerRepository.ScanProgress> = flow {
        // Quick scan implementation - only scan new/modified files
        val modifiedFiles = checkForModifiedFiles()
        // Implementation would be similar to scanMusicLibrary but only for modified files
        emit(MediaScannerRepository.ScanProgress(
            currentFile = "",
            filesScanned = 0,
            totalFiles = 0,
            isComplete = true
        ))
    }
    
    override fun isAudioFile(filePath: String): Boolean {
        val extension = File(filePath).extension.lowercase()
        return supportedFormats.contains(extension)
    }
    
    override suspend fun extractMetadata(filePath: String): MediaScannerRepository.AudioFileInfo? {
        if (!isAudioFile(filePath)) return null
        
        val file = File(filePath)
        if (!file.exists()) return null
        
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: file.nameWithoutExtension
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()
            val trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull()
            val discNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)?.toIntOrNull()
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull()
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            
            // For sample rate and bit depth, we'd need additional metadata extraction
            // This is a simplified version - actual implementation would need more robust metadata extraction
            val sampleRate = extractSampleRate(filePath)
            val bitDepth = extractBitDepth(filePath)
            val channels = extractChannels(filePath)
            
            MediaScannerRepository.AudioFileInfo(
                filePath = filePath,
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                fileSize = file.length(),
                format = file.extension.lowercase(),
                bitrate = bitrate,
                sampleRate = sampleRate,
                bitDepth = bitDepth,
                channels = channels,
                trackNumber = trackNumber,
                discNumber = discNumber,
                year = year,
                genre = genre,
                artworkPath = null // Would extract album art separately
            )
            
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
    }
    
    override suspend fun extractArtwork(filePath: String, outputPath: String): Boolean {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            val artwork = retriever.embeddedPicture
            if (artwork != null) {
                File(outputPath).writeBytes(artwork)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
    }
    
    override suspend fun updateLibraryFromScan(scanResults: List<MediaScannerRepository.AudioFileInfo>) {
        val tracks = mutableListOf<Track>()
        val artists = mutableSetOf<Artist>()
        val albums = mutableSetOf<Album>()
        
        scanResults.forEach { audioInfo ->
            val trackId = UUID.randomUUID().toString()
            val artistId = audioInfo.artist?.let { UUID.randomUUID().toString() }
            val albumId = audioInfo.album?.let { UUID.randomUUID().toString() }
            
            // Create track
            val track = Track(
                id = trackId,
                title = audioInfo.title ?: "Unknown Title",
                artistId = artistId,
                artistName = audioInfo.artist,
                albumId = albumId,
                albumName = audioInfo.album,
                durationMs = audioInfo.duration,
                filePath = audioInfo.filePath,
                fileSize = audioInfo.fileSize,
                format = audioInfo.format,
                bitrate = audioInfo.bitrate,
                sampleRate = audioInfo.sampleRate,
                bitDepth = audioInfo.bitDepth,
                channels = audioInfo.channels,
                trackNumber = audioInfo.trackNumber,
                discNumber = audioInfo.discNumber,
                year = audioInfo.year,
                genre = audioInfo.genre,
                artworkPath = audioInfo.artworkPath,
                isHighRes = isHighResFormat(audioInfo)
            )
            tracks.add(track)
            
            // Create artist if not exists
            if (audioInfo.artist != null && artistId != null) {
                artists.add(Artist(
                    id = artistId,
                    name = audioInfo.artist
                ))
            }
            
            // Create album if not exists
            if (audioInfo.album != null && albumId != null) {
                albums.add(Album(
                    id = albumId,
                    title = audioInfo.album,
                    artistId = artistId,
                    artistName = audioInfo.artist,
                    year = audioInfo.year,
                    genre = audioInfo.genre,
                    isHighRes = isHighResFormat(audioInfo)
                ))
            }
        }
        
        // Insert into database
        libraryDao.insertArtists(artists.toList())
        libraryDao.insertAlbums(albums.toList())
        trackDao.insertTracks(tracks)
    }
    
    override suspend fun removeDeletedFiles() {
        // Implementation to remove tracks for deleted files
        // This would need to be implemented using first() to get current list
        // or by querying tracks without Flow wrapper
        // For now, leaving as placeholder
    }
    
    override suspend fun checkForModifiedFiles(): List<String> {
        // Implementation to check for files that have been modified since last scan
        return emptyList() // Placeholder
    }
    
    override fun getSupportedFormats(): List<String> = supportedFormats
    
    override fun isHighResFormat(audioInfo: MediaScannerRepository.AudioFileInfo): Boolean {
        return highResFormats.contains(audioInfo.format.lowercase()) ||
                (audioInfo.sampleRate?.let { highResSampleRates.contains(it) } == true) ||
                (audioInfo.bitDepth?.let { highResBitDepths.contains(it) } == true)
    }
    
    override suspend fun getScanDirectories(): List<String> {
        // Default music directories
        return listOf(
            "/storage/emulated/0/Music",
            "/storage/emulated/0/Download"
        )
    }
    
    override suspend fun setScanDirectories(directories: List<String>) {
        // Save to preferences - implementation needed
    }
    
    override suspend fun addScanDirectory(directory: String) {
        // Add to preferences - implementation needed
    }
    
    override suspend fun removeScanDirectory(directory: String) {
        // Remove from preferences - implementation needed
    }
    
    override suspend fun isAutoScanEnabled(): Boolean = false // Default value
    
    override suspend fun setAutoScanEnabled(enabled: Boolean) {
        // Save to preferences - implementation needed
    }
    
    override suspend fun getAutoScanInterval(): Long = 3600000L // 1 hour default
    
    override suspend fun setAutoScanInterval(intervalMs: Long) {
        // Save to preferences - implementation needed
    }
    
    // Helper methods
    private fun collectAudioFiles(directory: File, fileList: MutableList<File>) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                collectAudioFiles(file, fileList)
            } else if (isAudioFile(file.absolutePath)) {
                fileList.add(file)
            }
        }
    }
    
    private fun extractSampleRate(filePath: String): Int? {
        // Placeholder - would need proper audio format parsing
        return null
    }
    
    private fun extractBitDepth(filePath: String): Int? {
        // Placeholder - would need proper audio format parsing
        return null
    }
    
    private fun extractChannels(filePath: String): Int? {
        // Placeholder - would need proper audio format parsing
        return null
    }
}