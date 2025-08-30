package com.ftl.hires.audioplayer.audio.equalizer

import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import androidx.compose.ui.graphics.Color

/**
 * Mode-specific preset system for adaptive EQ
 * 
 * Each EQ mode (5/10/20/32 bands) has tailored presets optimized
 * for that specific frequency resolution and use case.
 */

/**
 * Enhanced preset data class with mode support
 */
data class ModeSpecificPreset(
    val name: String,
    val description: String,
    val bands: List<Float>,
    val targetMode: EQMode,
    val category: PresetCategory = PresetCategory.GENERAL,
    val isCustom: Boolean = false,
    val author: String = "FTL Audio",
    val dateCreated: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList()
) {
    val id: String get() = "${targetMode.bandCount}_${name.lowercase().replace(" ", "_")}"
    val modeColor: Color get() = when (targetMode) {
        is EQMode.Simple5Band -> SubcoderColors.Orange
        is EQMode.Standard10Band -> SubcoderColors.Cyan
        is EQMode.Advanced20Band -> SubcoderColors.NeonGreen
        is EQMode.Pro32Band -> SubcoderColors.ElectricBlue
    }
}

/**
 * Preset manager for mode-specific presets
 */
object ModeSpecificPresets {
    
    /**
     * 5-Band Simple Mode Presets
     * Optimized for basic consumer audio adjustment
     */
    val SIMPLE_5_BAND_PRESETS = listOf(
        ModeSpecificPreset(
            name = "Flat",
            description = "Neutral reference sound",
            bands = listOf(0f, 0f, 0f, 0f, 0f), // 32Hz, 125Hz, 500Hz, 2kHz, 8kHz
            targetMode = EQMode.Simple5Band,
            category = PresetCategory.REFERENCE
        ),
        
        ModeSpecificPreset(
            name = "Bass Boost",
            description = "Enhanced low-end for hip-hop and EDM",
            bands = listOf(6f, 3f, 0f, 0f, 0f),
            targetMode = EQMode.Simple5Band,
            category = PresetCategory.ELECTRONIC,
            tags = listOf("bass", "edm", "hip-hop")
        ),
        
        ModeSpecificPreset(
            name = "Vocal",
            description = "Clear vocals and speech",
            bands = listOf(0f, -1f, 4f, 3f, 1f),
            targetMode = EQMode.Simple5Band,
            category = PresetCategory.VOCAL,
            tags = listOf("vocal", "speech", "podcast")
        ),
        
        ModeSpecificPreset(
            name = "Rock",
            description = "Punchy rock and metal sound",
            bands = listOf(3f, 0f, -2f, 2f, 4f),
            targetMode = EQMode.Simple5Band,
            category = PresetCategory.ROCK,
            tags = listOf("rock", "metal", "guitar")
        ),
        
        ModeSpecificPreset(
            name = "Pop",
            description = "Bright and engaging pop music",
            bands = listOf(2f, 1f, 1f, 2f, 3f),
            targetMode = EQMode.Simple5Band,
            category = PresetCategory.FUN,
            tags = listOf("pop", "mainstream", "radio")
        ),
        
        ModeSpecificPreset(
            name = "Classical",
            description = "Natural orchestral balance",
            bands = listOf(0f, 1f, 0f, 1f, 2f),
            targetMode = EQMode.Simple5Band,
            category = PresetCategory.CLASSICAL,
            tags = listOf("classical", "orchestral", "acoustic")
        ),
        
        ModeSpecificPreset(
            name = "Treble Boost",
            description = "Enhanced clarity and sparkle",
            bands = listOf(0f, 0f, 1f, 3f, 5f),
            targetMode = EQMode.Simple5Band,
            category = PresetCategory.AUDIOPHILE,
            tags = listOf("treble", "clarity", "detail")
        )
    )
    
    /**
     * 10-Band Standard Mode Presets  
     * Industry-standard 1/3 octave spacing
     */
    val STANDARD_10_BAND_PRESETS = listOf(
        ModeSpecificPreset(
            name = "Flat",
            description = "Industry standard flat response",
            bands = List(10) { 0f },
            targetMode = EQMode.Standard10Band,
            category = PresetCategory.REFERENCE
        ),
        
        ModeSpecificPreset(
            name = "V-Shaped",
            description = "Enhanced bass and treble, scooped mids",
            bands = listOf(4f, 3f, 1f, -1f, -2f, -2f, 0f, 2f, 4f, 5f),
            targetMode = EQMode.Standard10Band,
            category = PresetCategory.FUN,
            tags = listOf("v-shape", "fun", "consumer")
        ),
        
        ModeSpecificPreset(
            name = "Vocal Clarity",
            description = "Professional vocal enhancement",
            bands = listOf(0f, -1f, 0f, 2f, 4f, 3f, 1f, 0f, 1f, 0f),
            targetMode = EQMode.Standard10Band,
            category = PresetCategory.VOCAL,
            tags = listOf("vocal", "speech", "broadcast")
        ),
        
        ModeSpecificPreset(
            name = "Bass Heavy",
            description = "Extended low-frequency response",
            bands = listOf(6f, 5f, 4f, 2f, 0f, -1f, -1f, 0f, 1f, 2f),
            targetMode = EQMode.Standard10Band,
            category = PresetCategory.ELECTRONIC,
            tags = listOf("bass", "sub-bass", "electronic")
        ),
        
        ModeSpecificPreset(
            name = "Acoustic",
            description = "Natural acoustic instrument reproduction",
            bands = listOf(0f, 0f, 1f, 1f, 0f, 0f, 1f, 2f, 1f, 1f),
            targetMode = EQMode.Standard10Band,
            category = PresetCategory.CLASSICAL,
            tags = listOf("acoustic", "natural", "folk")
        ),
        
        ModeSpecificPreset(
            name = "Live Concert",
            description = "Simulates live venue acoustics",
            bands = listOf(1f, 2f, 1f, 0f, -1f, 0f, 1f, 2f, 2f, 1f),
            targetMode = EQMode.Standard10Band,
            category = PresetCategory.ROCK,
            tags = listOf("live", "concert", "venue")
        ),
        
        ModeSpecificPreset(
            name = "Dance",
            description = "Club and dance music optimization",
            bands = listOf(5f, 4f, 2f, 0f, -1f, 0f, 1f, 2f, 3f, 4f),
            targetMode = EQMode.Standard10Band,
            category = PresetCategory.ELECTRONIC,
            tags = listOf("dance", "club", "electronic")
        )
    )
    
    /**
     * 20-Band Advanced Mode Presets
     * Professional mixing precision
     */
    val ADVANCED_20_BAND_PRESETS = listOf(
        ModeSpecificPreset(
            name = "Reference Flat",
            description = "Precision reference monitoring",
            bands = List(20) { 0f },
            targetMode = EQMode.Advanced20Band,
            category = PresetCategory.REFERENCE,
            tags = listOf("reference", "monitoring", "flat")
        ),
        
        ModeSpecificPreset(
            name = "Mastering",
            description = "Professional mastering curve",
            bands = listOf(
                0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f,
                0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f
            ),
            targetMode = EQMode.Advanced20Band,
            category = PresetCategory.AUDIOPHILE,
            tags = listOf("mastering", "professional", "studio")
        ),
        
        ModeSpecificPreset(
            name = "Vocal Production",
            description = "Professional vocal recording EQ",
            bands = listOf(
                -2f, -1f, 0f, 0f, 0f, 0f, 1f, 2f, 3f, 4f,
                3f, 2f, 1f, 0f, 0f, 1f, 2f, 1f, 0f, -1f
            ),
            targetMode = EQMode.Advanced20Band,
            category = PresetCategory.VOCAL,
            tags = listOf("vocal", "recording", "production")
        ),
        
        ModeSpecificPreset(
            name = "Orchestral",
            description = "Symphonic orchestra optimization",
            bands = listOf(
                0f, 0f, 1f, 1f, 0f, 0f, 0f, 1f, 1f, 1f,
                0f, 0f, 1f, 1f, 2f, 2f, 1f, 1f, 1f, 1f
            ),
            targetMode = EQMode.Advanced20Band,
            category = PresetCategory.CLASSICAL,
            tags = listOf("orchestral", "symphony", "classical")
        ),
        
        ModeSpecificPreset(
            name = "Electronic Master",
            description = "Advanced electronic music processing",
            bands = listOf(
                4f, 3f, 2f, 1f, 0f, 0f, -1f, -1f, 0f, 0f,
                0f, 1f, 1f, 2f, 2f, 3f, 3f, 2f, 1f, 0f
            ),
            targetMode = EQMode.Advanced20Band,
            category = PresetCategory.ELECTRONIC,
            tags = listOf("electronic", "synthesis", "digital")
        )
    )
    
    /**
     * 32-Band Pro Mode Presets
     * Maximum precision audiophile settings  
     */
    val PRO_32_BAND_PRESETS = listOf(
        ModeSpecificPreset(
            name = "Audiophile Reference",
            description = "Ultimate reference standard",
            bands = List(32) { 0f },
            targetMode = EQMode.Pro32Band,
            category = PresetCategory.REFERENCE,
            tags = listOf("reference", "audiophile", "neutral")
        ),
        
        ModeSpecificPreset(
            name = "Hi-Res Optimized",
            description = "Optimized for high-resolution audio formats",
            bands = listOf(
                0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f, 0f, 0f, 1f, 1f, 1f, 2f, 2f, 2f, 1f, 1f, 1f, 1f
            ),
            targetMode = EQMode.Pro32Band,
            category = PresetCategory.AUDIOPHILE,
            tags = listOf("hi-res", "dsd", "flac", "audiophile")
        ),
        
        ModeSpecificPreset(
            name = "Studio Monitor",
            description = "Professional studio monitoring curve",
            bands = listOf(
                0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 1f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f
            ),
            targetMode = EQMode.Pro32Band,
            category = PresetCategory.AUDIOPHILE,
            tags = listOf("studio", "monitor", "professional")
        ),
        
        ModeSpecificPreset(
            name = "Headphone Correction",
            description = "Compensates for typical headphone colorations",
            bands = listOf(
                0f, 0f, 1f, 1f, 1f, 0f, 0f, 0f, -1f, -1f, -1f, 0f, 1f, 2f, 1f, 0f,
                0f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, -1f, -1f
            ),
            targetMode = EQMode.Pro32Band,
            category = PresetCategory.AUDIOPHILE,
            tags = listOf("headphone", "correction", "compensation")
        )
    )
    
    /**
     * Get all presets for a specific mode
     */
    fun getPresetsForMode(mode: EQMode): List<ModeSpecificPreset> {
        return when (mode) {
            is EQMode.Simple5Band -> SIMPLE_5_BAND_PRESETS
            is EQMode.Standard10Band -> STANDARD_10_BAND_PRESETS
            is EQMode.Advanced20Band -> ADVANCED_20_BAND_PRESETS
            is EQMode.Pro32Band -> PRO_32_BAND_PRESETS
        }
    }
    
    /**
     * Get all presets grouped by category for a mode
     */
    fun getPresetsByCategory(mode: EQMode): Map<PresetCategory, List<ModeSpecificPreset>> {
        return getPresetsForMode(mode).groupBy { it.category }
    }
    
    /**
     * Find presets by tags
     */
    fun findPresetsByTag(mode: EQMode, tag: String): List<ModeSpecificPreset> {
        return getPresetsForMode(mode).filter { preset ->
            preset.tags.any { it.contains(tag, ignoreCase = true) } ||
            preset.name.contains(tag, ignoreCase = true) ||
            preset.description.contains(tag, ignoreCase = true)
        }
    }
    
    /**
     * Get recommended presets for a mode (top 3 most versatile)
     */
    fun getRecommendedPresets(mode: EQMode): List<ModeSpecificPreset> {
        val presets = getPresetsForMode(mode)
        return when (mode) {
            is EQMode.Simple5Band -> presets.filter { 
                it.name in listOf("Flat", "Bass Boost", "Pop") 
            }
            is EQMode.Standard10Band -> presets.filter { 
                it.name in listOf("Flat", "V-Shaped", "Vocal Clarity") 
            }
            is EQMode.Advanced20Band -> presets.filter { 
                it.name in listOf("Reference Flat", "Mastering", "Vocal Production") 
            }
            is EQMode.Pro32Band -> presets.filter { 
                it.name in listOf("Audiophile Reference", "Hi-Res Optimized", "Studio Monitor") 
            }
        }
    }
}