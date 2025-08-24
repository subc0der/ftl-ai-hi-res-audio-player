package com.ftl.hires.audioplayer.data.scanner

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.common.Format
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.Extractor
import androidx.media3.extractor.ExtractorInput
import androidx.media3.extractor.ExtractorOutput
import androidx.media3.extractor.PositionHolder
import androidx.media3.extractor.SeekMap
import androidx.media3.extractor.TrackOutput
import androidx.media3.common.C
import com.ftl.hires.audioplayer.data.repository.MediaScannerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.CannotReadException
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException
import org.jaudiotagger.tag.TagException
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        // Supported audio formats for hi-res audio player
        private val SUPPORTED_FORMATS = setOf(
            "mp3", "flac", "wav", "aac", "ogg", "m4a", "wma", 
            "ape", "dsd", "dsf", "dff", "aiff", "alac", "opus"
        )
        
        // Hi-res format thresholds
        private const val HI_RES_SAMPLE_RATE_THRESHOLD = 96000 // 96kHz+
        private const val HI_RES_BIT_DEPTH_THRESHOLD = 24 // 24-bit+
        
        // Common sample rates for format detection
        private val COMMON_SAMPLE_RATES = listOf(
            44100, 48000, 88200, 96000, 176400, 192000, 352800, 384000, 705600, 768000
        )
        
        // DSD sample rates (1-bit at very high frequencies)
        private val DSD_SAMPLE_RATES = listOf(
            2822400, 5644800, 11289600, 22579200 // DSD64, DSD128, DSD256, DSD512
        )
    }

    /**
     * Extract comprehensive metadata from an audio file
     */
    suspend fun extractMetadata(filePath: String): MediaScannerRepository.AudioFileInfo? {
        return try {
            val file = File(filePath)
            if (!file.exists() || !isAudioFile(filePath)) {
                Timber.w("File does not exist or is not an audio file: $filePath")
                return null
            }

            // Try JAudioTagger first for comprehensive metadata
            val jAudioTaggerInfo = extractWithJAudioTagger(file)
            
            // Fallback to Media3 for additional format info if needed
            val media3Info = extractWithMedia3(filePath)
            
            // Combine information from both sources
            mergeAudioFileInfo(jAudioTaggerInfo, media3Info, filePath)
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract metadata from: $filePath")
            null
        }
    }

    /**
     * Extract metadata using JAudioTagger library (preferred for comprehensive tag support)
     */
    private fun extractWithJAudioTagger(file: File): AudioFileInfo? {
        return try {
            val audioFile = AudioFileIO.read(file)
            val header = audioFile.audioHeader
            val tag = audioFile.tag

            val format = detectFormat(file.extension.lowercase())
            val sampleRate = header.sampleRateAsNumber
            val bitDepth = when {
                header.bitsPerSample > 0 -> header.bitsPerSample
                format == "dsd" -> 1 // DSD is 1-bit
                else -> estimateBitDepthFromFormat(format)
            }

            AudioFileInfo(
                filePath = file.absolutePath,
                title = tag?.getFirst("TITLE")?.takeIf { it.isNotBlank() },
                artist = tag?.getFirst("ARTIST")?.takeIf { it.isNotBlank() },
                album = tag?.getFirst("ALBUM")?.takeIf { it.isNotBlank() },
                duration = (header.trackLength * 1000L), // Convert to milliseconds
                fileSize = file.length(),
                format = format,
                bitrate = header.bitRateAsNumber,
                sampleRate = sampleRate,
                bitDepth = bitDepth,
                channels = header.channelCount,
                trackNumber = parseTrackNumber(tag?.getFirst("TRACK")),
                discNumber = parseDiscNumber(tag?.getFirst("DISC")),
                year = parseYear(tag?.getFirst("YEAR") ?: tag?.getFirst("DATE")),
                genre = tag?.getFirst("GENRE")?.takeIf { it.isNotBlank() },
                artworkPath = null // Will be handled by ArtworkExtractor
            )
        } catch (e: CannotReadException) {
            Timber.w(e, "Cannot read audio file with JAudioTagger: ${file.absolutePath}")
            null
        } catch (e: Exception) {
            Timber.e(e, "JAudioTagger extraction failed: ${file.absolutePath}")
            null
        }
    }

    /**
     * Extract metadata using Media3/ExoPlayer (fallback for format detection)
     */
    private suspend fun extractWithMedia3(filePath: String): AudioFileInfo? {
        return try {
            // This is a simplified version - in a real implementation,
            // you'd set up ExoPlayer with proper extractors
            val file = File(filePath)
            val format = detectFormat(file.extension.lowercase())
            
            AudioFileInfo(
                filePath = filePath,
                title = null,
                artist = null,
                album = null,
                duration = 0L,
                fileSize = file.length(),
                format = format,
                bitrate = null,
                sampleRate = null,
                bitDepth = null,
                channels = null,
                trackNumber = null,
                discNumber = null,
                year = null,
                genre = null,
                artworkPath = null
            )
        } catch (e: Exception) {
            Timber.e(e, "Media3 extraction failed: $filePath")
            null
        }
    }

    /**
     * Merge information from multiple extraction sources
     */
    private fun mergeAudioFileInfo(
        primary: AudioFileInfo?, 
        fallback: AudioFileInfo?, 
        filePath: String
    ): MediaScannerRepository.AudioFileInfo? {
        val file = File(filePath)
        val source = primary ?: fallback ?: return null
        
        return MediaScannerRepository.AudioFileInfo(
            filePath = filePath,
            title = source.title ?: extractTitleFromFilename(file.nameWithoutExtension),
            artist = source.artist ?: "Unknown Artist",
            album = source.album ?: "Unknown Album",
            duration = source.duration,
            fileSize = file.length(),
            format = source.format.uppercase(),
            bitrate = source.bitrate,
            sampleRate = source.sampleRate,
            bitDepth = source.bitDepth,
            channels = source.channels ?: 2, // Default to stereo
            trackNumber = source.trackNumber,
            discNumber = source.discNumber,
            year = source.year,
            genre = source.genre,
            artworkPath = source.artworkPath
        )
    }

    /**
     * Check if a file is a supported audio format
     */
    fun isAudioFile(filePath: String): Boolean {
        val extension = File(filePath).extension.lowercase()
        return extension in SUPPORTED_FORMATS
    }

    /**
     * Get list of all supported audio format extensions
     */
    fun getSupportedFormats(): List<String> {
        return SUPPORTED_FORMATS.toList()
    }

    /**
     * Check if the audio format/specs qualify as hi-res
     */
    fun isHighResFormat(audioInfo: MediaScannerRepository.AudioFileInfo): Boolean {
        return when {
            // DSD formats are always considered hi-res
            audioInfo.format.uppercase() in listOf("DSD", "DSF", "DFF") -> true
            
            // PCM formats: check sample rate and bit depth
            audioInfo.sampleRate != null && audioInfo.sampleRate >= HI_RES_SAMPLE_RATE_THRESHOLD -> true
            audioInfo.bitDepth != null && audioInfo.bitDepth >= HI_RES_BIT_DEPTH_THRESHOLD -> true
            
            // Special cases for known hi-res formats
            audioInfo.format == "FLAC" && (audioInfo.bitDepth ?: 0) >= 24 -> true
            audioInfo.format in listOf("ALAC", "AIFF") && (audioInfo.bitDepth ?: 0) >= 24 -> true
            
            else -> false
        }
    }

    // Helper methods for metadata parsing

    private fun detectFormat(extension: String): String {
        return when (extension) {
            "mp3" -> "MP3"
            "flac" -> "FLAC"
            "wav" -> "WAV"
            "aac", "m4a" -> "AAC"
            "ogg" -> "OGG"
            "wma" -> "WMA"
            "ape" -> "APE"
            "dsd", "dsf", "dff" -> "DSD"
            "aiff", "aif" -> "AIFF"
            "alac" -> "ALAC"
            "opus" -> "OPUS"
            else -> extension.uppercase()
        }
    }

    private fun estimateBitDepthFromFormat(format: String): Int? {
        return when (format.uppercase()) {
            "DSD", "DSF", "DFF" -> 1 // DSD is 1-bit
            "FLAC" -> 24 // FLAC commonly 24-bit
            "ALAC" -> 24 // Apple Lossless commonly 24-bit
            "WAV", "AIFF" -> 16 // Default for unspecified WAV/AIFF
            "MP3", "AAC", "OGG", "OPUS" -> null // Lossy formats don't have bit depth
            else -> null
        }
    }

    private fun parseTrackNumber(trackString: String?): Int? {
        return trackString?.let { 
            // Handle formats like "1", "01", "1/12", "01/12"
            val trackPart = it.split("/").firstOrNull()
            trackPart?.toIntOrNull()
        }
    }

    private fun parseDiscNumber(discString: String?): Int? {
        return discString?.let {
            // Handle formats like "1", "01", "1/2", "01/02"  
            val discPart = it.split("/").firstOrNull()
            discPart?.toIntOrNull()
        }
    }

    private fun parseYear(yearString: String?): Int? {
        return yearString?.let { yearStr ->
            // Handle various date formats: "2023", "2023-01-01", etc.
            val yearMatch = Regex("\\d{4}").find(yearStr)
            yearMatch?.value?.toIntOrNull()
        }
    }

    private fun extractTitleFromFilename(filename: String): String {
        // Clean up filename to extract a reasonable title
        return filename
            .replace(Regex("^\\d+[\\s\\-\\.]*"), "") // Remove leading track numbers
            .replace(Regex("\\[.*?\\]"), "") // Remove anything in brackets
            .replace(Regex("\\(.*?\\)"), "") // Remove anything in parentheses  
            .replace("_", " ") // Replace underscores with spaces
            .trim()
            .takeIf { it.isNotBlank() } ?: filename
    }

    private data class AudioFileInfo(
        val filePath: String,
        val title: String?,
        val artist: String?,
        val album: String?,
        val duration: Long,
        val fileSize: Long,
        val format: String,
        val bitrate: Int?,
        val sampleRate: Int?,
        val bitDepth: Int?,
        val channels: Int?,
        val trackNumber: Int?,
        val discNumber: Int?,
        val year: Int?,
        val genre: String?,
        val artworkPath: String?
    )
}