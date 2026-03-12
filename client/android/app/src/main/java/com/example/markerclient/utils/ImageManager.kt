package com.example.markerclient.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ImageManager(private val context: Context) {
    private val imageDirectory: File
        get() = File(context.filesDir, "entry_images").apply {
            if(!exists()) mkdirs()
        }

    suspend fun saveImage(
        sourceUri: Uri,
        entryId: String
    ): SavedImage = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis()
            val filename = "entry_${entryId}_${timestamp}.jpg"
            val imageFile = File(imageDirectory, filename)

            // Copy and compress the image
            val bitmap = loadBitmapFromUri(sourceUri)
            val compressedBitmap = compressBitmap(bitmap)

            FileOutputStream(imageFile).use { out ->
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            SavedImage.Success(
                imagePath = imageFile.absolutePath,
                width = compressedBitmap.width,
                height = compressedBitmap.height
            )
        } catch(e: Exception){
            SavedImage.Error(e.message ?: "Failed to save image")
        }
    }

    /**
     * Load bitmap from URI
     */
    private suspend fun loadBitmapFromUri(uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }

    /**
     * Compress bitmap to reasonable size
     */
    private fun compressBitmap(bitmap: Bitmap, maxWidth: Int = 1920, maxHeight: Int = 1920): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val scale = minOf(
            maxWidth.toFloat() / width,
            maxHeight.toFloat() / height
        )

        val scaledWidth = (width * scale).toInt()
        val scaledHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }

    /**
     * Create thumbnail
     */
    private fun createThumbnail(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        return compressBitmap(bitmap, maxWidth, maxHeight)
    }

    /**
     * Delete image files
     */
    suspend fun deleteImage(imagePath: String, thumbnailPath: String? = null) = withContext(Dispatchers.IO) {
        try {
            File(imagePath).delete()
            thumbnailPath?.let { File(it).delete() }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get image file
     */
    fun getImageFile(imagePath: String): File? {
        val file = File(imagePath)
        return if (file.exists()) file else null
    }

    /**
     * Clear all cached images for an entry
     */
    suspend fun clearEntryImages(entryId: String) = withContext(Dispatchers.IO) {
        imageDirectory.listFiles()?.filter {
            it.name.contains("entry_${entryId}_")
        }?.forEach { it.delete() }
    }
}

sealed class SavedImage {
    data class Success(
        val imagePath: String,
        val width: Int,
        val height: Int
    ) : SavedImage()

    data class Error(val message: String) : SavedImage()
}