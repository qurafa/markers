package com.example.markerclient.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.markerclient.domain.Mark
import com.mapbox.geojson.Feature
import androidx.core.database.sqlite.transaction
import com.example.markerclient.domain.Entry
import com.example.markerclient.domain.EntryItem

class MarkRepository(private val dbHandler: MyDBHandler) {
    fun addNewMark(mark: Mark): Long {
        val db = dbHandler.writableDatabase
        var markId = mark.markId

        return try {
            db.transaction {
                val markValues = ContentValues().apply {
                    put(DBConstants.Marks.MARK_BUFFER_COL, if (mark.markIsBuffer) 1 else 0)
                    put(DBConstants.Marks.MARK_TITLE_COL, mark.markTitle)
                    put(DBConstants.Marks.MARK_DESC_COL, mark.markDescription)
                    put(DBConstants.Marks.MARK_GOEM_JSON_COL, mark.markFeature.toJson())
                    put(DBConstants.Marks.MARK_ICON_COL, mark.markIconImage)
                    put(DBConstants.Marks.MARK_COVER_IMG_COL, mark.markCoverImage)
                }

                markId = insert(DBConstants.Marks.MARK_TABLE_NAME, null, markValues)

                // Insert mark<->entries relationships
                for (entry in mark.markEntries) {
                    val junctionValues = ContentValues().apply {
                        put(DBConstants.MarkEntries.MARK_ENTRIES_MARK_ID_COL, markId)
                        put(DBConstants.MarkEntries.MARK_ENTRIES_ENTRY_ID_COL, entry.entryId)
                    }
                    insert(DBConstants.MarkEntries.MARK_ENTRIES_TABLE_NAME, null, junctionValues)
                }
            }
            markId
        } catch (e: Exception) {
            Log.e("MarkRepository", "Error adding mark", e)
            -1
        }
    }

    fun getMark(id: Long): Mark? {
        val db = dbHandler.readableDatabase
        var mark: Mark? = null

        try {
            db.transaction {
                val cursor: Cursor = rawQuery(
                    "SELECT * FROM ${DBConstants.Marks.MARK_TABLE_NAME} WHERE ${DBConstants.Marks.MARK_ID_COL} = ?",
                    arrayOf(id.toString())
                )

                if (cursor.moveToFirst()) {
                    mark = Mark().apply {
                        update(
                            id = cursor.getLong(0),
                            isBuffer = cursor.getInt(1) == 1,
                            title = cursor.getString(2),
                            desc = cursor.getString(3),
                            feature = try {
                                Feature.fromJson(cursor.getString(4))
                            } catch (e: Exception) {
                                Log.w("MarkRepository", "Invalid GeoJSON for mark $id", e)
                                Feature.fromGeometry(null)
                            },
                            iconImage = cursor.getString(5),
                            coverImage = cursor.getString(6),
                            entries = getEntries(cursor.getLong(0))
                        )
                    }
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.e("MarkRepository", "Error getting mark $id", e)
        }

        return mark
    }

    fun getEntries(markId: Long) : List<Entry> {
        val db = dbHandler.writableDatabase
        val entryList = mutableListOf<Entry>()

        try{
            db.transaction {
                val cursor: Cursor = rawQuery("""
                SELECT e.* FROM ${DBConstants.Entries.ENTRY_TABLE_NAME} e
                INNER JOIN ${DBConstants.MarkEntries.MARK_ENTRIES_TABLE_NAME} em 
                ON e.${DBConstants.Entries.ENTRY_ID_COL} = em.${DBConstants.MarkEntries.MARK_ENTRIES_ENTRY_ID_COL}
                WHERE em.${DBConstants.MarkEntries.MARK_ENTRIES_MARK_ID_COL} = ?
                """,arrayOf(markId.toString()))

                if (cursor.moveToFirst()) {
                    do{
                        try{
                            val entry = Entry().apply {
                                update(
                                    id = cursor.getLong(0),
                                    title = cursor.getString(1),
                                    icon = cursor.getString(3),
                                    cover = cursor.getString(4),
                                )
                            }
                            entryList.add(entry)
                        }catch(e: Exception){
                            Log.e("MarkRepository", "Error processing entry row", e)
                        }
                    }while(cursor.moveToNext())
                }
                cursor.close()
            }
        } catch(e: Exception) {
        }

        return entryList
    }

    fun getAllMarks(): List<Mark> {
        val db = dbHandler.readableDatabase
        val markList = mutableListOf<Mark>()

        try {
            db.transaction {
                val cursor: Cursor = rawQuery(
                    "SELECT * FROM ${DBConstants.Marks.MARK_TABLE_NAME} ORDER BY ${DBConstants.Marks.MARK_CREATED_COL} DESC",
                    null
                )

                if (cursor.moveToFirst()) {
                    do {
                        try {
                            val mark = Mark().apply {
                                update(
                                    id = cursor.getLong(0),
                                    isBuffer = cursor.getInt(1) == 1,
                                    title = cursor.getString(2) ?: "Untitled",
                                    desc = cursor.getString(3) ?: "",
                                    feature = try {
                                        Feature.fromJson(cursor.getString(4))
                                    } catch (e: Exception) {
                                        Log.w("MarkRepository", "Invalid GeoJSON for mark ${cursor.getLong(0)}", e)
                                        Feature.fromGeometry(null)
                                    },
                                    iconImage = cursor.getString(5),
                                    coverImage = cursor.getString(6),
                                    entries = getEntries(cursor.getLong(0))
                                )
                            }
                            markList.add(mark)
                        } catch (e: Exception) {
                            Log.e("MarkRepository", "Error processing mark row", e)
                        }
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.e("MarkRepository", "Error getting all marks", e)
        }

        Log.d("MarkRepository", "Loaded ${markList.size} marks from database")
        return markList
    }

    fun clearBufferMarks(): Int {
        val db = dbHandler.writableDatabase
        var deletedCount = 0

        try {
            db.transaction {
                // Delete mark-entries relationships for buffer marks first
                delete(
                    DBConstants.MarkEntries.MARK_ENTRIES_TABLE_NAME,
                    "${DBConstants.MarkEntries.MARK_ENTRIES_MARK_ID_COL} IN (SELECT ${DBConstants.Marks.MARK_ID_COL} FROM ${DBConstants.Marks.MARK_TABLE_NAME} WHERE ${DBConstants.Marks.MARK_BUFFER_COL} = 1)",
                    null
                )

                // Delete buffer marks
                deletedCount = delete(
                    DBConstants.Marks.MARK_TABLE_NAME,
                    "${DBConstants.Marks.MARK_BUFFER_COL} = ?",
                    arrayOf("1")
                )

                Log.d("MarkRepository", "Cleared $deletedCount buffer marks from database")
            }
        } catch (e: Exception) {
            Log.e("MarkRepository", "Error clearing buffer marks", e)
        }

        return deletedCount
    }

    fun updateMark(mark: Mark): Boolean {
        val db = dbHandler.writableDatabase
        var success = false

        try {
            db.transaction {
                val markValues = ContentValues().apply {
                    put(DBConstants.Marks.MARK_BUFFER_COL, if (mark.markIsBuffer) 1 else 0)
                    put(DBConstants.Marks.MARK_TITLE_COL, mark.markTitle)
                    put(DBConstants.Marks.MARK_DESC_COL, mark.markDescription)
                    put(DBConstants.Marks.MARK_GOEM_JSON_COL, mark.markFeature.toJson())
                    put(DBConstants.Marks.MARK_ICON_COL, mark.markIconImage)
                    put(DBConstants.Marks.MARK_COVER_IMG_COL, mark.markCoverImage)
                }

                val rowsUpdated = update(
                    DBConstants.Marks.MARK_TABLE_NAME,
                    markValues,
                    "${DBConstants.Marks.MARK_ID_COL} = ?",
                    arrayOf(mark.markId.toString())
                )

                success = rowsUpdated > 0
                Log.d("MarkRepository", "Updated mark ${mark.markId}, rows affected: $rowsUpdated")
            }
        } catch (e: Exception) {
            Log.e("MarkRepository", "Error updating mark ${mark.markId}", e)
            success = false
        }

        updateMarkEntries(mark)

        return success
    }

    fun updateMarkEntries(mark: Mark){
        val db = dbHandler.writableDatabase

        try {
            db.transaction {
                // Update mark<->entry relationships
                val currentEntries = getEntries(mark.markId).map { it.entryId }.toSet()
                val newEntries = mark.markEntries.map { it.entryId }.toSet()

                val entriesToRemove = currentEntries - newEntries
                val entriesToAdd = newEntries - currentEntries

                // Remove relationships that are no longer needed
                for (entryId in entriesToRemove) {
                    delete(
                        DBConstants.MarkEntries.MARK_ENTRIES_TABLE_NAME,
                        "${DBConstants.MarkEntries.MARK_ENTRIES_MARK_ID_COL} = ? AND ${DBConstants.MarkEntries.MARK_ENTRIES_ENTRY_ID_COL} = ?",
                        arrayOf(mark.markId.toString(), entryId.toString())
                    )
                }

                // Add new relationships
                for (entryId in entriesToAdd) {
                    val junctionValues = ContentValues().apply {
                        put(DBConstants.MarkEntries.MARK_ENTRIES_MARK_ID_COL, mark.markId)
                        put(DBConstants.MarkEntries.MARK_ENTRIES_ENTRY_ID_COL, entryId)
                    }
                    insert(DBConstants.MarkEntries.MARK_ENTRIES_TABLE_NAME, null, junctionValues)
                }
            }
        }catch (e: Exception) {
            Log.e("MarkRepository", "Error updating mark relationships ${mark.markId}", e)
        }
    }

    fun deleteMark(markId: Long): Boolean {
        val db = dbHandler.writableDatabase
        var success = false

        try {
            db.transaction {
                // Delete mark-entries relationships first
                delete(
                    DBConstants.MarkEntries.MARK_ENTRIES_TABLE_NAME,
                    "${DBConstants.MarkEntries.MARK_ENTRIES_MARK_ID_COL} = ?",
                    arrayOf(markId.toString())
                )

                // Delete the mark
                val rowsDeleted = delete(
                    DBConstants.Marks.MARK_TABLE_NAME,
                    "${DBConstants.Marks.MARK_ID_COL} = ?",
                    arrayOf(markId.toString())
                )

                success = rowsDeleted > 0
                Log.d("MarkRepository", "Deleted mark $markId, rows affected: $rowsDeleted")
            }
        } catch (e: Exception) {
            Log.e("MarkRepository", "Error deleting mark $markId", e)
            success = false
        }

        return success
    }

    // Convenience method for deleting by Mark object
    fun deleteMark(mark: Mark): Boolean = deleteMark(mark.markId)
}

class EntryRepository(private val dbHandler: MyDBHandler) {
    fun addNewEntry(entry : Entry): Long
    {
        val db = dbHandler.writableDatabase
        var entryId = entry.entryId

        return try {
            db.transaction {
                val entryValues = ContentValues().apply {
                    put(DBConstants.Entries.ENTRY_TITLE_COL, entry.entryTitle)
                    put(DBConstants.Entries.ENTRY_ICON_COL, entry.entryIcon)
                    put(DBConstants.Entries.ENTRY_COVER_IMG_COL, entry.entryCover)
                }

                entryId = insert(DBConstants.Entries.ENTRY_TABLE_NAME, null, entryValues)

                // Insert mark<->entries relationships
                for (mark in entry.getMarks()) {
                    val junctionValues = ContentValues().apply {
                        put(DBConstants.MarkEntries.MARK_ENTRIES_MARK_ID_COL, mark.markId)
                        put(DBConstants.MarkEntries.MARK_ENTRIES_ENTRY_ID_COL, entryId)
                    }
                    insert(DBConstants.MarkEntries.MARK_ENTRIES_TABLE_NAME, null, junctionValues)
                }
            }

            entryId
        } catch (e: Exception) {
            Log.e("EntryRepository", "Error adding entry", e)
            -1
        }
    }

    fun getAllEntries(): List<Entry> {
        val db = dbHandler.writableDatabase
        val entryList = mutableListOf<Entry>()

        try {
            db.transaction {
                val cursor: Cursor = rawQuery(
                    "SELECT * FROM ${DBConstants.Entries.ENTRY_TABLE_NAME} ORDER BY ${DBConstants.Entries.ENTRY_CREATED_COL} DESC",
                    null
                )

                if (cursor.moveToFirst()) {
                    do {
                        try {
                            val entry = Entry().apply {
                                update(
                                    id = cursor.getLong(0),
                                    title = cursor.getString(1),
                                    icon = cursor.getString(3),
                                    cover = cursor.getString(4)
                                )
                            }
                            entryList.add(entry)
                        } catch (e: Exception) {
                            Log.e("EntryRepository", "Error processing entry row", e)
                        }
                    }while (cursor.moveToNext())
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.e("EntryRepository", "Error getting all entries", e)
        }

        Log.d("EntryRepository", "Loaded ${entryList.size} entries from database")
        return entryList
    }

    fun getMarks(entryId : Long): List<Mark> {
        val db = dbHandler.writableDatabase
        val markList = mutableListOf<Mark>()

        try{
            db.transaction {
                val cursor: Cursor = rawQuery("""
                    SELECT m.* FROM ${DBConstants.Marks.MARK_TABLE_NAME} m
                    INNER JOIN ${DBConstants.MarkEntries.MARK_ENTRIES_TABLE_NAME} me 
                    ON m.${DBConstants.Marks.MARK_ID_COL} = me.${DBConstants.MarkEntries.MARK_ENTRIES_MARK_ID_COL}
                    WHERE me.${DBConstants.MarkEntries.MARK_ENTRIES_ENTRY_ID_COL} = ?
                    """,arrayOf(entryId.toString()))

                if (cursor.moveToFirst()) {
                    do{
                        try {
                            val mark = Mark().apply {
                                update(
                                    id = cursor.getLong(0),
                                    isBuffer = cursor.getInt(1) == 1,
                                    title = cursor.getString(2) ?: "Untitled",
                                    desc = cursor.getString(3) ?: "",
                                    feature = try {
                                        Feature.fromJson(cursor.getString(4))
                                    } catch (e: Exception) {
                                        Log.w("EntryRepository", "Invalid GeoJSON for mark ${cursor.getLong(0)}", e)
                                        Feature.fromGeometry(null)
                                    },
                                    iconImage = cursor.getString(5),
                                    coverImage = cursor.getString(6)
                                )
                            }
                            markList.add(mark)
                        } catch(e: Exception){
                            Log.e("EntryRepository", "Error processing mark row", e)
                        }
                    }while(cursor.moveToNext())
                }
                cursor.close()
            }
        }catch(e: Exception){
            Log.e("EntryRepository", "Error getting all marks for entry ${entryId}", e)
        }

        Log.d("EntryRepository", "Loaded ${markList.size} marks for entry ${entryId} from database")
        return markList
    }

    fun updateEntry(entry : Entry) : Boolean {
        val db = dbHandler.writableDatabase
        var success = false

        try {
            db.transaction {
                val entryValues = ContentValues().apply {
                    put(DBConstants.Entries.ENTRY_TITLE_COL, entry.entryTitle)
                    put(DBConstants.Entries.ENTRY_ICON_COL, entry.entryIcon)
                    put(DBConstants.Entries.ENTRY_COVER_IMG_COL, entry.entryCover)
                }

                val rowsUpdated = update(
                    DBConstants.Entries.ENTRY_TABLE_NAME,
                    entryValues,
                    "${DBConstants.Entries.ENTRY_ID_COL} = ?",
                    arrayOf(entry.entryId.toString())
                )

                success = rowsUpdated > 0

                Log.d("EntryRepository", "Updated entry ${entry.entryId}, rows affected: $rowsUpdated")
            }
        }catch (e: Exception) {
            Log.e("EntryRepository", "Error updating entry ${entry.entryId}", e)
            success = false
        }

        updateEntryMarks(entry)

        return success
    }

    fun updateEntryMarks(entry : Entry) {
        val db = dbHandler.writableDatabase

        try {
            db.transaction {
                // Get current mark IDs for this entry
                val currentMarkIds = getMarks(entry.entryId).map { it.markId }.toSet()
                val newMarkIds = entry.getMarks().map { it.markId }.toSet()

                // Find marks to remove and add
                val marksToRemove = currentMarkIds - newMarkIds
                val marksToAdd = newMarkIds - currentMarkIds

                // Remove relationships that are no longer needed
                for (markId in marksToRemove) {
                    delete(
                        DBConstants.MarkEntries.MARK_ENTRIES_TABLE_NAME,
                        "${DBConstants.MarkEntries.MARK_ENTRIES_MARK_ID_COL} = ? AND ${DBConstants.MarkEntries.MARK_ENTRIES_ENTRY_ID_COL} = ?",
                        arrayOf(markId.toString(), entry.entryId.toString())
                    )
                }

                // Add new relationships
                for (markId in marksToAdd) {
                    val junctionValues = ContentValues().apply {
                        put(DBConstants.MarkEntries.MARK_ENTRIES_MARK_ID_COL, markId)
                        put(DBConstants.MarkEntries.MARK_ENTRIES_ENTRY_ID_COL, entry.entryId)
                    }
                    insert(DBConstants.MarkEntries.MARK_ENTRIES_TABLE_NAME, null, junctionValues)
                }
            }
        }catch (e: Exception) {
            Log.e("EntryRepository", "Error updating entry relationships ${entry.entryId}", e)
        }
    }

    fun deleteEntry(entryId : Long) : Boolean {
        val db = dbHandler.writableDatabase
        var success = false

        try {
            db.transaction {
                // Delete mark-entries relationships first
                delete(
                    DBConstants.MarkEntries.MARK_ENTRIES_TABLE_NAME,
                    "${DBConstants.MarkEntries.MARK_ENTRIES_ENTRY_ID_COL} = ?",
                    arrayOf(entryId.toString())
                )

                // Delete the entry
                val rowsDeleted = delete(
                    DBConstants.Entries.ENTRY_TABLE_NAME,
                    "${DBConstants.Entries.ENTRY_ID_COL} = ?",
                    arrayOf(entryId.toString())
                )

                success = rowsDeleted > 0
                Log.d("EntryRepository", "Deleted entry $entryId, rows affected: $rowsDeleted")
            }
        } catch(e : Exception) {
            Log.e("EntryRepository", "Error deleting entry $entryId", e)
            success = false
        }

        return success
    }

    fun deleteEntry(entry : Entry) : Boolean = deleteEntry(entry.entryId)
}

class EntryItemRepository(private val dbHandler: MyDBHandler) {
    fun addNewEntryItem(entryItem: EntryItem, entryId: Long): Long {
        val db = dbHandler.writableDatabase
        var entryItemId = entryItem.entryItemId

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
                            put(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL, entryItem.textContent)
                            put(DBConstants.EntryItems.ENTRY_ITEM_LOCAL_URI_COL, entryItem.localUri)
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
                                        entryItemId = cursor.getLong(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_ID_COL)),
                                        entryId = cursor.getLong(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_ENTRY_ID_COL)),
                                        textContent = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL))
                                    )
                                }
                                DBConstants.EntryItems.ENTRY_ITEM_IMAGE_TYPE -> {
                                    EntryItem.ImageItem(
                                        entryItemId = cursor.getLong(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_ID_COL)),
                                        entryId = cursor.getLong(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_ENTRY_ID_COL)),
                                        textContent = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL)),
                                        localUri = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_LOCAL_URI_COL)),
                                        remoteUrl = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_REMOTE_URL_COL))
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
                                        entryItemId = cursor.getLong(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_ID_COL)),
                                        entryId = entryId,
                                        textContent = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL))
                                    )
                                }
                                DBConstants.EntryItems.ENTRY_ITEM_IMAGE_TYPE -> {
                                    EntryItem.ImageItem(
                                        entryItemId = cursor.getLong(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_ID_COL)),
                                        entryId = entryId,
                                        textContent = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL)),
                                        localUri = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_LOCAL_URI_COL)),
                                        remoteUrl = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.EntryItems.ENTRY_ITEM_REMOTE_URL_COL))
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
                    put(DBConstants.EntryItems.ENTRY_ITEM_ENTRY_ID_COL, entryId) // set the entryItemId for the reference entry
                    when(entryItem) {
                        is EntryItem.TextItem -> {
                            put(DBConstants.EntryItems.ENTRY_ITEM_TYPE_COL, DBConstants.EntryItems.ENTRY_ITEM_TEXT_TYPE)
                            put(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL, entryItem.textContent)
                        }
                        is EntryItem.ImageItem -> {
                            put(DBConstants.EntryItems.ENTRY_ITEM_TYPE_COL, DBConstants.EntryItems.ENTRY_ITEM_IMAGE_TYPE)
                            put(DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL, entryItem.textContent)
                            put(DBConstants.EntryItems.ENTRY_ITEM_LOCAL_URI_COL, entryItem.localUri)
                        }
                    }
                }

                val rowsUpdated = update(
                    DBConstants.EntryItems.ENTRY_ITEM_TABLE_NAME,
                    entryItemValues,
                    "${DBConstants.EntryItems.ENTRY_ITEM_ID_COL} = ?",
                    arrayOf(entryItem.entryItemId.toString())
                )

                success = rowsUpdated > 0
                Log.d("EntryItemRepository", "Updated entryItem ${entryItem.entryItemId}, rows affected: $rowsUpdated")
            }
        } catch (e: Exception) {
            Log.e("EntryItemRepository", "Error updating entryItem ${entryItem.entryItemId}", e)
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

    fun deleteEntryItem(entryItem : EntryItem) : Boolean = deleteEntryItem(entryItem.entryItemId)
}

class MyDBHandler(context : Context?) : SQLiteOpenHelper(context, DBConstants.DB_NAME, null, DBConstants.DB_VERSION){
    override fun onCreate(db: SQLiteDatabase) {
        try {
            // Create marks table with fixed column type
            val marksTableQuery = """
                CREATE TABLE ${DBConstants.Marks.MARK_TABLE_NAME} (
                    ${DBConstants.Marks.MARK_ID_COL} INTEGER PRIMARY KEY AUTOINCREMENT,
                    ${DBConstants.Marks.MARK_BUFFER_COL} INTEGER DEFAULT 0,
                    ${DBConstants.Marks.MARK_TITLE_COL} TEXT,
                    ${DBConstants.Marks.MARK_DESC_COL} TEXT,
                    ${DBConstants.Marks.MARK_GOEM_JSON_COL} TEXT,
                    ${DBConstants.Marks.MARK_ICON_COL} TEXT,
                    ${DBConstants.Marks.MARK_COVER_IMG_COL} TEXT,
                    ${DBConstants.Marks.MARK_CREATED_COL} TEXT DEFAULT (datetime('now')),
                    ${DBConstants.Marks.MARK_UPDATED_COL} TEXT DEFAULT (datetime('now'))
                )
            """.trimIndent()

            // Create trigger for marks table
            val marksTableUpdateTriggerQuery = """
                CREATE TRIGGER marks_set_updated_timestamp
                AFTER UPDATE ON ${DBConstants.Marks.MARK_TABLE_NAME}
                FOR EACH ROW
                BEGIN
                    UPDATE ${DBConstants.Marks.MARK_TABLE_NAME} 
                    SET ${DBConstants.Marks.MARK_UPDATED_COL} = datetime('now') 
                    WHERE ${DBConstants.Marks.MARK_ID_COL} = NEW.${DBConstants.Marks.MARK_ID_COL};
                END
            """.trimIndent()

            // Create entry table
            val entriesTableQuery = """
                CREATE TABLE ${DBConstants.Entries.ENTRY_TABLE_NAME} (
                    ${DBConstants.Entries.ENTRY_ID_COL} INTEGER PRIMARY KEY AUTOINCREMENT,
                    ${DBConstants.Entries.ENTRY_TITLE_COL} TEXT,
                    ${DBConstants.Entries.ENTRY_ICON_COL} TEXT,
                    ${DBConstants.Entries.ENTRY_COVER_IMG_COL} TEXT,
                    ${DBConstants.Entries.ENTRY_CREATED_COL} TEXT DEFAULT (datetime('now')),
                    ${DBConstants.Entries.ENTRY_UPDATED_COL} TEXT DEFAULT (datetime('now'))
                )
            """.trimIndent()

            // Create trigger for entries table
            val entriesTableUpdateTriggerQuery = """
                CREATE TRIGGER entries_set_updated_timestamp
                AFTER UPDATE ON ${DBConstants.Entries.ENTRY_TABLE_NAME}
                FOR EACH ROW
                BEGIN
                    UPDATE ${DBConstants.Entries.ENTRY_TABLE_NAME} 
                    SET ${DBConstants.Entries.ENTRY_UPDATED_COL} = datetime('now') 
                    WHERE ${DBConstants.Entries.ENTRY_ID_COL} = NEW.${DBConstants.Entries.ENTRY_ID_COL};
                END
            """.trimIndent()

            val entryItemsTableQuery = """
                CREATE TABLE ${DBConstants.EntryItems.ENTRY_ITEM_TABLE_NAME} (
                    ${DBConstants.EntryItems.ENTRY_ITEM_ID_COL} INTEGER PRIMARY KEY AUTOINCREMENT,
                    ${DBConstants.EntryItems.ENTRY_ITEM_ENTRY_ID_COL} INTEGER NOT NULL,
                    ${DBConstants.EntryItems.ENTRY_ITEM_TYPE_COL} TEXT NOT NULL,
                    ${DBConstants.EntryItems.ENTRY_ITEM_TEXT_CONTENT_COL} TEXT,
                    ${DBConstants.EntryItems.ENTRY_ITEM_LOCAL_URI_COL} TEXT,
                    ${DBConstants.EntryItems.ENTRY_ITEM_REMOTE_URL_COL} TEXT,
                    ${DBConstants.EntryItems.ENTRY_ITEM_CREATED_COL} TEXT DEFAULT (datetime('now')),
                    ${DBConstants.EntryItems.ENTRY_ITEM_UPDATED_COL} TEXT DEFAULT (datetime('now')),
                    FOREIGN KEY (${DBConstants.EntryItems.ENTRY_ITEM_ENTRY_ID_COL}) REFERENCES ${DBConstants.Entries.ENTRY_TABLE_NAME}(${DBConstants.Entries.ENTRY_ID_COL}) ON DELETE CASCADE
                )
            """.trimIndent()

            // Create trigger for entries table
            val entryItemsTableUpdateTriggerQuery = """
                CREATE TRIGGER entry_items_set_updated_timestamp
                AFTER UPDATE ON ${DBConstants.EntryItems.ENTRY_ITEM_TABLE_NAME}
                FOR EACH ROW
                BEGIN
                    UPDATE ${DBConstants.EntryItems.ENTRY_ITEM_TABLE_NAME} 
                    SET ${DBConstants.EntryItems.ENTRY_ITEM_UPDATED_COL} = datetime('now') 
                    WHERE ${DBConstants.EntryItems.ENTRY_ITEM_ID_COL} = NEW.${DBConstants.EntryItems.ENTRY_ITEM_ID_COL};
                END
            """.trimIndent()

            // Create junction table with correct foreign key names
            val markEntriesTableQuery = """
                CREATE TABLE ${DBConstants.MarkEntries.MARK_ENTRIES_TABLE_NAME} (
                    ${DBConstants.MarkEntries.MARK_ENTRIES_MARK_ID_COL} INTEGER NOT NULL,
                    ${DBConstants.MarkEntries.MARK_ENTRIES_ENTRY_ID_COL} INTEGER NOT NULL,
                    PRIMARY KEY (${DBConstants.MarkEntries.MARK_ENTRIES_MARK_ID_COL}, ${DBConstants.MarkEntries.MARK_ENTRIES_ENTRY_ID_COL}),
                    FOREIGN KEY (${DBConstants.MarkEntries.MARK_ENTRIES_MARK_ID_COL}) REFERENCES ${DBConstants.Marks.MARK_TABLE_NAME}(${DBConstants.Marks.MARK_ID_COL}) ON DELETE CASCADE,
                    FOREIGN KEY (${DBConstants.MarkEntries.MARK_ENTRIES_ENTRY_ID_COL}) REFERENCES ${DBConstants.Entries.ENTRY_TABLE_NAME}(${DBConstants.Entries.ENTRY_ID_COL}) ON DELETE CASCADE
                )
            """.trimIndent()

            // Execute all queries
            db.execSQL(marksTableQuery)
            db.execSQL(marksTableUpdateTriggerQuery)
            db.execSQL(entriesTableQuery)
            db.execSQL(entriesTableUpdateTriggerQuery)
            db.execSQL(entryItemsTableQuery)
            db.execSQL(entryItemsTableUpdateTriggerQuery)
            db.execSQL(markEntriesTableQuery)

            Log.d("MyDBHandler", "Database created successfully")

        } catch (e: Exception) {
            Log.e("MyDBHandler", "Error creating database", e)
            throw e
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            Log.d("MyDBHandler", "Upgrading database from version $oldVersion to $newVersion")

            // Drop all tables in correct order (respecting foreign key constraints)
            db.execSQL("DROP TABLE IF EXISTS ${DBConstants.MarkEntries.MARK_ENTRIES_TABLE_NAME}")
            db.execSQL("DROP TABLE IF EXISTS ${DBConstants.Entries.ENTRY_TABLE_NAME}")
            db.execSQL("DROP TABLE IF EXISTS ${DBConstants.Marks.MARK_TABLE_NAME}")

            // Recreate database
            onCreate(db)

        } catch (e: Exception) {
            Log.e("MyDBHandler", "Error upgrading database", e)
            throw e
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON")
        // Enable WAL mode for better concurrency
//        db.execSQL("PRAGMA journal_mode=WAL")
    }
}

object DBConstants {
    const val DB_NAME = "mark_db"
    const val DB_VERSION = 8 // Increment version to trigger upgrade

    object Marks {
        const val MARK_TABLE_NAME = "marks"
        const val MARK_ID_COL = "id"
        const val MARK_BUFFER_COL = "is_buffer"
        const val MARK_TITLE_COL = "title"
        const val MARK_DESC_COL = "description"
        const val MARK_GOEM_JSON_COL = "geom_json"
        const val MARK_ICON_COL = "icon_url"
        const val MARK_COVER_IMG_COL = "cover_image_url"
        const val MARK_CREATED_COL = "created"
        const val MARK_UPDATED_COL = "updated"
    }

    object Entries {
        const val ENTRY_TABLE_NAME = "entries"
        const val ENTRY_ID_COL = "id"
        const val ENTRY_TITLE_COL = "title"
        const val ENTRY_ICON_COL = "icon_url"
        const val ENTRY_COVER_IMG_COL = "cover_image_url"
        const val ENTRY_CREATED_COL = "created"
        const val ENTRY_UPDATED_COL = "updated"
    }

    object EntryItems {
        const val ENTRY_ITEM_TABLE_NAME = "entry_items"
        const val ENTRY_ITEM_ID_COL = "entry_item_id"
        const val ENTRY_ITEM_ENTRY_ID_COL = "entry_id"
        const val ENTRY_ITEM_TYPE_COL = "type" // IMAGE, TEXT, AUDIO, SKETCH
        const val ENTRY_ITEM_TEXT_TYPE = "TEXT"
        const val ENTRY_ITEM_IMAGE_TYPE = "IMAGE"
        const val ENTRY_ITEM_TEXT_CONTENT_COL = "text_content"
        const val ENTRY_ITEM_LOCAL_URI_COL = "local_uri"
        const val ENTRY_ITEM_REMOTE_URL_COL = "remote_url"
        const val ENTRY_ITEM_CREATED_COL = "created"
        const val ENTRY_ITEM_UPDATED_COL = "updated"
    }

    object MarkEntries {
        const val MARK_ENTRIES_TABLE_NAME = "mark_entries"
        const val MARK_ENTRIES_MARK_ID_COL = "mark_id"
        const val MARK_ENTRIES_ENTRY_ID_COL = "entry_id"
    }
}