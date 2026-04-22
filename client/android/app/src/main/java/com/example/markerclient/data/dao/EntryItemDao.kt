package com.example.markerclient.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import androidx.core.database.sqlite.transaction
import com.example.markerclient.data.db.DBConstants
import com.example.markerclient.data.db.MyDBHandler
import com.example.markerclient.domain.model.Entry
import com.example.markerclient.domain.model.EntryItem
import com.mapbox.maps.extension.style.expressions.dsl.generated.image

class EntryItemDao(private val dbHandler: MyDBHandler) {
    fun addNewEntryItem(entryItem: EntryItem, entryId: Long): Long {
        val db = dbHandler.writableDatabase
        var entryItemId = entryItem.id

        return try {
            db.transaction {
                val entryItemValues = ContentValues().apply {
                    put(DBConstants.EntryItems.ENTRY_ITEM_ENTRY_ID_COL, entryId) // set the entryId for the reference entry
                    when(entryItem) {
                        is EntryItem.TextItem -> {
                            put(DBConstants.EntryItems.ENTRY_ITEM_TYPE_COL, DBConstants.EntryItems.ENTRY_ITEM_TEXT_TYPE)
                            put(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL, entryItem.textContent)
                        }
                        is EntryItem.ImageItem -> {
                            put(DBConstants.EntryItems.ENTRY_ITEM_TYPE_COL, DBConstants.EntryItems.ENTRY_ITEM_IMAGE_TYPE)
                            put(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL, entryItem.caption)
                            put(DBConstants.EntryItems.ENTRY_ITEM_LOCAL_URI_COL, entryItem.imageUri)
                        }
                    }
                }
                entryItemId = insert(DBConstants.EntryItems.ENTRY_ITEM_TABLE_NAME, null, entryItemValues)
            }
            entryItemId
        } catch (e: Exception) {
            Log.e("EntryItemRepository", "Error adding entry item to entry ${entryId}", e)
            -1
        }
    }

    fun getEntryItem(id: Long, entryId: Long): EntryItem? {
        val db = dbHandler.readableDatabase
        var entryItem: EntryItem? = null

        try {
            db.transaction {
                val cursor: Cursor = rawQuery(
                    "SELECT * FROM ${DBConstants.Entries.ENTRY_TABLE_NAME} WHERE ${DBConstants.Entries.ENTRY_ID_COL} = ?",
                    arrayOf(id.toString())
                )

                if (cursor.moveToFirst()) {
                    val type = cursor.getString(2);

                    if(type == DBConstants.EntryItems.ENTRY_ITEM_TEXT_TYPE) {
                        entryItem = EntryItem.TextItem(
                            id = cursor.getLong(0),
                            entryId = cursor.getLong(1),
                            textContent = cursor.getString(3)
                        )
                    } else if(type == DBConstants.EntryItems.ENTRY_ITEM_IMAGE_TYPE) {
                        entryItem = EntryItem.ImageItem(
                            id = cursor.getLong(0),
                            entryId = cursor.getLong(1),
                            caption = cursor.getString(3),
                            imageUri = cursor.getString(4)
                        )
                    }
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.e("EntryDao", "Error getting entry $id", e)
        }

        return entryItem
    }

    fun getAllEntryItems(): List<EntryItem> {
        val db = dbHandler.writableDatabase
        val entryItemList = mutableListOf<EntryItem>()

        try{
            db.transaction {
                val cursor: Cursor = rawQuery(
                    "SELECT * FROM ${DBConstants.EntryItems.ENTRY_ITEM_TABLE_NAME} ORDER BY ${DBConstants.EntryItems.ENTRY_ITEM_CREATED_COL} DESC",
                    null
                )

                if (cursor.moveToFirst()) {
                    do{
                        try{
                            val type = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_TYPE_COL))
                            val entryItem: EntryItem = when(type) {
                                DBConstants.EntryItems.ENTRY_ITEM_TEXT_TYPE -> {
                                    EntryItem.TextItem(
                                        id = cursor.getLong(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_ID_COL)),
                                        entryId = cursor.getLong(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_ENTRY_ID_COL)),
                                        textContent = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL))
                                    )
                                }
                                DBConstants.EntryItems.ENTRY_ITEM_IMAGE_TYPE -> {
                                    EntryItem.ImageItem(
                                        id = cursor.getLong(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_ID_COL)),
                                        entryId = cursor.getLong(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_ENTRY_ID_COL)),
                                        caption = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL)),
                                        imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_LOCAL_URI_COL))
                                    )
                                }
                                else -> {
                                    throw IllegalArgumentException("Unhandled EntryItem type: $type")
                                }
                            }

                            entryItemList.add(entryItem)
                        } catch (e: Exception) {
                            Log.e("EntryItemRepository", "Error processing entryItem row", e)
                        }
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.e("EntryItemRepository", "Error getting all entryItems", e)
        }

        return entryItemList
    }

    fun getAllEntryItems(entryId: Long): List<EntryItem> {
        val db = dbHandler.writableDatabase
        val entryItemList = mutableListOf<EntryItem>()

        try{
            db.transaction {
                val cursor: Cursor = rawQuery("""
                SELECT * FROM ${DBConstants.EntryItems.ENTRY_ITEM_TABLE_NAME}
                WHERE ${DBConstants.EntryItems.ENTRY_ITEM_ENTRY_ID_COL} = ?
                """,arrayOf(entryId.toString()))

                if (cursor.moveToFirst()) {
                    do{
                        try{
                            val type = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_TYPE_COL))
                            val entryItem: EntryItem = when(type) {
                                DBConstants.EntryItems.ENTRY_ITEM_TEXT_TYPE -> {
                                    EntryItem.TextItem(
                                        id = cursor.getLong(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_ID_COL)),
                                        entryId = entryId,
                                        textContent = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL))
                                    )
                                }
                                DBConstants.EntryItems.ENTRY_ITEM_IMAGE_TYPE -> {
                                    EntryItem.ImageItem(
                                        id = cursor.getLong(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_ID_COL)),
                                        entryId = entryId,
                                        caption = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL)),
                                        imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_LOCAL_URI_COL))
                                    )
                                }
                                else -> {
                                    throw IllegalArgumentException("Unhandled EntryItem type $type")
                                }
                            }

                            entryItemList.add(entryItem)
                        } catch (e: Exception) {
                            Log.e("EntryItemRepository", "Error processing entryItem row", e)
                        }
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.e("EntryItemRepository", "Error getting all entryItems for entry ${entryId}", e)
        }

        return entryItemList
    }

    fun updateEntryItem(entryItem: EntryItem, entryId: Long): Boolean {
        val db = dbHandler.writableDatabase
        var success = false

        try {
            db.transaction {
                val entryItemValues = ContentValues().apply {
                    put(DBConstants.EntryItems.ENTRY_ITEM_ENTRY_ID_COL, entryId) // set the id for the reference entry
                    when(entryItem) {
                        is EntryItem.TextItem -> {
                            put(DBConstants.EntryItems.ENTRY_ITEM_TYPE_COL, DBConstants.EntryItems.ENTRY_ITEM_TEXT_TYPE)
                            put(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL, entryItem.textContent)
                        }
                        is EntryItem.ImageItem -> {
                            put(DBConstants.EntryItems.ENTRY_ITEM_TYPE_COL, DBConstants.EntryItems.ENTRY_ITEM_IMAGE_TYPE)
                            put(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL, entryItem.caption)
                            put(DBConstants.EntryItems.ENTRY_ITEM_LOCAL_URI_COL, entryItem.imageUri)
                        }
                    }
                }

                val rowsUpdated = update(
                    DBConstants.EntryItems.ENTRY_ITEM_TABLE_NAME,
                    entryItemValues,
                    "${DBConstants.EntryItems.ENTRY_ITEM_ID_COL} = ?",
                    arrayOf(entryItem.id.toString())
                )

                success = rowsUpdated > 0
                Log.d("EntryItemRepository", "Updated entryItem ${entryItem.id}, rows affected: $rowsUpdated")
            }
        } catch (e: Exception) {
            Log.e("EntryItemRepository", "Error updating entryItem ${entryItem.id}", e)
            success = false
        }

        return success
    }

    fun deleteEntryItem(entryItemId: Long) : Boolean {
        val db = dbHandler.writableDatabase
        var success = false

        try {
            db.transaction {
                // Delete the entry item
                val rowsDeleted = delete(
                    DBConstants.EntryItems.ENTRY_ITEM_TABLE_NAME,
                    "${DBConstants.EntryItems.ENTRY_ITEM_ID_COL} = ?",
                    arrayOf(entryItemId.toString())
                )

                success = rowsDeleted > 0
                Log.d("EntryItemRepository", "Deleted entry item $entryItemId, rows affected: $rowsDeleted")
            }
        } catch(e : Exception) {
            Log.e("EntryItemRepository", "Error deleting entry item $entryItemId", e)
            success = false
        }

        return success
    }

    fun deleteEntryItem(entryItem : EntryItem) : Boolean = deleteEntryItem(entryItem.id)
}