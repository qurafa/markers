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

class MarkDao(private val dbHandler: MyDBHandler) {
    fun addNewMark(isBuffer: Boolean, title: String, desc: String, feature: Feature, icon: String, cover: String): Long {
        val db = dbHandler.writableDatabase
        var markId: Long = -1;

        return try {
            db.transaction {
                val markValues = ContentValues().apply {
                    put(DBConstants.Marks.MARK_BUFFER_COL, if (isBuffer) 1 else 0)
                    put(DBConstants.Marks.MARK_TITLE_COL, title)
                    put(DBConstants.Marks.MARK_DESC_COL, desc)
                    put(DBConstants.Marks.MARK_GOEM_JSON_COL, feature.toJson())
                    put(DBConstants.Marks.MARK_ICON_COL, icon)
                    put(DBConstants.Marks.MARK_COVER_IMG_COL, cover)
                }

                markId = insert(DBConstants.Marks.MARK_TABLE_NAME, null, markValues)

                // Insert mark<->entries relationships
//                for (entry in mark.markEntries) {
//                    val junctionValues = ContentValues().apply {
//                        put(DBConstants.MarkEntries.MARK_ENTRIES_MARK_ID_COL, markId)
//                        put(DBConstants.MarkEntries.MARK_ENTRIES_ENTRY_ID_COL, entry.entryId)
//                    }
//                    insert(DBConstants.MarkEntries.MARK_ENTRIES_TABLE_NAME, null, junctionValues)
//                }
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