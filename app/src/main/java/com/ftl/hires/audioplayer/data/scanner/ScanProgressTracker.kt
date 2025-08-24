package com.ftl.hires.audioplayer.data.scanner

import com.ftl.hires.audioplayer.data.repository.MediaScannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanProgressTracker @Inject constructor() {

    private val mutex = Mutex()
    
    private val _scanProgress = MutableStateFlow(
        MediaScannerRepository.ScanProgress(
            currentFile = "",
            filesScanned = 0,
            totalFiles = 0,
            isComplete = false
        )
    )
    val scanProgress: StateFlow<MediaScannerRepository.ScanProgress> = _scanProgress.asStateFlow()

    private val _scanState = MutableStateFlow(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private var startTime: Long = 0
    private var filesPerSecond: Double = 0.0

    enum class ScanState {
        Idle,
        Initializing,
        Scanning,
        Processing,
        Complete,
        Error,
        Cancelled
    }

    data class ScanStatistics(
        val filesScanned: Int,
        val totalFiles: Int,
        val timeElapsedMs: Long,
        val filesPerSecond: Double,
        val estimatedTimeRemainingMs: Long,
        val hiResFilesFound: Int,
        val errorCount: Int,
        val bytesProcessed: Long
    )

    // Additional statistics tracking
    private var hiResFilesFound = 0
    private var errorCount = 0
    private var bytesProcessed = 0L
    private var lastUpdateTime = 0L

    /**
     * Start a new scan session
     */
    suspend fun startScan(totalFiles: Int) {
        mutex.withLock {
            startTime = System.currentTimeMillis()
            lastUpdateTime = startTime
            hiResFilesFound = 0
            errorCount = 0
            bytesProcessed = 0L
            filesPerSecond = 0.0

            _scanState.value = ScanState.Initializing
            _scanProgress.value = MediaScannerRepository.ScanProgress(
                currentFile = "Initializing scan...",
                filesScanned = 0,
                totalFiles = totalFiles,
                isComplete = false
            )
            
            Timber.d("Started scan session for $totalFiles files")
        }
    }

    /**
     * Update scan progress with current file being processed
     */
    suspend fun updateProgress(
        currentFile: String,
        filesScanned: Int,
        isHiRes: Boolean = false,
        fileSize: Long = 0L
    ) {
        mutex.withLock {
            val currentTime = System.currentTimeMillis()
            val currentProgress = _scanProgress.value

            // Update statistics
            if (isHiRes) hiResFilesFound++
            bytesProcessed += fileSize

            // Calculate scanning speed
            if (filesScanned > 0 && currentTime > startTime) {
                filesPerSecond = filesScanned.toDouble() / ((currentTime - startTime) / 1000.0)
            }

            _scanState.value = ScanState.Scanning
            _scanProgress.value = currentProgress.copy(
                currentFile = extractFilenameFromPath(currentFile),
                filesScanned = filesScanned
            )

            // Log progress at intervals
            if (currentTime - lastUpdateTime > 5000) { // Every 5 seconds
                logProgress()
                lastUpdateTime = currentTime
            }
        }
    }

    /**
     * Report an error during scanning
     */
    suspend fun reportError(errorMessage: String, currentFile: String? = null) {
        mutex.withLock {
            errorCount++
            val currentProgress = _scanProgress.value

            _scanProgress.value = currentProgress.copy(
                error = errorMessage,
                currentFile = currentFile?.let { extractFilenameFromPath(it) } ?: currentProgress.currentFile
            )

            Timber.w("Scan error (#$errorCount): $errorMessage for file: $currentFile")
        }
    }

    /**
     * Set scan state to processing (indexing database)
     */
    suspend fun startProcessing() {
        mutex.withLock {
            _scanState.value = ScanState.Processing
            val currentProgress = _scanProgress.value
            
            _scanProgress.value = currentProgress.copy(
                currentFile = "Processing and indexing files...",
                error = null
            )
            
            Timber.d("Started processing phase")
        }
    }

    /**
     * Complete the scan session
     */
    suspend fun completeScan(successCount: Int? = null) {
        mutex.withLock {
            val currentProgress = _scanProgress.value
            val finalFilesScanned = successCount ?: currentProgress.filesScanned
            
            _scanState.value = ScanState.Complete
            _scanProgress.value = currentProgress.copy(
                filesScanned = finalFilesScanned,
                isComplete = true,
                error = null,
                currentFile = "Scan completed successfully"
            )

            logFinalStatistics(finalFilesScanned)
            Timber.i("Scan completed: $finalFilesScanned files processed")
        }
    }

    /**
     * Cancel the scan session
     */
    suspend fun cancelScan() {
        mutex.withLock {
            _scanState.value = ScanState.Cancelled
            val currentProgress = _scanProgress.value
            
            _scanProgress.value = currentProgress.copy(
                currentFile = "Scan cancelled",
                error = "Scan was cancelled by user",
                isComplete = true
            )
            
            Timber.i("Scan cancelled after processing ${currentProgress.filesScanned} files")
        }
    }

    /**
     * Set scan state to error
     */
    suspend fun failScan(errorMessage: String) {
        mutex.withLock {
            _scanState.value = ScanState.Error
            val currentProgress = _scanProgress.value
            
            _scanProgress.value = currentProgress.copy(
                error = errorMessage,
                isComplete = true,
                currentFile = "Scan failed"
            )
            
            Timber.e("Scan failed: $errorMessage")
        }
    }

    /**
     * Reset tracker to idle state
     */
    suspend fun reset() {
        mutex.withLock {
            _scanState.value = ScanState.Idle
            _scanProgress.value = MediaScannerRepository.ScanProgress(
                currentFile = "",
                filesScanned = 0,
                totalFiles = 0,
                isComplete = false
            )
            
            // Reset statistics
            startTime = 0
            hiResFilesFound = 0
            errorCount = 0
            bytesProcessed = 0L
            filesPerSecond = 0.0
            lastUpdateTime = 0L
            
            Timber.d("Progress tracker reset")
        }
    }

    /**
     * Get current scan statistics
     */
    fun getScanStatistics(): ScanStatistics {
        val currentProgress = _scanProgress.value
        val currentTime = System.currentTimeMillis()
        val timeElapsed = if (startTime > 0) currentTime - startTime else 0L
        
        val estimatedTimeRemaining = if (filesPerSecond > 0 && currentProgress.totalFiles > 0) {
            val remainingFiles = currentProgress.totalFiles - currentProgress.filesScanned
            (remainingFiles / filesPerSecond * 1000).toLong()
        } else 0L

        return ScanStatistics(
            filesScanned = currentProgress.filesScanned,
            totalFiles = currentProgress.totalFiles,
            timeElapsedMs = timeElapsed,
            filesPerSecond = filesPerSecond,
            estimatedTimeRemainingMs = estimatedTimeRemaining,
            hiResFilesFound = hiResFilesFound,
            errorCount = errorCount,
            bytesProcessed = bytesProcessed
        )
    }

    /**
     * Get current progress percentage (0-100)
     */
    fun getProgressPercentage(): Int {
        val currentProgress = _scanProgress.value
        return if (currentProgress.totalFiles > 0) {
            ((currentProgress.filesScanned.toFloat() / currentProgress.totalFiles) * 100).toInt()
        } else 0
    }

    /**
     * Check if scan is currently active
     */
    fun isScanning(): Boolean {
        return _scanState.value in listOf(
            ScanState.Initializing, 
            ScanState.Scanning, 
            ScanState.Processing
        )
    }

    // Helper methods

    private fun extractFilenameFromPath(filePath: String): String {
        return filePath.substringAfterLast("/").substringAfterLast("\\")
    }

    private fun logProgress() {
        val stats = getScanStatistics()
        val percentage = getProgressPercentage()
        
        Timber.i(
            "Scan progress: $percentage% (${stats.filesScanned}/${stats.totalFiles}) | " +
            "Speed: ${String.format("%.1f", stats.filesPerSecond)} files/sec | " +
            "Hi-res found: ${stats.hiResFilesFound} | " +
            "Errors: ${stats.errorCount}"
        )
    }

    private fun logFinalStatistics(finalFilesScanned: Int) {
        val totalTime = System.currentTimeMillis() - startTime
        val avgSpeed = if (totalTime > 0) {
            finalFilesScanned.toDouble() / (totalTime / 1000.0)
        } else 0.0

        Timber.i(
            "Scan completed in ${totalTime / 1000}s | " +
            "Files processed: $finalFilesScanned | " +
            "Hi-res files: $hiResFilesFound | " +
            "Average speed: ${String.format("%.1f", avgSpeed)} files/sec | " +
            "Errors: $errorCount | " +
            "Data processed: ${formatBytes(bytesProcessed)}"
        )
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> String.format("%.1f GB", bytes / 1_000_000_000.0)
            bytes >= 1_000_000 -> String.format("%.1f MB", bytes / 1_000_000.0)
            bytes >= 1_000 -> String.format("%.1f KB", bytes / 1_000.0)
            else -> "$bytes bytes"
        }
    }
}