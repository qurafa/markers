package com.example.markerclient.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import androidx.core.database.sqlite.transaction
import com.example.markerclient.data.db.DBConstants
import com.example.markerclient.data.db.MyDBHandler
import com.example.markerclient.domain.model.Entry
import com.example.markerclient.domain.model.Mark
import com.mapbox.geojson.Feature

class EntryDao(private val dbHandler: MyDBHandler) {
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

    fun getEntry(id: Long): Entry? {
        val db = dbHandler.readableDatabase
        var entry: Entry? = null

        try {
            db.transaction {
                val cursor: Cursor = rawQuery(
                    "SELECT * FROM ${DBConstants.Entries.ENTRY_TABLE_NAME} WHERE ${DBConstants.Entries.ENTRY_ID_COL} = ?",
                    arrayOf(id.toString())
                )

                if (cursor.moveToFirst()) {
                    entry = Entry().apply {
                        update(
                            id = cursor.getLong(0),
                            title = cursor.getString(1),
                            icon = cursor.getString(2),
                            cover = cursor.getString(3)
                        )
                    }
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.e("EntryDao", "Error getting entry $id", e)
        }

        return entry
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