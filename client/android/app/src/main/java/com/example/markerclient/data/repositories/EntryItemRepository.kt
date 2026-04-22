package com.example.markerclient.data.repositories

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.core.database.sqlite.transaction
import com.example.markerclient.data.dao.EntryItemDao
import com.example.markerclient.data.db.DBConstants
import com.example.markerclient.data.db.MyDBHandler
import com.example.markerclient.data.storage.ImageStorageManager
import com.example.markerclient.domain.model.EntryItem

class EntryItemRepository(private val entryItemDao: EntryItemDao, private val imageStorageManager: ImageStorageManager) {
    fun getAllEntryItems(entryId: Long): List<EntryItem> {
        return entryItemDao.getAllEntryItems(entryId)
    }

    suspend fun addImageItem(
        sourceUri: Uri,
        entryId: Long,
        caption: String) : Result<Long> {
        return imageStorageManager.saveImage(sourceUri, entryId)
            .mapCatching { savedImage ->
            val imageItem = EntryItem.ImageItem(
                id = 0,
                entryId = entryId,
                caption = caption,
                imageUri = savedImage.imagePath
            )
            entryItemDao.addNewEntryItem(imageItem, entryId);
        }
    }

    fun updateImageItem(imageItem: EntryItem.ImageItem): Result<Boolean> {
        return try {
            entryItemDao.updateEntryItem(imageItem, imageItem.entryId);
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e);
        }
    }

    suspend fun deleteImageItem(imageItem: EntryItem.ImageItem): Result<Unit> {
        return try {
            imageStorageManager.deleteImage(imageItem.imageUri);
            entryItemDao.deleteEntryItem(imageItem);
            Result.success(Unit)
        } catch(e: Exception) {
            Result.failure(e)
        }
    }

    fun addTextItem(
        content: String,
        entryId: Long): Result<Long> {
        return try {
            val textItem = EntryItem.TextItem(
                id = 0,
                entryId = entryId,
                textContent = content
            )
            val itemId = entryItemDao.addNewEntryItem(textItem, entryId);
            Result.success(itemId)
        } catch(e: Exception) {
            Result.failure(e)
        }
    }

    fun updateTextItem(textItem: EntryItem.TextItem): Result<Boolean> {
        return try {
            entryItemDao.updateEntryItem(textItem, textItem.entryId);
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e);
        }
    }

    fun deleteTextItem(textItem: EntryItem.TextItem): Result<Unit> {
        return try {
            entryItemDao.deleteEntryItem(textItem);
            Result.success(Unit);
        } catch(e: Exception) {
            Result.failure(e);
        }
    }
}