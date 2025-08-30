package com.ftl.hires.audioplayer.audio.equalizer

/**
 * Equalizer preset with 32-band settings
 */
data class EqualizerPreset(
    val name: String,
    val description: String,
    val bands: List<Float>, // 32 gain values in dB
    val isCustom: Boolean = false,
    val category: PresetCategory = PresetCategory.GENERAL
) {
    companion object {
        
        /**
         * Built-in audiophile presets for 32-band EQ
         */
        val BUILT_IN_PRESETS = listOf(
            
            // Flat response
            EqualizerPreset(
                name = "Flat",
                description = "Neutral reference - no coloration",
                bands = List(32) { 0f },
                category = PresetCategory.REFERENCE
            ),
            
            // Bass enhancement
            EqualizerPreset(
                name = "Bass Boost",
                description = "Enhanced low-end for EDM and hip-hop",
                bands = listOf(
                    4f, 4f, 3f, 3f, 2f, 2f, 1f, 1f,  // Sub-bass and bass boost
                    0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,  // Midrange neutral
                    0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,  // Upper-mid neutral
                    0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f   // Treble neutral
                ),
                category = PresetCategory.ELECTRONIC
            ),
            
            // Vocal clarity
            EqualizerPreset(
                name = "Vocal",
                description = "Enhanced vocal presence and clarity",
                bands = listOf(
                    0f, 0f, 0f, 0f, 0f, 0f, -1f, -1f, // Bass slightly reduced
                    0f, 0f, 1f, 2f, 3f, 4f, 3f, 2f,   // Midrange boost for vocals
                    1f, 1f, 2f, 2f, 1f, 1f, 0f, 0f,   // Upper-mid presence
                    0f, 0f, 1f, 1f, 0f, 0f, 0f, 0f    // Gentle treble
                ),
                category = PresetCategory.VOCAL
            ),
            
            // Classical music
            EqualizerPreset(
                name = "Classical",
                description = "Natural orchestral balance",
                bands = listOf(
                    0f, 0f, 0f, 1f, 1f, 0f, 0f, 0f,   // Subtle bass warmth
                    0f, 1f, 1f, 1f, 0f, 0f, 0f, 1f,   // Midrange clarity
                    1f, 1f, 0f, 0f, 1f, 1f, 1f, 2f,   // String presence
                    2f, 2f, 1f, 1f, 0f, 0f, 0f, 0f    // Air and sparkle
                ),
                category = PresetCategory.CLASSICAL
            ),
            
            // Jazz
            EqualizerPreset(
                name = "Jazz",
                description = "Warm and intimate jazz sound",
                bands = listOf(
                    0f, 0f, 1f, 1f, 1f, 1f, 0f, 0f,   // Warm bass
                    0f, 0f, 0f, 1f, 1f, 2f, 1f, 1f,   // Rich midrange
                    1f, 0f, 0f, 0f, 1f, 1f, 1f, 1f,   // Smooth highs
                    1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f    // Gentle treble
                ),
                category = PresetCategory.JAZZ
            ),
            
            // Rock/Metal
            EqualizerPreset(
                name = "Rock",
                description = "Punchy and energetic for rock music",
                bands = listOf(
                    2f, 2f, 1f, 1f, 0f, 0f, 1f, 2f,   // Solid bass foundation
                    -1f, -1f, 0f, 1f, 2f, 1f, 0f, 0f, // Scooped mids
                    1f, 2f, 3f, 3f, 2f, 1f, 2f, 3f,   // Aggressive highs
                    3f, 2f, 1f, 1f, 0f, 0f, 0f, 0f    // Crisp treble
                ),
                category = PresetCategory.ROCK
            ),
            
            // Hi-Res Audio
            EqualizerPreset(
                name = "Hi-Res",
                description = "Optimized for high-resolution audio",
                bands = listOf(
                    0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,   // Clean bass
                    0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,   // Transparent mids
                    0f, 0f, 0f, 0f, 0f, 0f, 1f, 1f,   // Subtle high detail
                    1f, 2f, 2f, 2f, 1f, 1f, 1f, 1f    // Extended frequency response
                ),
                category = PresetCategory.AUDIOPHILE
            ),
            
            // V-Shaped (fun sound)
            EqualizerPreset(
                name = "V-Shaped",
                description = "Enhanced bass and treble, recessed mids",
                bands = listOf(
                    3f, 3f, 2f, 2f, 1f, 1f, 0f, 0f,   // Strong bass
                    -1f, -2f, -2f, -1f, -1f, 0f, 0f, 0f, // Recessed lower mids
                    0f, 0f, 0f, 0f, 1f, 2f, 3f, 3f,   // Rising highs
                    3f, 3f, 2f, 2f, 1f, 1f, 0f, 0f    // Bright treble
                ),
                category = PresetCategory.FUN
            ),
            
            // Electronic/EDM
            EqualizerPreset(
                name = "Electronic",
                description = "Optimized for electronic dance music",
                bands = listOf(
                    5f, 4f, 3f, 2f, 2f, 1f, 1f, 0f,   // Heavy sub-bass
                    0f, 0f, 0f, 0f, 1f, 1f, 0f, 0f,   // Clean mids
                    1f, 1f, 2f, 2f, 3f, 3f, 4f, 4f,   // Bright highs
                    3f, 3f, 2f, 2f, 1f, 1f, 0f, 0f    // Sparkle
                ),
                category = PresetCategory.ELECTRONIC
            )
        )
        
        /**
         * Create a custom preset with current EQ settings
         */
        fun createCustomPreset(name: String, bands: List<Float>): EqualizerPreset {
            return EqualizerPreset(
                name = name,
                description = "Custom user preset",
                bands = bands,
                isCustom = true,
                category = PresetCategory.CUSTOM
            )
        }
    }
}

/**
 * Preset categories for organization
 */
enum class PresetCategory(val displayName: String, val icon: String) {
    REFERENCE("Reference", "⚖️"),
    CLASSICAL("Classical", "🎼"),
    JAZZ("Jazz", "🎷"), 
    ROCK("Rock", "🎸"),
    ELECTRONIC("Electronic", "🎛️"),
    VOCAL("Vocal", "🎤"),
    AUDIOPHILE("Hi-Res", "💎"),
    FUN("Fun", "🎉"),
    CUSTOM("Custom", "⚙️"),
    GENERAL("General", "🎵")
}