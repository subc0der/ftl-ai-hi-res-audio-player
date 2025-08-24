package com.ftl.hires.audioplayer.data.repository

import android.content.Context
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ftl.hires.audioplayer.data.database.dao.TrackDao
import com.ftl.hires.audioplayer.data.database.dao.LibraryDao
import com.ftl.hires.audioplayer.data.scanner.MetadataExtractor
import com.ftl.hires.audioplayer.data.scanner.ArtworkExtractor
import com.ftl.hires.audioplayer.data.scanner.LibraryIndexer
import com.ftl.hires.audioplayer.data.scanner.ScanProgressTracker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "media_scanner_prefs")

@Singleton
class MediaScannerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
    private val libraryDao: LibraryDao,
    private val metadataExtractor: MetadataExtractor,
    private val artworkExtractor: ArtworkExtractor,
    private val libraryIndexer: LibraryIndexer,
    private val scanProgressTracker: ScanProgressTracker
) : MediaScannerRepository {
    
    companion object {
        private val SCAN_DIRECTORIES_KEY = stringSetPreferencesKey("scan_directories")
        private val AUTO_SCAN_ENABLED_KEY = booleanPreferencesKey("auto_scan_enabled")
        private val AUTO_SCAN_INTERVAL_KEY = longPreferencesKey("auto_scan_interval")
        
        private const val DEFAULT_AUTO_SCAN_INTERVAL = 3600000L // 1 hour
    }
    
    override suspend fun scanMusicLibrary(): Flow<MediaScannerRepository.ScanProgress> = flow {
        try {
            val musicDirs = getScanDirectories()
            val allFiles = mutableListOf<File>()
            
            Timber.i("Starting full library scan in directories: $musicDirs")
            
            // Collect all audio files
            musicDirs.forEach { dir ->
                val directory = File(dir)
                if (directory.exists() && directory.isDirectory) {
                    collectAudioFiles(directory, allFiles)
                }
            }
            
            val totalFiles = allFiles.size
            Timber.i("Found $totalFiles audio files to scan")
            
            // Initialize progress tracker
            scanProgressTracker.startScan(totalFiles)
            
            val scanResults = mutableListOf<MediaScannerRepository.AudioFileInfo>()
            
            allFiles.forEachIndexed { index, file ->
                // Update progress tracker
                scanProgressTracker.updateProgress(
                    currentFile = file.absolutePath,
                    filesScanned = index,
                    fileSize = file.length()
                )
                
                // Emit current progress
                emit(scanProgressTracker.scanProgress.value)
                
                try {
                    extractMetadata(file.absolutePath)?.let { audioInfo ->
                        scanResults.add(audioInfo)
                        
                        // Update progress with hi-res detection
                        scanProgressTracker.updateProgress(
                            currentFile = file.absolutePath,
                            filesScanned = index + 1,
                            isHiRes = metadataExtractor.isHighResFormat(audioInfo),
                            fileSize = file.length()
                        )
                    }
                } catch (e: Exception) {
                    scanProgressTracker.reportError(
                        errorMessage = "Failed to process ${file.name}: ${e.message}",
                        currentFile = file.absolutePath
                    )
                }
            }
            
            // Start processing phase
            scanProgressTracker.startProcessing()
            emit(scanProgressTracker.scanProgress.value)
            
            // Update database with scan results
            updateLibraryFromScan(scanResults)
            
            // Complete scan
            scanProgressTracker.completeScan(scanResults.size)
            emit(scanProgressTracker.scanProgress.value)
            
            Timber.i("Library scan completed successfully")
            
        } catch (e: Exception) {
            Timber.e(e, "Library scan failed")
            scanProgressTracker.failScan("Scan failed: ${e.localizedMessage}")
            emit(scanProgressTracker.scanProgress.value)
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun scanSpecificFolder(folderPath: String): Flow<MediaScannerRepository.ScanProgress> = flow {
        try {
            val directory = File(folderPath)
            if (!directory.exists() || !directory.isDirectory) {
                scanProgressTracker.failScan("Directory does not exist: $folderPath")
                emit(scanProgressTracker.scanProgress.value)
                return@flow
            }
            
            Timber.i("Starting folder scan: $folderPath")
            
            val files = mutableListOf<File>()
            collectAudioFiles(directory, files)
            
            val totalFiles = files.size
            Timber.i("Found $totalFiles audio files in folder")
            
            // Initialize progress tracker
            scanProgressTracker.startScan(totalFiles)
            
            val scanResults = mutableListOf<MediaScannerRepository.AudioFileInfo>()
            
            files.forEachIndexed { index, file ->
                scanProgressTracker.updateProgress(
                    currentFile = file.absolutePath,
                    filesScanned = index,
                    fileSize = file.length()
                )
                
                emit(scanProgressTracker.scanProgress.value)
                
                try {
                    extractMetadata(file.absolutePath)?.let { audioInfo ->
                        scanResults.add(audioInfo)
                        
                        scanProgressTracker.updateProgress(
                            currentFile = file.absolutePath,
                            filesScanned = index + 1,
                            isHiRes = metadataExtractor.isHighResFormat(audioInfo),
                            fileSize = file.length()
                        )
                    }
                } catch (e: Exception) {
                    scanProgressTracker.reportError(
                        errorMessage = "Failed to process ${file.name}: ${e.message}",
                        currentFile = file.absolutePath
                    )
                }
            }
            
            // Start processing phase
            scanProgressTracker.startProcessing()
            emit(scanProgressTracker.scanProgress.value)
            
            updateLibraryFromScan(scanResults)
            
            scanProgressTracker.completeScan(scanResults.size)
            emit(scanProgressTracker.scanProgress.value)
            
            Timber.i("Folder scan completed successfully")
            
        } catch (e: Exception) {
            Timber.e(e, "Folder scan failed")
            scanProgressTracker.failScan("Folder scan failed: ${e.localizedMessage}")
            emit(scanProgressTracker.scanProgress.value)
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun scanFile(filePath: String): MediaScannerRepository.AudioFileInfo? {
        return withContext(Dispatchers.IO) {
            try {
                metadataExtractor.extractMetadata(filePath)
            } catch (e: Exception) {
                Timber.e(e, "Failed to scan file: $filePath")
                null
            }
        }
    }
    
    override suspend fun quickScan(): Flow<MediaScannerRepository.ScanProgress> = flow {
        try {
            Timber.i("Starting quick scan")
            
            // First remove deleted files
            val deletedCount = libraryIndexer.removeDeletedTracks()
            
            // Find modified files
            val modifiedFiles = libraryIndexer.findModifiedTracks()
            
            val totalFiles = modifiedFiles.size
            Timber.i("Quick scan: $deletedCount deleted files removed, $totalFiles modified files to process")
            
            if (totalFiles == 0) {
                emit(MediaScannerRepository.ScanProgress(
                    currentFile = "No changes detected",
                    filesScanned = 0,
                    totalFiles = 0,
                    isComplete = true
                ))
                return@flow
            }
            
            scanProgressTracker.startScan(totalFiles)
            
            val scanResults = mutableListOf<MediaScannerRepository.AudioFileInfo>()
            
            modifiedFiles.forEachIndexed { index, filePath ->
                scanProgressTracker.updateProgress(
                    currentFile = filePath,
                    filesScanned = index
                )
                
                emit(scanProgressTracker.scanProgress.value)
                
                try {
                    extractMetadata(filePath)?.let { audioInfo ->
                        scanResults.add(audioInfo)
                        
                        scanProgressTracker.updateProgress(
                            currentFile = filePath,
                            filesScanned = index + 1,
                            isHiRes = metadataExtractor.isHighResFormat(audioInfo)
                        )
                    }
                } catch (e: Exception) {
                    scanProgressTracker.reportError(
                        errorMessage = "Failed to process modified file: ${e.message}",
                        currentFile = filePath
                    )
                }
            }
            
            scanProgressTracker.startProcessing()
            emit(scanProgressTracker.scanProgress.value)
            
            updateLibraryFromScan(scanResults)
            
            scanProgressTracker.completeScan(scanResults.size)
            emit(scanProgressTracker.scanProgress.value)
            
            Timber.i("Quick scan completed successfully")
            
        } catch (e: Exception) {
            Timber.e(e, "Quick scan failed")
            scanProgressTracker.failScan("Quick scan failed: ${e.localizedMessage}")
            emit(scanProgressTracker.scanProgress.value)
        }
    }.flowOn(Dispatchers.IO)
    
    override fun isAudioFile(filePath: String): Boolean {
        return metadataExtractor.isAudioFile(filePath)
    }
    
    override suspend fun extractMetadata(filePath: String): MediaScannerRepository.AudioFileInfo? {
        return withContext(Dispatchers.IO) {
            metadataExtractor.extractMetadata(filePath)
        }
    }
    
    override suspend fun extractArtwork(filePath: String, outputPath: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val artworkPath = artworkExtractor.extractArtwork(filePath)
                if (artworkPath != null) {
                    // Copy to desired output path if different
                    if (artworkPath != outputPath) {
                        File(artworkPath).copyTo(File(outputPath), overwrite = true)
                    }
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to extract artwork to $outputPath")
                false
            }
        }
    }
    
    override suspend fun updateLibraryFromScan(scanResults: List<MediaScannerRepository.AudioFileInfo>) {
        withContext(Dispatchers.IO) {
            libraryIndexer.indexAudioFiles(scanResults)
        }
    }
    
    override suspend fun removeDeletedFiles() {
        withContext(Dispatchers.IO) {
            libraryIndexer.removeDeletedTracks()
        }
    }
    
    override suspend fun checkForModifiedFiles(): List<String> {
        return withContext(Dispatchers.IO) {
            libraryIndexer.findModifiedTracks()
        }
    }
    
    override fun getSupportedFormats(): List<String> = metadataExtractor.getSupportedFormats()
    
    override fun isHighResFormat(audioInfo: MediaScannerRepository.AudioFileInfo): Boolean {
        return metadataExtractor.isHighResFormat(audioInfo)
    }
    
    override suspend fun getScanDirectories(): List<String> {
        return context.dataStore.data.map { preferences ->
            preferences[SCAN_DIRECTORIES_KEY] ?: getDefaultScanDirectories()
        }.first().toList()
    }
    
    override suspend fun setScanDirectories(directories: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[SCAN_DIRECTORIES_KEY] = directories.toSet()
        }
    }
    
    override suspend fun addScanDirectory(directory: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[SCAN_DIRECTORIES_KEY] ?: emptySet()
            preferences[SCAN_DIRECTORIES_KEY] = current + directory
        }
    }
    
    override suspend fun removeScanDirectory(directory: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[SCAN_DIRECTORIES_KEY] ?: emptySet()
            preferences[SCAN_DIRECTORIES_KEY] = current - directory
        }
    }
    
    override suspend fun isAutoScanEnabled(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[AUTO_SCAN_ENABLED_KEY] ?: false
        }.first()
    }
    
    override suspend fun setAutoScanEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SCAN_ENABLED_KEY] = enabled
        }
    }
    
    override suspend fun getAutoScanInterval(): Long {
        return context.dataStore.data.map { preferences ->
            preferences[AUTO_SCAN_INTERVAL_KEY] ?: DEFAULT_AUTO_SCAN_INTERVAL
        }.first()
    }
    
    override suspend fun setAutoScanInterval(intervalMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SCAN_INTERVAL_KEY] = intervalMs
        }
    }
    
    // Helper methods
    private fun collectAudioFiles(directory: File, fileList: MutableList<File>) {
        try {
            directory.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    collectAudioFiles(file, fileList)
                } else if (isAudioFile(file.absolutePath)) {
                    fileList.add(file)
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to list files in directory: ${directory.absolutePath}")
        }
    }
    
    private fun getDefaultScanDirectories(): Set<String> {
        val directories = mutableSetOf<String>()
        
        // Add external storage music directory
        val externalMusic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        if (externalMusic?.exists() == true) {
            directories.add(externalMusic.absolutePath)
        }
        
        // Add downloads directory
        val externalDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (externalDownloads?.exists() == true) {
            directories.add(externalDownloads.absolutePath)
        }
        
        // Add common music directories
        val commonDirectories = listOf(
            "/storage/emulated/0/Music",
            "/storage/emulated/0/Download",
            "/sdcard/Music",
            "/sdcard/Download"
        )
        
        commonDirectories.forEach { dir ->
            val file = File(dir)
            if (file.exists() && file.isDirectory) {
                directories.add(dir)
            }
        }
        
        return directories
    }
}