package com.ftl.hires.audioplayer.audio

import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import androidx.media3.exoplayer.ExoPlayer
import com.ftl.hires.audioplayer.audio.equalizer.EqualizerBand
import com.ftl.hires.audioplayer.audio.equalizer.EqualizerPreset
import com.ftl.hires.audioplayer.audio.equalizer.EQMode
import com.ftl.hires.audioplayer.audio.equalizer.EQConfiguration
import com.ftl.hires.audioplayer.audio.equalizer.EQModeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FTL Equalizer Processor - Adaptive Multi-Band Audio Processing
 * 
 * Integrates Android's hardware AudioEffect.Equalizer with our adaptive
 * EQ system supporting 5, 10, 20, and 32-band configurations.
 * 
 * Features:
 * - Adaptive EQ modes (5/10/20/32 bands)
 * - Seamless mode switching with gain preservation
 * - Real-time EQ band adjustment
 * - Preset management with audio processing
 * - Hardware-accelerated audio effects
 * - Intelligent frequency mapping
 */
@Singleton
class FTLEqualizerProcessor @Inject constructor() {
    
    private var systemEqualizer: Equalizer? = null
    private var audioSessionId: Int = AudioEffect.ERROR_NO_INIT
    
    // Adaptive EQ System
    private val eqModeManager = EQModeManager()
    
    // EQ State Management
    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()
    
    private val _currentConfiguration = MutableStateFlow(EQConfiguration.create(EQMode.Pro32Band))
    val currentConfiguration: StateFlow<EQConfiguration> = _currentConfiguration.asStateFlow()
    
    private val _currentBands = MutableStateFlow(_currentConfiguration.value.bands)
    val currentBands: StateFlow<List<EqualizerBand>> = _currentBands.asStateFlow()
    
    private val _currentMode = MutableStateFlow<EQMode>(EQMode.Pro32Band)
    val currentMode: StateFlow<EQMode> = _currentMode.asStateFlow()
    
    private val _activePreset = MutableStateFlow<EqualizerPreset?>(null)
    val activePreset: StateFlow<EqualizerPreset?> = _activePreset.asStateFlow()
    
    // System EQ info
    private val _supportedBands = MutableStateFlow<List<Int>>(emptyList())
    val supportedBands: StateFlow<List<Int>> = _supportedBands.asStateFlow()
    
    private val _bandFrequencyRange = MutableStateFlow(Pair(0, 0))
    val bandFrequencyRange: StateFlow<Pair<Int, Int>> = _bandFrequencyRange.asStateFlow()
    
    /**
     * Initialize the equalizer with the audio session from ExoPlayer
     */
    fun initialize(player: ExoPlayer) {
        try {
            // Get audio session ID from ExoPlayer
            audioSessionId = player.audioSessionId
            
            if (audioSessionId == AudioEffect.ERROR_NO_INIT.toInt()) {
                Timber.w("Invalid audio session ID, EQ will not work")
                return
            }
            
            // Create system equalizer
            systemEqualizer?.release()
            systemEqualizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
            
            _isEnabled.value = true
            inspectSystemEqualizer()
            
            Timber.d("FTL Equalizer initialized with session ID: $audioSessionId")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize equalizer")
            _isEnabled.value = false
        }
    }
    
    /**
     * Inspect the system equalizer capabilities
     */
    private fun inspectSystemEqualizer() {
        systemEqualizer?.let { eq ->
            try {
                val numBands = eq.numberOfBands.toInt()
                val bandFrequencies = (0 until numBands).map { 
                    eq.getCenterFreq(it.toShort()) 
                }
                val freqRange = eq.bandLevelRange
                
                _supportedBands.value = bandFrequencies
                _bandFrequencyRange.value = Pair(freqRange[0].toInt(), freqRange[1].toInt())
                
                Timber.d("System EQ: $numBands bands, range: ${freqRange[0]}dB to ${freqRange[1]}dB")
                Timber.d("Band frequencies: $bandFrequencies")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to inspect system equalizer")
            }
        }
    }
    
    /**
     * Apply EQ band settings to the audio processor
     * Maps our 32 UI bands to the available system EQ bands (typically 5)
     */
    fun applyBandSettings(bands: List<EqualizerBand>) {
        systemEqualizer?.let { eq ->
            try {
                val numSystemBands = eq.numberOfBands.toInt()
                Timber.d("Mapping 32 UI bands to $numSystemBands system bands")
                
                // Group our 32 bands into system band groups
                val bandsPerSystemBand = bands.size / numSystemBands
                val remainder = bands.size % numSystemBands
                
                for (systemBandIndex in 0 until numSystemBands) {
                    // Calculate which UI bands map to this system band
                    val startIdx = systemBandIndex * bandsPerSystemBand + minOf(systemBandIndex, remainder)
                    val endIdx = startIdx + bandsPerSystemBand + (if (systemBandIndex < remainder) 1 else 0)
                    
                    // Average the gains of the UI bands that map to this system band
                    val mappedBands = bands.subList(startIdx, minOf(endIdx, bands.size))
                    val averageGain = if (mappedBands.isNotEmpty()) {
                        mappedBands.map { it.gain }.average().toFloat()
                    } else {
                        0f
                    }
                    
                    // Apply the averaged gain to the system band
                    val systemGain = convertToSystemGain(averageGain)
                    eq.setBandLevel(systemBandIndex.toShort(), systemGain)
                    
                    val systemFreq = eq.getCenterFreq(systemBandIndex.toShort())
                    Timber.d("System band $systemBandIndex (${systemFreq}Hz): UI bands $startIdx-${endIdx-1} -> ${averageGain}dB")
                }
                
                _currentBands.value = bands
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to apply band settings")
            }
        }
    }
    
    /**
     * Apply a complete EQ preset
     */
    fun applyPreset(preset: EqualizerPreset) {
        try {
            // Update our 32-band system with preset values
            val updatedBands = _currentBands.value.mapIndexed { index, band ->
                val gain = if (index < preset.bands.size) preset.bands[index] else 0f
                band.copy(gain = gain)
            }
            
            applyBandSettings(updatedBands)
            _activePreset.value = preset
            
            Timber.d("Applied preset: ${preset.name}")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to apply preset: ${preset.name}")
        }
    }
    
    /**
     * Update a single EQ band in real-time
     */
    fun updateBand(bandId: Int, gain: Float) {
        try {
            val updatedBands = _currentBands.value.map { band ->
                if (band.id == bandId) {
                    band.copy(gain = gain)
                } else {
                    band
                }
            }
            
            // Apply all bands to recalculate the system band mappings
            // This ensures that changing one UI band properly affects the corresponding system band
            applyBandSettings(updatedBands)
            
            _activePreset.value = null // Clear preset when manually adjusted
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to update band $bandId")
        }
    }
    
    /**
     * Reset EQ to flat response (all bands to 0dB)
     */
    fun resetToFlat() {
        val flatBands = _currentBands.value.map { it.copy(gain = 0f) }
        applyBandSettings(flatBands)
        _activePreset.value = null
        Timber.d("EQ reset to flat response")
    }
    
    /**
     * Enable/disable the equalizer
     */
    fun setEnabled(enabled: Boolean) {
        try {
            systemEqualizer?.enabled = enabled
            _isEnabled.value = enabled
            Timber.d("EQ enabled: $enabled")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set EQ enabled: $enabled")
        }
    }
    
    /**
     * Convert our gain range (-15 to +15) to system equalizer range
     */
    private fun convertToSystemGain(ourGain: Float): Short {
        systemEqualizer?.let { eq ->
            val range = eq.bandLevelRange
            val minGain = range[0].toFloat()
            val maxGain = range[1].toFloat()
            
            // Map our -15 to +15 range to system range
            val normalizedGain = (ourGain + 15f) / 30f // Convert to 0-1
            val systemGain = (minGain + normalizedGain * (maxGain - minGain)).toInt()
            
            return systemGain.coerceIn(minGain.toInt(), maxGain.toInt()).toShort()
        }
        return 0
    }
    
    /**
     * Get current EQ band values for UI updates
     */
    fun getCurrentBands(): List<EqualizerBand> = _currentBands.value
    
    /**
     * Switch EQ mode with intelligent gain preservation
     */
    fun switchMode(newMode: EQMode) {
        try {
            val newConfig = eqModeManager.switchToMode(newMode)
            
            _currentMode.value = newMode
            _currentConfiguration.value = newConfig
            _currentBands.value = newConfig.bands
            
            // Apply the new configuration to the audio processor
            applyBandSettings(newConfig.bands)
            
            Timber.d("Switched to EQ mode: ${newMode.displayName} (${newMode.bandCount} bands)")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to switch EQ mode to: ${newMode.displayName}")
        }
    }
    
    /**
     * Get available EQ modes
     */
    fun getAvailableModes(): List<EQMode> = EQMode.ALL_MODES
    
    /**
     * Get current EQ mode
     */
    fun getCurrentMode(): EQMode = _currentMode.value
    
    /**
     * Get current EQ configuration
     */
    fun getCurrentConfiguration(): EQConfiguration = _currentConfiguration.value
    
    /**
     * Check if EQ is properly initialized and working
     */
    fun isReady(): Boolean {
        return systemEqualizer != null && audioSessionId != AudioEffect.ERROR_NO_INIT.toInt()
    }
    
    /**
     * Release the equalizer resources
     */
    fun release() {
        try {
            systemEqualizer?.release()
            systemEqualizer = null
            audioSessionId = AudioEffect.ERROR_NO_INIT.toInt()
            _isEnabled.value = false
            
            Timber.d("FTL Equalizer released")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to release equalizer")
        }
    }
    
    /**
     * Get equalizer information for debugging
     */
    fun getEqualizerInfo(): EqualizerInfo? {
        return systemEqualizer?.let { eq ->
            EqualizerInfo(
                numBands = eq.numberOfBands.toInt(),
                bandFrequencies = (0 until eq.numberOfBands).map { 
                    eq.getCenterFreq(it.toShort()) 
                },
                gainRange = eq.bandLevelRange.let { Pair(it[0].toInt(), it[1].toInt()) },
                isEnabled = eq.enabled,
                audioSessionId = audioSessionId
            )
        }
    }
    
    data class EqualizerInfo(
        val numBands: Int,
        val bandFrequencies: List<Int>,
        val gainRange: Pair<Int, Int>,
        val isEnabled: Boolean,
        val audioSessionId: Int
    )
}