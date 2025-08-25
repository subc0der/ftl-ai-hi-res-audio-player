package com.ftl.hires.audioplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ftl.hires.audioplayer.R
import com.ftl.hires.audioplayer.data.repository.MediaScannerRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MediaScannerService : Service() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "media_scanner_channel"
        private const val NOTIFICATION_ID = 2001
        private const val ACTION_START_SCAN = "action_start_scan"
        private const val ACTION_START_QUICK_SCAN = "action_start_quick_scan"
        private const val ACTION_STOP_SCAN = "action_stop_scan"
        private const val EXTRA_SCAN_FOLDER = "extra_scan_folder"

        fun startFullScan(context: Context) {
            val intent = Intent(context, MediaScannerService::class.java).apply {
                action = ACTION_START_SCAN
            }
            context.startForegroundService(intent)
        }

        fun startFolderScan(context: Context, folderPath: String) {
            val intent = Intent(context, MediaScannerService::class.java).apply {
                action = ACTION_START_SCAN
                putExtra(EXTRA_SCAN_FOLDER, folderPath)
            }
            context.startForegroundService(intent)
        }

        fun startQuickScan(context: Context) {
            val intent = Intent(context, MediaScannerService::class.java).apply {
                action = ACTION_START_QUICK_SCAN
            }
            context.startForegroundService(intent)
        }

        fun stopScan(context: Context) {
            val intent = Intent(context, MediaScannerService::class.java).apply {
                action = ACTION_STOP_SCAN
            }
            context.startService(intent)
        }
    }

    @Inject
    lateinit var mediaScannerRepository: MediaScannerRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentScanJob: Job? = null

    private val _scanState = MutableStateFlow(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _scanProgress = MutableStateFlow(
        MediaScannerRepository.ScanProgress(
            currentFile = "",
            filesScanned = 0,
            totalFiles = 0,
            isComplete = false
        )
    )
    val scanProgress: StateFlow<MediaScannerRepository.ScanProgress> = _scanProgress.asStateFlow()

    private val binder = MediaScannerBinder()
    private lateinit var notificationManager: NotificationManager

    enum class ScanState {
        Idle, Scanning, QuickScanning, Stopping, Complete, Error
    }

    inner class MediaScannerBinder : Binder() {
        fun getService(): MediaScannerService = this@MediaScannerService
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("MediaScannerService created")
        
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("MediaScannerService onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_SCAN -> {
                val folderPath = intent.getStringExtra(EXTRA_SCAN_FOLDER)
                startScanning(folderPath)
            }
            ACTION_START_QUICK_SCAN -> {
                startQuickScanning()
            }
            ACTION_STOP_SCAN -> {
                stopScanning()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("MediaScannerService destroyed")
        stopScanning()
        serviceScope.launch {
            // Cleanup any ongoing operations
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Music Library Scanner",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress while scanning music library"
                setShowBadge(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createScanningNotification(progress: MediaScannerRepository.ScanProgress): Notification {
        val progressPercentage = if (progress.totalFiles > 0) {
            ((progress.filesScanned.toFloat() / progress.totalFiles) * 100).toInt()
        } else 0

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Scanning Music Library")
            .setContentText(when {
                progress.isComplete -> "Scan complete - Found ${progress.filesScanned} files"
                progress.error != null -> "Scan error: ${progress.error}"
                else -> "Scanned ${progress.filesScanned} of ${progress.totalFiles} files ($progressPercentage%)"
            })
            .setSmallIcon(R.drawable.ic_music_note)
            .setProgress(progress.totalFiles, progress.filesScanned, progress.totalFiles == 0)
            .setOngoing(!progress.isComplete && progress.error == null)
            .setAutoCancel(progress.isComplete || progress.error != null)
            .addAction(
                R.drawable.ic_stop,
                "Stop",
                null // We'll add PendingIntent for stop action if needed
            )
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startScanning(folderPath: String? = null) {
        if (_scanState.value == ScanState.Scanning || _scanState.value == ScanState.QuickScanning) {
            Timber.w("Scan already in progress, ignoring new scan request")
            return
        }

        _scanState.value = ScanState.Scanning
        
        // Start foreground service
        val initialNotification = createScanningNotification(
            MediaScannerRepository.ScanProgress("", 0, 0, false)
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                initialNotification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        currentScanJob = serviceScope.launch {
            try {
                val scanFlow = if (folderPath != null) {
                    mediaScannerRepository.scanSpecificFolder(folderPath)
                } else {
                    mediaScannerRepository.scanMusicLibrary()
                }

                scanFlow.collect { progress ->
                    _scanProgress.value = progress
                    
                    // Update notification
                    val notification = createScanningNotification(progress)
                    notificationManager.notify(NOTIFICATION_ID, notification)

                    if (progress.isComplete) {
                        _scanState.value = ScanState.Complete
                        Timber.i("Music library scan completed. Scanned ${progress.filesScanned} files")
                        
                        serviceScope.launch {
                            kotlinx.coroutines.delay(3000) // Show completion for 3 seconds
                            stopForegroundService()
                        }
                        return@collect
                    }

                    if (progress.error != null) {
                        _scanState.value = ScanState.Error
                        Timber.e("Music library scan error: ${progress.error}")
                        
                        // Stop foreground service after showing error
                        launch {
                            kotlinx.coroutines.delay(5000) // Show error for 5 seconds
                            stopForegroundService()
                        }
                        return@collect
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during music library scan")
                _scanState.value = ScanState.Error
                _scanProgress.value = _scanProgress.value.copy(
                    error = "Scan failed: ${e.localizedMessage}"
                )
                
                val errorNotification = createScanningNotification(_scanProgress.value)
                notificationManager.notify(NOTIFICATION_ID, errorNotification)
                
                launch {
                    kotlinx.coroutines.delay(5000)
                    stopForegroundService()
                }
            }
        }
    }

    private fun startQuickScanning() {
        if (_scanState.value == ScanState.Scanning || _scanState.value == ScanState.QuickScanning) {
            Timber.w("Scan already in progress, ignoring quick scan request")
            return
        }

        _scanState.value = ScanState.QuickScanning
        
        val initialNotification = createScanningNotification(
            MediaScannerRepository.ScanProgress("Quick scan starting...", 0, 0, false)
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                initialNotification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        currentScanJob = serviceScope.launch {
            try {
                mediaScannerRepository.quickScan().collect { progress ->
                    _scanProgress.value = progress
                    
                    val notification = createScanningNotification(progress)
                    notificationManager.notify(NOTIFICATION_ID, notification)

                    if (progress.isComplete) {
                        _scanState.value = ScanState.Complete
                        Timber.i("Quick scan completed. Processed ${progress.filesScanned} files")
                        
                        launch {
                            kotlinx.coroutines.delay(2000) // Show completion for 2 seconds
                            stopForegroundService()
                        }
                        return@collect
                    }

                    if (progress.error != null) {
                        _scanState.value = ScanState.Error
                        Timber.e("Quick scan error: ${progress.error}")
                        
                        launch {
                            kotlinx.coroutines.delay(3000)
                            stopForegroundService()
                        }
                        return@collect
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during quick scan")
                _scanState.value = ScanState.Error
                _scanProgress.value = _scanProgress.value.copy(
                    error = "Quick scan failed: ${e.localizedMessage}"
                )
                
                val errorNotification = createScanningNotification(_scanProgress.value)
                notificationManager.notify(NOTIFICATION_ID, errorNotification)
                
                launch {
                    kotlinx.coroutines.delay(3000)
                    stopForegroundService()
                }
            }
        }
    }

    private fun stopScanning() {
        if (_scanState.value == ScanState.Idle || _scanState.value == ScanState.Stopping) {
            return
        }

        Timber.d("Stopping media scanner service")
        _scanState.value = ScanState.Stopping
        
        currentScanJob?.cancel()
        currentScanJob = null
        
        _scanState.value = ScanState.Idle
        _scanProgress.value = MediaScannerRepository.ScanProgress("", 0, 0, false)
        
        stopForegroundService()
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // Public methods for external access
    fun getCurrentScanState(): ScanState = _scanState.value
    
    fun isScanning(): Boolean = _scanState.value in listOf(ScanState.Scanning, ScanState.QuickScanning)
    
    fun startFullLibraryScan() {
        startScanning()
    }
    
    fun startFolderScan(folderPath: String) {
        startScanning(folderPath)
    }
    
    fun startQuickLibraryScan() {
        startQuickScanning()
    }
    
    fun stopCurrentScan() {
        stopScanning()
    }
}