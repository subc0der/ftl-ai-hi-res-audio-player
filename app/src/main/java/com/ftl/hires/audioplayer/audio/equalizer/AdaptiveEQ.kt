package com.ftl.hires.audioplayer.audio.equalizer

import androidx.compose.ui.graphics.Color
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import kotlin.math.*

/**
 * Adaptive EQ System - Core Data Structures
 * 
 * Provides seamless switching between different EQ modes:
 * - Simple 5-Band: Basic bass, mid, treble control
 * - Standard 10-Band: Industry-standard graphic EQ
 * - Advanced 20-Band: Professional mixing
 * - Pro 32-Band: Audiophile precision control
 */

/**
 * EQ Mode sealed class defining different equalizer configurations
 */
sealed class EQMode(
    val bandCount: Int,
    val displayName: String,
    val description: String,
    val color: Color
) {
    object Simple5Band : EQMode(
        bandCount = 5,
        displayName = "SIMPLE",
        description = "Basic 5-Band EQ",
        color = SubcoderColors.Orange
    )
    
    object Standard10Band : EQMode(
        bandCount = 10,
        displayName = "STANDARD", 
        description = "10-Band Graphic EQ",
        color = SubcoderColors.Cyan
    )
    
    object Advanced20Band : EQMode(
        bandCount = 20,
        displayName = "ADVANCED",
        description = "20-Band Professional EQ",
        color = SubcoderColors.NeonGreen
    )
    
    object Pro32Band : EQMode(
        bandCount = 32,
        displayName = "PRO",
        description = "32-Band Audiophile EQ", 
        color = SubcoderColors.ElectricBlue
    )
    
    companion object {
        val ALL_MODES = listOf(Simple5Band, Standard10Band, Advanced20Band, Pro32Band)
        
        fun fromBandCount(count: Int): EQMode = when (count) {
            5 -> Simple5Band
            10 -> Standard10Band
            20 -> Advanced20Band
            32 -> Pro32Band
            else -> Pro32Band // Default to most advanced
        }
    }
}

/**
 * EQ Configuration data class with intelligent frequency generation
 */
data class EQConfiguration(
    val mode: EQMode,
    val frequencies: List<Int>,
    val bands: List<EqualizerBand>,
    val isActive: Boolean = true
) {
    companion object {
        /**
         * Create EQ configuration for specified mode
         */
        fun create(mode: EQMode): EQConfiguration {
            val frequencies = generateFrequencies(mode.bandCount)
            val bands = frequencies.mapIndexed { index, freq ->
                EqualizerBand(
                    id = index,
                    frequency = freq,
                    gain = 0f,
                    isEnabled = true
                )
            }
            
            return EQConfiguration(
                mode = mode,
                frequencies = frequencies,
                bands = bands
            )
        }
        
        /**
         * Generate logarithmically distributed frequencies for EQ bands
         * 
         * Uses professional audio frequency ranges:
         * - Sub-bass: 20-60 Hz
         * - Bass: 60-250 Hz  
         * - Low-mid: 250-500 Hz
         * - Mid: 500-2000 Hz
         * - High-mid: 2000-4000 Hz
         * - Presence: 4000-6000 Hz
         * - Brilliance: 6000-20000 Hz
         */
        fun generateFrequencies(bandCount: Int): List<Int> {
            val minFreq = 20.0 // 20 Hz
            val maxFreq = 20000.0 // 20 kHz
            
            return when (bandCount) {
                5 -> generate5BandFrequencies()
                10 -> generate10BandFrequencies() 
                20 -> generate20BandFrequencies()
                32 -> generate32BandFrequencies()
                else -> generateLogarithmicFrequencies(bandCount, minFreq, maxFreq)
            }
        }
        
        /**
         * 5-Band EQ: Simple bass/mid/treble control
         */
        private fun generate5BandFrequencies(): List<Int> = listOf(
            32,    // Sub-bass
            125,   // Bass  
            500,   // Mid
            2000,  // High-mid
            8000   // Treble
        )
        
        /**
         * 10-Band EQ: Industry standard graphic equalizer
         */
        private fun generate10BandFrequencies(): List<Int> = listOf(
            31,    // Sub-bass
            63,    // Bass
            125,   // Low-bass
            250,   // Low-mid
            500,   // Mid
            1000,  // High-mid
            2000,  // Presence
            4000,  // High presence  
            8000,  // Brilliance
            16000  // Air
        )
        
        /**
         * 20-Band EQ: Professional mixing console
         */
        private fun generate20BandFrequencies(): List<Int> = listOf(
            25, 40, 63, 100,      // Sub-bass range
            160, 250, 400, 630,   // Bass range
            1000, 1600, 2500,     // Mid range
            4000, 6300, 10000,    // High-mid range
            12500, 16000, 20000   // Treble range
        ).plus(
            // Add intermediate frequencies for smoother control
            listOf(80, 200, 800)
        ).sorted()
        
        /**
         * 32-Band EQ: Maximum precision audiophile control
         */
        private fun generate32BandFrequencies(): List<Int> {
            return generateLogarithmicFrequencies(32, 20.0, 20000.0)
        }
        
        /**
         * Generate logarithmically spaced frequencies
         */
        private fun generateLogarithmicFrequencies(
            count: Int,
            minFreq: Double,
            maxFreq: Double
        ): List<Int> {
            val logMin = ln(minFreq)
            val logMax = ln(maxFreq)
            val logStep = (logMax - logMin) / (count - 1)
            
            return (0 until count).map { i ->
                val logFreq = logMin + i * logStep
                val freq = exp(logFreq)
                
                // Round to nearest standard frequency
                roundToStandardFrequency(freq)
            }
        }
        
        /**
         * Round frequency to nearest standard audio frequency
         */
        private fun roundToStandardFrequency(freq: Double): Int {
            val standardFreqs = listOf(
                20, 25, 31, 40, 50, 63, 80, 100, 125, 160,
                200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600,
                2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500, 16000, 20000
            )
            
            return standardFreqs.minByOrNull { abs(it - freq) } ?: freq.roundToInt()
        }
    }
}

/**
 * EQ Mode Manager - Handles switching between different EQ configurations
 */
class EQModeManager {
    private var currentConfiguration: EQConfiguration = EQConfiguration.create(EQMode.Pro32Band)
    
    fun getCurrentConfiguration(): EQConfiguration = currentConfiguration
    
    fun switchToMode(newMode: EQMode): EQConfiguration {
        // Preserve gains when switching modes by intelligent mapping
        val preservedGains = preserveGainsForMode(currentConfiguration, newMode)
        
        currentConfiguration = EQConfiguration.create(newMode).copy(
            bands = preservedGains
        )
        
        return currentConfiguration
    }
    
    /**
     * Intelligently map EQ gains when switching between modes
     */
    private fun preserveGainsForMode(
        oldConfig: EQConfiguration,
        newMode: EQMode
    ): List<EqualizerBand> {
        val newFrequencies = EQConfiguration.generateFrequencies(newMode.bandCount)
        
        return newFrequencies.mapIndexed { index, newFreq ->
            // Find the closest frequency band from the old configuration
            val closestOldBand = oldConfig.bands.minByOrNull { band ->
                abs(band.frequency - newFreq)
            }
            
            EqualizerBand(
                id = index,
                frequency = newFreq,
                gain = closestOldBand?.gain ?: 0f,
                isEnabled = true
            )
        }
    }
    
    /**
     * Get frequency range information for current mode
     */
    fun getFrequencyRange(): Pair<Int, Int> {
        val frequencies = currentConfiguration.frequencies
        return Pair(frequencies.minOrNull() ?: 20, frequencies.maxOrNull() ?: 20000)
    }
    
    /**
     * Get recommended gain range for current mode
     */
    fun getGainRange(): Pair<Float, Float> = Pair(-15f, 15f)
}

/**
 * EQ Mode Extensions for UI helpers
 */
fun EQMode.getFrequencySpacing(): String = when (this) {
    is EQMode.Simple5Band -> "Wide spacing for basic control"
    is EQMode.Standard10Band -> "1/3 octave standard spacing"
    is EQMode.Advanced20Band -> "1/6 octave professional spacing"
    is EQMode.Pro32Band -> "Maximum precision logarithmic spacing"
}

fun EQMode.getRecommendedUse(): String = when (this) {
    is EQMode.Simple5Band -> "Quick adjustments, casual listening"
    is EQMode.Standard10Band -> "Music mixing, live sound"
    is EQMode.Advanced20Band -> "Studio mixing, mastering"
    is EQMode.Pro32Band -> "Audiophile tuning, room correction"
}

fun EQMode.getCyberpunkIcon(): String = when (this) {
    is EQMode.Simple5Band -> "▌▌▌▌▌"
    is EQMode.Standard10Band -> "▌▌▌▌▌▌▌▌▌▌"
    is EQMode.Advanced20Band -> "████████████████████"
    is EQMode.Pro32Band -> "■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"
}