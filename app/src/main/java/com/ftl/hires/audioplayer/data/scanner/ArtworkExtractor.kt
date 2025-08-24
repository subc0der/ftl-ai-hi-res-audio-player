package com.ftl.hires.audioplayer.data.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.images.Artwork
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtworkExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val ARTWORK_CACHE_DIR = "album_artwork"
        private const val ARTWORK_SIZE = 512 // Target size for artwork cache
        private const val ARTWORK_QUALITY = 85 // JPEG quality for cached artwork
        
        // Common artwork filenames to look for in directories
        private val ARTWORK_FILENAMES = listOf(
            "cover.jpg", "cover.jpeg", "cover.png",
            "folder.jpg", "folder.jpeg", "folder.png",
            "albumart.jpg", "albumart.jpeg", "albumart.png",
            "front.jpg", "front.jpeg", "front.png",
            "artwork.jpg", "artwork.jpeg", "artwork.png"
        )
    }

    private val artworkCacheDir: File by lazy {
        File(context.cacheDir, ARTWORK_CACHE_DIR).apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Extract and cache artwork from an audio file
     * Returns the path to the cached artwork file, or null if no artwork found
     */
    suspend fun extractArtwork(audioFilePath: String): String? {
        return try {
            val audioFile = File(audioFilePath)
            if (!audioFile.exists()) {
                Timber.w("Audio file does not exist: $audioFilePath")
                return null
            }

            // Generate cache key based on file path and modification time
            val cacheKey = generateCacheKey(audioFile)
            val cachedArtworkFile = File(artworkCacheDir, "$cacheKey.jpg")
            
            // Return cached artwork if it exists and is newer than the audio file
            if (cachedArtworkFile.exists() && cachedArtworkFile.lastModified() >= audioFile.lastModified()) {
                return cachedArtworkFile.absolutePath
            }

            // Try to extract embedded artwork first
            val embeddedArtwork = extractEmbeddedArtwork(audioFile)
            if (embeddedArtwork != null) {
                return cacheArtwork(embeddedArtwork, cachedArtworkFile)
            }

            // Fallback to directory-based artwork
            val directoryArtwork = findDirectoryArtwork(audioFile.parentFile)
            if (directoryArtwork != null) {
                return cacheArtwork(directoryArtwork, cachedArtworkFile)
            }

            Timber.d("No artwork found for: $audioFilePath")
            null
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract artwork from: $audioFilePath")
            null
        }
    }

    /**
     * Extract artwork embedded in the audio file using JAudioTagger
     */
    private fun extractEmbeddedArtwork(audioFile: File): ByteArray? {
        return try {
            val audioFileObj = AudioFileIO.read(audioFile)
            val tag = audioFileObj.tag ?: return null
            
            val artwork = tag.firstArtwork ?: return null
            
            when {
                artwork.binaryData != null -> artwork.binaryData
                artwork.imageUrl != null -> {
                    // Handle URL-based artwork if needed
                    null
                }
                else -> null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract embedded artwork: ${audioFile.absolutePath}")
            null
        }
    }

    /**
     * Find artwork files in the same directory as the audio file
     */
    private fun findDirectoryArtwork(directory: File?): ByteArray? {
        if (directory == null || !directory.isDirectory) return null
        
        return try {
            // Look for common artwork filenames
            for (filename in ARTWORK_FILENAMES) {
                val artworkFile = File(directory, filename)
                if (artworkFile.exists() && artworkFile.isFile) {
                    val artworkData = artworkFile.readBytes()
                    if (isValidImageData(artworkData)) {
                        Timber.d("Found directory artwork: ${artworkFile.absolutePath}")
                        return artworkData
                    }
                }
            }
            
            // If no standard names found, look for any image files
            directory.listFiles { file ->
                file.isFile && file.extension.lowercase() in listOf("jpg", "jpeg", "png", "bmp", "gif")
            }?.firstOrNull()?.let { imageFile ->
                val artworkData = imageFile.readBytes()
                if (isValidImageData(artworkData)) {
                    Timber.d("Found directory image file: ${imageFile.absolutePath}")
                    return artworkData
                }
            }
            
            null
        } catch (e: Exception) {
            Timber.e(e, "Failed to find directory artwork in: ${directory.absolutePath}")
            null
        }
    }

    /**
     * Cache artwork data to file system
     */
    private fun cacheArtwork(artworkData: ByteArray, cacheFile: File): String? {
        return try {
            // Decode and resize the artwork
            val originalBitmap = BitmapFactory.decodeByteArray(artworkData, 0, artworkData.size)
                ?: return null
            
            val resizedBitmap = resizeBitmap(originalBitmap, ARTWORK_SIZE)
            
            // Save as JPEG with compression
            FileOutputStream(cacheFile).use { fos ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, ARTWORK_QUALITY, fos)
            }
            
            // Cleanup
            if (resizedBitmap != originalBitmap) {
                resizedBitmap.recycle()
            }
            originalBitmap.recycle()
            
            Timber.d("Cached artwork: ${cacheFile.absolutePath}")
            cacheFile.absolutePath
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to cache artwork")
            null
        }
    }

    /**
     * Resize bitmap maintaining aspect ratio
     */
    private fun resizeBitmap(original: Bitmap, maxSize: Int): Bitmap {
        val width = original.width
        val height = original.height
        
        if (width <= maxSize && height <= maxSize) {
            return original
        }
        
        val aspectRatio = width.toFloat() / height.toFloat()
        
        val (newWidth, newHeight) = if (width > height) {
            maxSize to (maxSize / aspectRatio).toInt()
        } else {
            (maxSize * aspectRatio).toInt() to maxSize
        }
        
        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
    }

    /**
     * Check if byte array contains valid image data
     */
    private fun isValidImageData(data: ByteArray): Boolean {
        if (data.size < 8) return false
        
        // Check common image format headers
        return when {
            // JPEG
            data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte() -> true
            
            // PNG
            data[0] == 0x89.toByte() && data[1] == 0x50.toByte() && 
            data[2] == 0x4E.toByte() && data[3] == 0x47.toByte() -> true
            
            // BMP
            data[0] == 0x42.toByte() && data[1] == 0x4D.toByte() -> true
            
            // GIF
            data[0] == 0x47.toByte() && data[1] == 0x49.toByte() && 
            data[2] == 0x46.toByte() -> true
            
            else -> false
        }
    }

    /**
     * Generate cache key for artwork based on file path and modification time
     */
    private fun generateCacheKey(audioFile: File): String {
        val input = "${audioFile.absolutePath}:${audioFile.lastModified()}"
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Clear artwork cache
     */
    fun clearArtworkCache() {
        try {
            if (artworkCacheDir.exists()) {
                artworkCacheDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        file.delete()
                    }
                }
            }
            Timber.i("Artwork cache cleared")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear artwork cache")
        }
    }

    /**
     * Get cache size in bytes
     */
    fun getCacheSize(): Long {
        return try {
            if (!artworkCacheDir.exists()) return 0L
            
            var totalSize = 0L
            artworkCacheDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    totalSize += file.length()
                }
            }
            totalSize
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate cache size")
            0L
        }
    }

    /**
     * Check if artwork exists for a given audio file
     */
    suspend fun hasArtwork(audioFilePath: String): Boolean {
        return try {
            val audioFile = File(audioFilePath)
            if (!audioFile.exists()) return false
            
            val cacheKey = generateCacheKey(audioFile)
            val cachedArtworkFile = File(artworkCacheDir, "$cacheKey.jpg")
            
            // Check if cached artwork exists and is valid
            if (cachedArtworkFile.exists() && cachedArtworkFile.lastModified() >= audioFile.lastModified()) {
                return true
            }
            
            // Check for embedded artwork
            val audioFileObj = AudioFileIO.read(audioFile)
            if (audioFileObj.tag?.firstArtwork != null) {
                return true
            }
            
            // Check for directory artwork
            findDirectoryArtwork(audioFile.parentFile) != null
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to check artwork availability: $audioFilePath")
            false
        }
    }

    /**
     * Get artwork file path if it exists (without extracting)
     */
    fun getArtworkPath(audioFilePath: String): String? {
        return try {
            val audioFile = File(audioFilePath)
            if (!audioFile.exists()) return null
            
            val cacheKey = generateCacheKey(audioFile)
            val cachedArtworkFile = File(artworkCacheDir, "$cacheKey.jpg")
            
            if (cachedArtworkFile.exists() && cachedArtworkFile.lastModified() >= audioFile.lastModified()) {
                cachedArtworkFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get artwork path: $audioFilePath")
            null
        }
    }
}