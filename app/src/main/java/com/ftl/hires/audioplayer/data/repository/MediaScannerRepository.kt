package com.ftl.hires.audioplayer.data.repository

import com.ftl.hires.audioplayer.data.database.entities.Track
import com.ftl.hires.audioplayer.data.database.entities.Album
import com.ftl.hires.audioplayer.data.database.entities.Artist
import kotlinx.coroutines.flow.Flow

interface MediaScannerRepository {
    
    data class ScanProgress(
        val currentFile: String,
        val filesScanned: Int,
        val totalFiles: Int,
        val isComplete: Boolean,
        val error: String? = null
    )
    
    data class AudioFileInfo(
        val filePath: String,
        val title: String?,
        val artist: String?,
        val album: String?,
        val duration: Long,
        val fileSize: Long,
        val format: String,
        val bitrate: Int?,
        val sampleRate: Int?,
        val bitDepth: Int?,
        val channels: Int?,
        val trackNumber: Int?,
        val discNumber: Int?,
        val year: Int?,
        val genre: String?,
        val artworkPath: String?
    )
    
    // Scanning operations
    suspend fun scanMusicLibrary(): Flow<ScanProgress>
    suspend fun scanSpecificFolder(folderPath: String): Flow<ScanProgress>
    suspend fun scanFile(filePath: String): AudioFileInfo?
    suspend fun quickScan(): Flow<ScanProgress> // Only new/modified files
    
    // File operations
    fun isAudioFile(filePath: String): Boolean
    suspend fun extractMetadata(filePath: String): AudioFileInfo?
    suspend fun extractArtwork(filePath: String, outputPath: String): Boolean
    
    // Library management
    suspend fun updateLibraryFromScan(scanResults: List<AudioFileInfo>)
    suspend fun removeDeletedFiles()
    suspend fun checkForModifiedFiles(): List<String> // Returns list of modified file paths
    
    // Format support
    fun getSupportedFormats(): List<String>
    fun isHighResFormat(audioInfo: AudioFileInfo): Boolean
    
    // Preferences
    suspend fun getScanDirectories(): List<String>
    suspend fun setScanDirectories(directories: List<String>)
    suspend fun addScanDirectory(directory: String)
    suspend fun removeScanDirectory(directory: String)
    
    // Auto-scan settings
    suspend fun isAutoScanEnabled(): Boolean
    suspend fun setAutoScanEnabled(enabled: Boolean)
    suspend fun getAutoScanInterval(): Long // in milliseconds
    suspend fun setAutoScanInterval(intervalMs: Long)
}