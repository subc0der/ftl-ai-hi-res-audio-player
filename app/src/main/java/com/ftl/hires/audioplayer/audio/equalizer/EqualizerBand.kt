package com.ftl.hires.audioplayer.audio.equalizer

/**
 * Represents a single band in the 32-band parametric equalizer
 */
data class EqualizerBand(
    val id: Int,
    val frequency: Int, // Center frequency in Hz
    val gain: Float = 0f, // Gain in dB (-15 to +15)
    val isEnabled: Boolean = true
) {
    companion object {
        const val MIN_GAIN = -15f
        const val MAX_GAIN = 15f
        const val DEFAULT_GAIN = 0f
        
        /**
         * Standard 32-band frequencies for audiophile EQ
         * Based on 1/3 octave spacing for precise control
         */
        val STANDARD_32_BANDS = listOf(
            EqualizerBand(0, 20),      // Sub-bass
            EqualizerBand(1, 25),      // Sub-bass
            EqualizerBand(2, 31),      // Sub-bass
            EqualizerBand(3, 40),      // Bass
            EqualizerBand(4, 50),      // Bass
            EqualizerBand(5, 63),      // Bass
            EqualizerBand(6, 80),      // Bass
            EqualizerBand(7, 100),     // Bass
            EqualizerBand(8, 125),     // Low-midrange
            EqualizerBand(9, 160),     // Low-midrange
            EqualizerBand(10, 200),    // Low-midrange
            EqualizerBand(11, 250),    // Low-midrange
            EqualizerBand(12, 315),    // Midrange
            EqualizerBand(13, 400),    // Midrange
            EqualizerBand(14, 500),    // Midrange
            EqualizerBand(15, 630),    // Midrange
            EqualizerBand(16, 800),    // Midrange
            EqualizerBand(17, 1000),   // Midrange (1kHz reference)
            EqualizerBand(18, 1250),   // Upper-midrange
            EqualizerBand(19, 1600),   // Upper-midrange
            EqualizerBand(20, 2000),   // Upper-midrange
            EqualizerBand(21, 2500),   // Upper-midrange
            EqualizerBand(22, 3150),   // High-midrange
            EqualizerBand(23, 4000),   // High-midrange
            EqualizerBand(24, 5000),   // High-midrange
            EqualizerBand(25, 6300),   // Treble
            EqualizerBand(26, 8000),   // Treble
            EqualizerBand(27, 10000),  // High treble
            EqualizerBand(28, 12500),  // High treble
            EqualizerBand(29, 16000),  // High treble
            EqualizerBand(30, 20000),  // Super treble
            EqualizerBand(31, 25000)   // Ultra treble (for hi-res audio)
        )
    }
    
    /**
     * Format frequency for display
     */
    fun getDisplayFrequency(): String {
        return when {
            frequency < 1000 -> "${frequency}Hz"
            frequency < 10000 -> "${frequency / 1000}.${(frequency % 1000) / 100}kHz"
            else -> "${frequency / 1000}kHz"
        }
    }
    
    /**
     * Get frequency category for grouping
     */
    fun getFrequencyCategory(): FrequencyCategory {
        return when {
            frequency < 60 -> FrequencyCategory.SUB_BASS
            frequency < 250 -> FrequencyCategory.BASS
            frequency < 2000 -> FrequencyCategory.MIDRANGE
            frequency < 6000 -> FrequencyCategory.HIGH_MID
            frequency < 12000 -> FrequencyCategory.TREBLE
            else -> FrequencyCategory.HIGH_TREBLE
        }
    }
}

/**
 * Frequency categories for visual grouping
 */
enum class FrequencyCategory(val displayName: String, val color: androidx.compose.ui.graphics.Color) {
    SUB_BASS("Sub Bass", androidx.compose.ui.graphics.Color(0xFF7C4DFF)),  // Deep purple
    BASS("Bass", androidx.compose.ui.graphics.Color(0xFF3F51B5)),          // Indigo
    MIDRANGE("Mid", androidx.compose.ui.graphics.Color(0xFF00BCD4)),       // Cyan
    HIGH_MID("High Mid", androidx.compose.ui.graphics.Color(0xFF4CAF50)),  // Green  
    TREBLE("Treble", androidx.compose.ui.graphics.Color(0xFFFF9800)),      // Orange
    HIGH_TREBLE("High", androidx.compose.ui.graphics.Color(0xFFF44336))    // Red
}