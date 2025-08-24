package com.ftl.hires.audioplayer.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.*
import kotlin.random.Random

/**
 * HomeViewModel for FTL Hi-Res Audio Player Command Center
 * 
 * Manages:
 * - Dashboard state and system monitoring
 * - Real-time audio visualization data (32-band spectrum)
 * - Quick EQ controls and presets
 * - Audio analysis and processing metrics
 * - Neural network animation data
 * - System performance monitoring
 * - Library statistics and quick access
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    // Injected dependencies will be added here
    // private val audioRepository: AudioRepository,
    // private val audioAnalysisService: AudioAnalysisService,
    // private val eqRepository: EQRepository,
    // private val systemMonitorService: SystemMonitorService,
    // private val libraryRepository: LibraryRepository
) : ViewModel() {

    // Private mutable state flows
    private val _dashboardState = MutableStateFlow(DashboardState())
    private val _audioVisualizationData = MutableStateFlow(AudioVisualizationData())
    private val _quickControlsState = MutableStateFlow(QuickControlsState())
    private val _audioAnalysisState = MutableStateFlow(AudioAnalysisState())
    private val _systemMonitoringState = MutableStateFlow(SystemMonitoringState())
    private val _libraryStatsState = MutableStateFlow(LibraryStatsState())
    private val _neuralNetworkState = MutableStateFlow(NeuralNetworkState())

    // Public read-only state flows
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()
    val audioVisualizationData: StateFlow<AudioVisualizationData> = _audioVisualizationData.asStateFlow()
    val quickControlsState: StateFlow<QuickControlsState> = _quickControlsState.asStateFlow()
    val audioAnalysisState: StateFlow<AudioAnalysisState> = _audioAnalysisState.asStateFlow()
    val systemMonitoringState: StateFlow<SystemMonitoringState> = _systemMonitoringState.asStateFlow()
    val libraryStatsState: StateFlow<LibraryStatsState> = _libraryStatsState.asStateFlow()
    val neuralNetworkState: StateFlow<NeuralNetworkState> = _neuralNetworkState.asStateFlow()

    // Combined UI state for easy consumption
    val homeUiState: StateFlow<HomeUiState> = combine(
        _dashboardState,
        _audioVisualizationData,
        _quickControlsState,
        _audioAnalysisState,
        _systemMonitoringState,
        _libraryStatsState,
        _neuralNetworkState
    ) { dashboard, visualization, controls, analysis, monitoring, library, network ->
        HomeUiState(
            isLoading = dashboard.isLoading,
            isInitialized = dashboard.isInitialized,
            spectrumData = visualization.spectrumData,
            isAudioPlaying = visualization.isPlaying,
            currentAudioFormat = visualization.currentFormat,
            sampleRate = visualization.sampleRate,
            bitDepth = visualization.bitDepth,
            eqPreset = controls.currentEQPreset,
            quickEQBands = controls.quickEQBands,
            audioLatency = analysis.currentLatency,
            cpuUsage = monitoring.cpuUsage,
            memoryUsage = monitoring.memoryUsage,
            trackCount = library.totalTracks,
            formatDistribution = library.formatDistribution,
            networkNodes = network.nodes,
            networkConnections = network.connections,
            animationOffset = network.animationOffset
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    // Background jobs for continuous monitoring
    private var visualizationJob: Job? = null
    private var systemMonitoringJob: Job? = null
    private var neuralNetworkJob: Job? = null
    private var audioAnalysisJob: Job? = null

    init {
        Timber.d("HomeViewModel initialized")
        initializeHomeScreen()
    }

    /**
     * Initialize the home screen with all necessary data and monitoring
     */
    private fun initializeHomeScreen() {
        viewModelScope.launch {
            try {
                _dashboardState.update { it.copy(isLoading = true) }

                // Initialize all subsystems
                initializeAudioVisualization()
                initializeQuickControls()
                initializeSystemMonitoring()
                initializeLibraryStats()
                initializeNeuralNetwork()
                initializeAudioAnalysis()

                // Start background monitoring jobs
                startVisualizationUpdates()
                startSystemMonitoring()
                startNeuralNetworkAnimation()
                startAudioAnalysis()

                _dashboardState.update {
                    it.copy(
                        isLoading = false,
                        isInitialized = true
                    )
                }

                Timber.i("Home screen initialized successfully")

            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize home screen")
                _dashboardState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    /**
     * Initialize audio visualization with 32-band spectrum analyzer
     */
    private suspend fun initializeAudioVisualization() {
        val initialSpectrumData = generateInitialSpectrum()
        
        _audioVisualizationData.update {
            it.copy(
                spectrumData = initialSpectrumData,
                isInitialized = true,
                currentFormat = "FLAC",
                sampleRate = 48000,
                bitDepth = 24,
                channelCount = 2
            )
        }
        
        Timber.d("Audio visualization initialized with ${initialSpectrumData.size} bands")
    }

    /**
     * Initialize quick EQ controls with default settings
     */
    private suspend fun initializeQuickControls() {
        val defaultEQBands = generateDefaultEQBands()
        
        _quickControlsState.update {
            it.copy(
                currentEQPreset = "Flat",
                quickEQBands = defaultEQBands,
                isEQEnabled = true,
                availablePresets = listOf("Flat", "Rock", "Jazz", "Classical", "Electronic", "Vocal"),
                isInitialized = true
            )
        }
        
        Timber.d("Quick controls initialized with ${defaultEQBands.size} EQ bands")
    }

    /**
     * Initialize system monitoring
     */
    private suspend fun initializeSystemMonitoring() {
        _systemMonitoringState.update {
            it.copy(
                isAudioServiceOnline = true,
                hasRequiredPermissions = true,
                isDSPEngineActive = true,
                nativeLibrariesLoaded = true,
                audioDeviceConnected = true,
                lastUpdated = System.currentTimeMillis(),
                isInitialized = true
            )
        }
        
        Timber.d("System monitoring initialized")
    }

    /**
     * Initialize library statistics
     */
    private suspend fun initializeLibraryStats() {
        // In a real implementation, this would fetch from repository
        val formatDistribution = mapOf(
            "FLAC" to 450,
            "DSD" to 120,
            "WAV" to 230,
            "APE" to 85,
            "MP3" to 340
        )
        
        _libraryStatsState.update {
            it.copy(
                totalTracks = formatDistribution.values.sum(),
                formatDistribution = formatDistribution,
                totalSize = calculateTotalSize(formatDistribution),
                averageQuality = "Hi-Res",
                lastScanned = System.currentTimeMillis(),
                isInitialized = true
            )
        }
        
        Timber.d("Library stats initialized with ${formatDistribution.values.sum()} total tracks")
    }

    /**
     * Initialize neural network background animation
     */
    private suspend fun initializeNeuralNetwork() {
        val nodes = generateNeuralNetworkNodes(20)
        val connections = generateNeuralNetworkConnections(nodes)
        
        _neuralNetworkState.update {
            it.copy(
                nodes = nodes,
                connections = connections,
                animationSpeed = 1.0f,
                intensity = 0.7f,
                isInitialized = true
            )
        }
        
        Timber.d("Neural network initialized with ${nodes.size} nodes and ${connections.size} connections")
    }

    /**
     * Initialize audio analysis engine
     */
    private suspend fun initializeAudioAnalysis() {
        _audioAnalysisState.update {
            it.copy(
                isAnalysisActive = true,
                currentLatency = 64.0f, // milliseconds
                bufferHealth = 95.0f,
                dropoutCount = 0,
                averageCPULoad = 15.0f,
                peakLevel = -6.0f, // dB
                rmsLevel = -18.0f, // dB
                dynamicRange = 72.0f, // dB
                isInitialized = true
            )
        }
        
        Timber.d("Audio analysis initialized")
    }

    /**
     * Start real-time audio visualization updates
     */
    private fun startVisualizationUpdates() {
        visualizationJob?.cancel()
        visualizationJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val currentState = _audioVisualizationData.value
                    
                    if (currentState.isPlaying) {
                        // Generate realistic spectrum data
                        val newSpectrumData = generateRealtimeSpectrum(currentState.spectrumData)
                        
                        _audioVisualizationData.update {
                            it.copy(
                                spectrumData = newSpectrumData,
                                lastUpdated = System.currentTimeMillis()
                            )
                        }
                    }
                    
                    delay(50) // 20 FPS update rate
                    
                } catch (e: Exception) {
                    Timber.e(e, "Error updating visualization data")
                    delay(1000) // Longer delay on error
                }
            }
        }
    }

    /**
     * Start system performance monitoring
     */
    private fun startSystemMonitoring() {
        systemMonitoringJob?.cancel()
        systemMonitoringJob = viewModelScope.launch {
            while (isActive) {
                try {
                    // Simulate system metrics (would be real in production)
                    val cpuUsage = generateRealisticCpuUsage()
                    val memoryUsage = generateRealisticMemoryUsage()
                    
                    _systemMonitoringState.update {
                        it.copy(
                            cpuUsage = cpuUsage,
                            memoryUsage = memoryUsage,
                            lastUpdated = System.currentTimeMillis()
                        )
                    }
                    
                    delay(2000) // Update every 2 seconds
                    
                } catch (e: Exception) {
                    Timber.e(e, "Error updating system monitoring")
                    delay(5000)
                }
            }
        }
    }

    /**
     * Start neural network animation updates
     */
    private fun startNeuralNetworkAnimation() {
        neuralNetworkJob?.cancel()
        neuralNetworkJob = viewModelScope.launch {
            while (isActive) {
                try {
                    _neuralNetworkState.update {
                        it.copy(
                            animationOffset = (it.animationOffset + 0.02f) % 1.0f,
                            lastUpdated = System.currentTimeMillis()
                        )
                    }
                    
                    delay(33) // ~30 FPS animation
                    
                } catch (e: Exception) {
                    Timber.e(e, "Error updating neural network animation")
                    delay(1000)
                }
            }
        }
    }

    /**
     * Start audio analysis monitoring
     */
    private fun startAudioAnalysis() {
        audioAnalysisJob?.cancel()
        audioAnalysisJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val currentState = _audioAnalysisState.value
                    
                    if (currentState.isAnalysisActive) {
                        // Update audio analysis metrics
                        _audioAnalysisState.update {
                            it.copy(
                                currentLatency = generateRealisticLatency(),
                                bufferHealth = generateBufferHealth(),
                                averageCPULoad = generateAudioCPULoad(),
                                peakLevel = generatePeakLevel(),
                                rmsLevel = generateRMSLevel(),
                                lastUpdated = System.currentTimeMillis()
                            )
                        }
                    }
                    
                    delay(500) // Update twice per second
                    
                } catch (e: Exception) {
                    Timber.e(e, "Error updating audio analysis")
                    delay(2000)
                }
            }
        }
    }

    // Public methods for UI interactions

    /**
     * Toggle audio playback
     */
    fun togglePlayback() {
        viewModelScope.launch {
            _audioVisualizationData.update {
                it.copy(isPlaying = !it.isPlaying)
            }
            
            Timber.d("Playback toggled: ${_audioVisualizationData.value.isPlaying}")
        }
    }

    /**
     * Apply quick EQ preset
     */
    fun applyEQPreset(presetName: String) {
        viewModelScope.launch {
            val newEQBands = generateEQBandsForPreset(presetName)
            
            _quickControlsState.update {
                it.copy(
                    currentEQPreset = presetName,
                    quickEQBands = newEQBands
                )
            }
            
            Timber.d("EQ preset applied: $presetName")
        }
    }

    /**
     * Update quick EQ band value
     */
    fun updateQuickEQBand(bandIndex: Int, value: Float) {
        if (bandIndex < 0 || bandIndex >= _quickControlsState.value.quickEQBands.size) return
        
        viewModelScope.launch {
            val updatedBands = _quickControlsState.value.quickEQBands.toMutableList()
            updatedBands[bandIndex] = updatedBands[bandIndex].copy(gain = value)
            
            _quickControlsState.update {
                it.copy(
                    quickEQBands = updatedBands,
                    currentEQPreset = "Custom" // Switch to custom when manually adjusted
                )
            }
        }
    }

    /**
     * Refresh library statistics
     */
    fun refreshLibraryStats() {
        viewModelScope.launch {
            _libraryStatsState.update { it.copy(isRefreshing = true) }
            
            try {
                // Simulate library scan
                delay(1500)
                
                // In real implementation, would call repository
                val updatedStats = _libraryStatsState.value.copy(
                    lastScanned = System.currentTimeMillis(),
                    isRefreshing = false
                )
                
                _libraryStatsState.update { updatedStats }
                
                Timber.d("Library stats refreshed")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh library stats")
                _libraryStatsState.update { 
                    it.copy(
                        isRefreshing = false,
                        error = e.message
                    )
                }
            }
        }
    }

    /**
     * Toggle neural network animation
     */
    fun toggleNeuralNetworkAnimation(enabled: Boolean) {
        _neuralNetworkState.update {
            it.copy(isAnimationEnabled = enabled)
        }
        
        if (enabled && neuralNetworkJob?.isActive != true) {
            startNeuralNetworkAnimation()
        } else if (!enabled) {
            neuralNetworkJob?.cancel()
        }
    }

    /**
     * Reset audio analysis statistics
     */
    fun resetAudioAnalysis() {
        _audioAnalysisState.update {
            it.copy(
                dropoutCount = 0,
                lastResetTime = System.currentTimeMillis()
            )
        }
        
        Timber.d("Audio analysis statistics reset")
    }

    // Helper methods for data generation

    private fun generateInitialSpectrum(): List<SpectrumBand> {
        return (0 until 32).map { index ->
            val frequency = 20f * (2f.pow(index / 4.8f)) // Logarithmic frequency scale
            SpectrumBand(
                frequency = frequency,
                magnitude = 0f,
                phase = 0f,
                bandIndex = index
            )
        }
    }

    private fun generateRealtimeSpectrum(currentSpectrum: List<SpectrumBand>): List<SpectrumBand> {
        return currentSpectrum.map { band ->
            // Simulate realistic audio spectrum with some variation
            val baseMagnitude = when {
                band.frequency < 100f -> Random.nextFloat() * 0.8f + 0.2f  // Bass
                band.frequency < 2000f -> Random.nextFloat() * 0.6f + 0.3f // Midrange
                band.frequency < 8000f -> Random.nextFloat() * 0.5f + 0.2f // Upper midrange
                else -> Random.nextFloat() * 0.3f + 0.1f                   // Treble
            }
            
            band.copy(
                magnitude = baseMagnitude,
                phase = (band.phase + Random.nextFloat() * 0.2f - 0.1f) % (2f * PI)
            )
        }
    }

    private fun generateDefaultEQBands(): List<EQBand> {
        val frequencies = listOf(32f, 64f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)
        return frequencies.mapIndexed { index, frequency ->
            EQBand(
                frequency = frequency,
                gain = 0f, // Flat response
                q = 1f,
                bandIndex = index
            )
        }
    }

    private fun generateEQBandsForPreset(presetName: String): List<EQBand> {
        val currentBands = _quickControlsState.value.quickEQBands
        
        return currentBands.mapIndexed { index, band ->
            val gain = when (presetName) {
                "Rock" -> when (index) {
                    0, 1 -> 3f      // Bass boost
                    2, 3 -> 1f      // Low mid
                    4, 5 -> -1f     // Mid cut
                    6, 7 -> 2f      // High mid boost
                    8, 9 -> 4f      // Treble boost
                    else -> 0f
                }
                "Jazz" -> when (index) {
                    0, 1 -> 1f      // Slight bass
                    2, 3, 4 -> 2f   // Mid boost
                    5, 6 -> 1f      // Upper mid
                    7, 8, 9 -> -1f  // Slight treble cut
                    else -> 0f
                }
                "Classical" -> when (index) {
                    0 -> -1f        // Bass cut
                    1, 2 -> 1f      // Low boost
                    3, 4, 5 -> 2f   // Mid boost
                    6, 7 -> 1f      // Upper mid
                    8, 9 -> 3f      // Treble boost
                    else -> 0f
                }
                "Electronic" -> when (index) {
                    0, 1 -> 5f      // Heavy bass
                    2 -> 2f         // Low mid
                    3, 4, 5 -> -2f  // Mid cut
                    6, 7 -> 1f      // Upper mid
                    8, 9 -> 4f      // Treble boost
                    else -> 0f
                }
                "Vocal" -> when (index) {
                    0, 1 -> -2f     // Bass cut
                    2, 3 -> -1f     // Low mid cut
                    4, 5, 6 -> 3f   // Mid boost for vocals
                    7 -> 1f         // Upper mid
                    8, 9 -> -1f     // Treble cut
                    else -> 0f
                }
                else -> 0f // Flat
            }
            
            band.copy(gain = gain)
        }
    }

    private fun generateNeuralNetworkNodes(count: Int): List<NetworkNode> {
        return (0 until count).map {
            NetworkNode(
                id = it,
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                intensity = Random.nextFloat() * 0.5f + 0.5f,
                connectionCount = 0
            )
        }
    }

    private fun generateNeuralNetworkConnections(nodes: List<NetworkNode>): List<NetworkConnection> {
        val connections = mutableListOf<NetworkConnection>()
        
        for (i in nodes.indices) {
            for (j in i + 1 until nodes.size) {
                val distance = sqrt(
                    (nodes[i].x - nodes[j].x).pow(2) + 
                    (nodes[i].y - nodes[j].y).pow(2)
                )
                
                if (distance < 0.3f && Random.nextFloat() > 0.7f) {
                    connections.add(
                        NetworkConnection(
                            fromNodeId = i,
                            toNodeId = j,
                            strength = 1f - distance,
                            isActive = Random.nextFloat() > 0.3f
                        )
                    )
                }
            }
        }
        
        return connections
    }

    private fun calculateTotalSize(formatDistribution: Map<String, Int>): String {
        // Simulate total library size calculation
        val totalTracks = formatDistribution.values.sum()
        val averageSizeMB = 25 // Average file size in MB
        val totalSizeGB = (totalTracks * averageSizeMB) / 1024.0
        
        return String.format("%.1f GB", totalSizeGB)
    }

    private fun generateRealisticCpuUsage(): Float {
        val baseUsage = 0.15f
        val variation = sin(System.currentTimeMillis() * 0.001) * 0.1f
        return (baseUsage + variation + Random.nextFloat() * 0.05f).coerceIn(0f, 1f)
    }

    private fun generateRealisticMemoryUsage(): Float {
        val baseUsage = 0.45f
        val variation = cos(System.currentTimeMillis() * 0.0005) * 0.05f
        return (baseUsage + variation + Random.nextFloat() * 0.02f).coerceIn(0f, 1f)
    }

    private fun generateRealisticLatency(): Float {
        return 48f + Random.nextFloat() * 32f // 48-80ms range
    }

    private fun generateBufferHealth(): Float {
        return 85f + Random.nextFloat() * 12f // 85-97% range
    }

    private fun generateAudioCPULoad(): Float {
        return 12f + Random.nextFloat() * 8f // 12-20% range
    }

    private fun generatePeakLevel(): Float {
        return -12f + Random.nextFloat() * 6f // -12 to -6 dB range
    }

    private fun generateRMSLevel(): Float {
        return -24f + Random.nextFloat() * 6f // -24 to -18 dB range
    }

    override fun onCleared() {
        super.onCleared()
        
        // Cancel all background jobs
        visualizationJob?.cancel()
        systemMonitoringJob?.cancel()
        neuralNetworkJob?.cancel()
        audioAnalysisJob?.cancel()
        
        Timber.d("HomeViewModel cleared")
    }
}

// Data classes for state management

/**
 * Main dashboard state
 */
data class DashboardState(
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long = 0L
)

/**
 * Audio visualization data for spectrum analyzer
 */
data class AudioVisualizationData(
    val spectrumData: List<SpectrumBand> = emptyList(),
    val isPlaying: Boolean = false,
    val currentFormat: String = "FLAC",
    val sampleRate: Int = 48000,
    val bitDepth: Int = 24,
    val channelCount: Int = 2,
    val isInitialized: Boolean = false,
    val lastUpdated: Long = 0L
)

/**
 * Quick EQ controls state
 */
data class QuickControlsState(
    val currentEQPreset: String = "Flat",
    val quickEQBands: List<EQBand> = emptyList(),
    val isEQEnabled: Boolean = true,
    val availablePresets: List<String> = emptyList(),
    val isInitialized: Boolean = false
)

/**
 * Audio analysis state
 */
data class AudioAnalysisState(
    val isAnalysisActive: Boolean = false,
    val currentLatency: Float = 0f,
    val bufferHealth: Float = 0f,
    val dropoutCount: Int = 0,
    val averageCPULoad: Float = 0f,
    val peakLevel: Float = 0f,
    val rmsLevel: Float = 0f,
    val dynamicRange: Float = 0f,
    val lastResetTime: Long = 0L,
    val isInitialized: Boolean = false,
    val lastUpdated: Long = 0L
)

/**
 * System monitoring state
 */
data class SystemMonitoringState(
    val cpuUsage: Float = 0f,
    val memoryUsage: Float = 0f,
    val isAudioServiceOnline: Boolean = false,
    val hasRequiredPermissions: Boolean = false,
    val isDSPEngineActive: Boolean = false,
    val nativeLibrariesLoaded: Boolean = false,
    val audioDeviceConnected: Boolean = false,
    val isInitialized: Boolean = false,
    val lastUpdated: Long = 0L
)

/**
 * Library statistics state
 */
data class LibraryStatsState(
    val totalTracks: Int = 0,
    val formatDistribution: Map<String, Int> = emptyMap(),
    val totalSize: String = "0 GB",
    val averageQuality: String = "Unknown",
    val isRefreshing: Boolean = false,
    val lastScanned: Long = 0L,
    val error: String? = null,
    val isInitialized: Boolean = false
)

/**
 * Neural network animation state
 */
data class NeuralNetworkState(
    val nodes: List<NetworkNode> = emptyList(),
    val connections: List<NetworkConnection> = emptyList(),
    val animationOffset: Float = 0f,
    val animationSpeed: Float = 1f,
    val intensity: Float = 1f,
    val isAnimationEnabled: Boolean = true,
    val isInitialized: Boolean = false,
    val lastUpdated: Long = 0L
)

/**
 * Combined UI state for easy consumption
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val spectrumData: List<SpectrumBand> = emptyList(),
    val isAudioPlaying: Boolean = false,
    val currentAudioFormat: String = "FLAC",
    val sampleRate: Int = 48000,
    val bitDepth: Int = 24,
    val eqPreset: String = "Flat",
    val quickEQBands: List<EQBand> = emptyList(),
    val audioLatency: Float = 0f,
    val cpuUsage: Float = 0f,
    val memoryUsage: Float = 0f,
    val trackCount: Int = 0,
    val formatDistribution: Map<String, Int> = emptyMap(),
    val networkNodes: List<NetworkNode> = emptyList(),
    val networkConnections: List<NetworkConnection> = emptyList(),
    val animationOffset: Float = 0f
)

// Supporting data classes

/**
 * Spectrum band data
 */
data class SpectrumBand(
    val frequency: Float,
    val magnitude: Float,
    val phase: Float,
    val bandIndex: Int
)

/**
 * EQ band configuration
 */
data class EQBand(
    val frequency: Float,
    val gain: Float,
    val q: Float,
    val bandIndex: Int
)

/**
 * Neural network node
 */
data class NetworkNode(
    val id: Int,
    val x: Float,
    val y: Float,
    val intensity: Float,
    val connectionCount: Int
)

/**
 * Neural network connection
 */
data class NetworkConnection(
    val fromNodeId: Int,
    val toNodeId: Int,
    val strength: Float,
    val isActive: Boolean
)