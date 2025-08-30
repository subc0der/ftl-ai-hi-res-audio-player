package com.ftl.hires.audioplayer.audio.equalizer

import kotlin.math.*
import timber.log.Timber

/**
 * Smart Preset Conversion System
 * 
 * Intelligently converts EQ presets between different mode resolutions
 * using frequency analysis and curve fitting algorithms.
 */
object PresetConverter {
    
    /**
     * Convert a preset from one mode to another with intelligent frequency mapping
     */
    fun convertPreset(
        sourcePreset: ModeSpecificPreset,
        targetMode: EQMode
    ): ModeSpecificPreset {
        if (sourcePreset.targetMode == targetMode) {
            return sourcePreset // No conversion needed
        }
        
        val convertedBands = when {
            // Convert to higher resolution (interpolation)
            targetMode.bandCount > sourcePreset.targetMode.bandCount -> {
                upscalePreset(sourcePreset, targetMode)
            }
            // Convert to lower resolution (decimation with anti-aliasing)
            targetMode.bandCount < sourcePreset.targetMode.bandCount -> {
                downscalePreset(sourcePreset, targetMode)
            }
            // Same band count, different frequency distribution
            else -> {
                remapPreset(sourcePreset, targetMode)
            }
        }
        
        return ModeSpecificPreset(
            name = "${sourcePreset.name} (Converted)",
            description = "Converted from ${sourcePreset.targetMode.displayName} to ${targetMode.displayName}",
            bands = convertedBands,
            targetMode = targetMode,
            category = sourcePreset.category,
            isCustom = true,
            author = "FTL Converter",
            tags = sourcePreset.tags + "converted"
        )
    }
    
    /**
     * Upscale preset to higher resolution using cubic spline interpolation
     */
    private fun upscalePreset(sourcePreset: ModeSpecificPreset, targetMode: EQMode): List<Float> {
        val sourceFrequencies = getFrequenciesForMode(sourcePreset.targetMode)
        val targetFrequencies = getFrequenciesForMode(targetMode)
        
        Timber.d("Upscaling ${sourcePreset.targetMode.bandCount} -> ${targetMode.bandCount} bands")
        
        return targetFrequencies.map { targetFreq ->
            interpolateGainAtFrequency(
                frequency = targetFreq,
                sourceFrequencies = sourceFrequencies,
                sourceGains = sourcePreset.bands
            )
        }
    }
    
    /**
     * Downscale preset to lower resolution using intelligent frequency grouping
     */
    private fun downscalePreset(sourcePreset: ModeSpecificPreset, targetMode: EQMode): List<Float> {
        val sourceFrequencies = getFrequenciesForMode(sourcePreset.targetMode)
        val targetFrequencies = getFrequenciesForMode(targetMode)
        
        Timber.d("Downscaling ${sourcePreset.targetMode.bandCount} -> ${targetMode.bandCount} bands")
        
        return targetFrequencies.map { targetFreq ->
            // Find source bands that should contribute to this target band
            val influenceRadius = getFrequencyInfluenceRadius(targetMode)
            val contributingBands = sourceFrequencies.zip(sourcePreset.bands).filter { (sourceFreq, _) ->
                abs(log10(targetFreq.toFloat()) - log10(sourceFreq.toFloat())) <= influenceRadius
            }
            
            if (contributingBands.isEmpty()) {
                0f // No contributing bands, use flat response
            } else {
                // Weight by frequency proximity (closer frequencies have more influence)
                val weightedSum = contributingBands.sumByDouble { (sourceFreq, gain) ->
                    val distance = abs(log10(targetFreq.toFloat()) - log10(sourceFreq.toFloat()))
                    val weight = exp(-distance * 2) // Exponential decay weighting
                    (gain * weight).toDouble()
                }
                val totalWeight = contributingBands.sumByDouble { (sourceFreq, _) ->
                    val distance = abs(log10(targetFreq.toFloat()) - log10(sourceFreq.toFloat()))
                    exp(-distance * 2).toDouble()
                }
                
                (weightedSum / totalWeight).toFloat().coerceIn(-15f, 15f)
            }
        }
    }
    
    /**
     * Remap preset between different frequency distributions of same resolution
     */
    private fun remapPreset(sourcePreset: ModeSpecificPreset, targetMode: EQMode): List<Float> {
        val sourceFrequencies = getFrequenciesForMode(sourcePreset.targetMode)
        val targetFrequencies = getFrequenciesForMode(targetMode)
        
        Timber.d("Remapping ${sourcePreset.targetMode.displayName} -> ${targetMode.displayName}")
        
        return targetFrequencies.map { targetFreq ->
            interpolateGainAtFrequency(
                frequency = targetFreq,
                sourceFrequencies = sourceFrequencies,
                sourceGains = sourcePreset.bands
            )
        }
    }
    
    /**
     * Interpolate gain value at specific frequency using cubic spline
     */
    private fun interpolateGainAtFrequency(
        frequency: Int,
        sourceFrequencies: List<Int>,
        sourceGains: List<Float>
    ): Float {
        if (sourceFrequencies.size != sourceGains.size) {
            return 0f
        }
        
        val logFreq = log10(frequency.toFloat())
        val logFrequencies = sourceFrequencies.map { log10(it.toFloat()) }
        
        // Find surrounding points for interpolation
        val index = logFrequencies.indexOfFirst { it >= logFreq }
        
        when {
            index == -1 -> {
                // Target frequency is higher than all source frequencies
                return sourceGains.last()
            }
            index == 0 -> {
                // Target frequency is lower than all source frequencies  
                return sourceGains.first()
            }
            else -> {
                // Interpolate between surrounding points
                val x0 = logFrequencies[index - 1]
                val x1 = logFrequencies[index]
                val y0 = sourceGains[index - 1]
                val y1 = sourceGains[index]
                
                // Linear interpolation in log-frequency domain
                val t = (logFreq - x0) / (x1 - x0)
                return (y0 + t * (y1 - y0)).coerceIn(-15f, 15f)
            }
        }
    }
    
    /**
     * Get frequency influence radius for downscaling algorithm
     */
    private fun getFrequencyInfluenceRadius(mode: EQMode): Double {
        return when (mode) {
            is EQMode.Simple5Band -> 0.8    // Wide influence for few bands
            is EQMode.Standard10Band -> 0.4  // Medium influence
            is EQMode.Advanced20Band -> 0.2  // Narrow influence  
            is EQMode.Pro32Band -> 0.1       // Very narrow influence for precision
        }
    }
    
    /**
     * Get standard frequency distribution for each mode
     */
    private fun getFrequenciesForMode(mode: EQMode): List<Int> {
        val config = EQConfiguration.create(mode)
        return config.bands.map { it.frequency }
    }
    
    /**
     * Auto-suggest best matching preset in target mode
     */
    fun findBestMatchingPreset(
        sourcePreset: ModeSpecificPreset,
        targetMode: EQMode
    ): ModeSpecificPreset? {
        val availablePresets = ModeSpecificPresets.getPresetsForMode(targetMode)
        
        // First, try to find preset with same name or similar category
        val exactNameMatch = availablePresets.find { 
            it.name.equals(sourcePreset.name, ignoreCase = true) 
        }
        if (exactNameMatch != null) return exactNameMatch
        
        // Find presets in same category
        val categoryMatches = availablePresets.filter { 
            it.category == sourcePreset.category 
        }
        
        if (categoryMatches.isNotEmpty()) {
            // Score presets by tag similarity
            val bestMatch = categoryMatches.maxByOrNull { candidate ->
                val commonTags = candidate.tags.intersect(sourcePreset.tags.toSet()).size
                val nameDistance = calculateStringDistance(candidate.name, sourcePreset.name)
                commonTags * 10 - nameDistance // Prioritize tag matches over name similarity
            }
            return bestMatch
        }
        
        // Fallback to flat/reference preset
        return availablePresets.find { 
            it.category == PresetCategory.REFERENCE || 
            it.name.contains("flat", ignoreCase = true)
        }
    }
    
    /**
     * Calculate string similarity distance (Levenshtein-like)
     */
    private fun calculateStringDistance(s1: String, s2: String): Int {
        val str1 = s1.lowercase()
        val str2 = s2.lowercase()
        
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }
        
        for (i in 0..str1.length) dp[i][0] = i
        for (j in 0..str2.length) dp[0][j] = j
        
        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                if (str1[i - 1] == str2[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1]
                } else {
                    dp[i][j] = 1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        
        return dp[str1.length][str2.length]
    }
    
    /**
     * Batch convert multiple presets
     */
    fun batchConvertPresets(
        sourcePresets: List<ModeSpecificPreset>,
        targetMode: EQMode
    ): List<ModeSpecificPreset> {
        return sourcePresets.map { convertPreset(it, targetMode) }
    }
    
    /**
     * Analyze preset characteristics for intelligent conversion
     */
    fun analyzePresetCharacteristics(preset: ModeSpecificPreset): PresetCharacteristics {
        val bands = preset.bands
        
        // Calculate frequency response characteristics
        val bassLevel = bands.take(bands.size / 4).average().toFloat()
        val midLevel = bands.drop(bands.size / 4).take(bands.size / 2).average().toFloat()
        val trebleLevel = bands.takeLast(bands.size / 4).average().toFloat()
        
        val bassHeavy = bassLevel > 2f
        val midScooped = midLevel < -1f
        val trebleBoost = trebleLevel > 2f
        val isFlat = bands.all { abs(it) < 0.5f }
        
        return PresetCharacteristics(
            bassLevel = bassLevel,
            midLevel = midLevel,
            trebleLevel = trebleLevel,
            isBassHeavy = bassHeavy,
            isMidScooped = midScooped,
            hasTrebleBoost = trebleBoost,
            isFlat = isFlat,
            dynamicRange = bands.maxOrNull()?.minus(bands.minOrNull() ?: 0f) ?: 0f
        )
    }
}

/**
 * Preset characteristics for analysis
 */
data class PresetCharacteristics(
    val bassLevel: Float,
    val midLevel: Float,
    val trebleLevel: Float,
    val isBassHeavy: Boolean,
    val isMidScooped: Boolean,
    val hasTrebleBoost: Boolean,
    val isFlat: Boolean,
    val dynamicRange: Float
) {
    val shape: String get() = when {
        isFlat -> "Flat"
        isBassHeavy && hasTrebleBoost && isMidScooped -> "V-Shaped"
        isBassHeavy -> "Bass-Heavy"
        hasTrebleBoost -> "Bright"
        isMidScooped -> "Scooped"
        midLevel > 1f -> "Mid-Forward"
        else -> "Balanced"
    }
}