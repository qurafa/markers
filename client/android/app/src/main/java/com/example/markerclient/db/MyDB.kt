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

class MarkRepository(private val dbHandler: MyDBHandler) {
    fun addNewMark(mark: Mark): Long {
        val db = dbHandler.writableDatabase
        var markId = mark.markId

        return try {
            db.transaction {
                val markValues = ContentValues().apply {
                    put(DBConstants.Marks.MARKS_BUFFER_COL, if (mark.markIsBuffer) 1 else 0)
                    put(DBConstants.Marks.MARKS_TITLE_COL, mark.markTitle)
                    put(DBConstants.Marks.MARKS_DESC_COL, mark.markDescription)
                    put(DBConstants.Marks.MARKS_GOEM_JSON_COL, mark.markFeature.toJson())
                    put(DBConstants.Marks.MARKS_ICON_COL, mark.markIconImage)
                    put(DBConstants.Marks.MARKS_COVER_IMG_COL, mark.markCoverImage)
                }

                markId = insert(DBConstants.Marks.MARKS_TABLE_NAME, null, markValues)

                // Insert mark<->notes relationships
                for (note in mark.markNotes) {
                    note?.let {
                        val junctionValues = ContentValues().apply {
                            put(DBConstants.MarkNotes.MARK_NOTES_MARK_ID_COL, markId)
                            put(DBConstants.MarkNotes.MARK_NOTES_NOTE_ID_COL, it.noteId)
                        }
                        insert(DBConstants.MarkNotes.MARK_NOTES_TABLE_NAME, null, junctionValues)
                    }
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
                    "SELECT * FROM ${DBConstants.Marks.MARKS_TABLE_NAME} WHERE ${DBConstants.Marks.MARKS_ID_COL} = ?",
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
                            coverImage = cursor.getString(6)
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

    fun getAllMarks(): List<Mark> {
        val db = dbHandler.readableDatabase
        val markList = mutableListOf<Mark>()

        try {
            db.transaction {
                val cursor: Cursor = rawQuery(
                    "SELECT * FROM ${DBConstants.Marks.MARKS_TABLE_NAME} ORDER BY ${DBConstants.Marks.MARKS_CREATED_COL} DESC",
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
                                    coverImage = cursor.getString(6)
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
                // Delete mark-notes relationships for buffer marks first
                delete(
                    DBConstants.MarkNotes.MARK_NOTES_TABLE_NAME,
                    "${DBConstants.MarkNotes.MARK_NOTES_MARK_ID_COL} IN (SELECT ${DBConstants.Marks.MARKS_ID_COL} FROM ${DBConstants.Marks.MARKS_TABLE_NAME} WHERE ${DBConstants.Marks.MARKS_BUFFER_COL} = 1)",
                    null
                )

                // Delete buffer marks
                deletedCount = delete(
                    DBConstants.Marks.MARKS_TABLE_NAME,
                    "${DBConstants.Marks.MARKS_BUFFER_COL} = ?",
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
                    put(DBConstants.Marks.MARKS_BUFFER_COL, if (mark.markIsBuffer) 1 else 0)
                    put(DBConstants.Marks.MARKS_TITLE_COL, mark.markTitle)
                    put(DBConstants.Marks.MARKS_DESC_COL, mark.markDescription)
                    put(DBConstants.Marks.MARKS_GOEM_JSON_COL, mark.markFeature.toJson())
                    put(DBConstants.Marks.MARKS_ICON_COL, mark.markIconImage)
                    put(DBConstants.Marks.MARKS_COVER_IMG_COL, mark.markCoverImage)
                }

                val rowsUpdated = update(
                    DBConstants.Marks.MARKS_TABLE_NAME,
                    markValues,
                    "${DBConstants.Marks.MARKS_ID_COL} = ?",
                    arrayOf(mark.markId.toString())
                )

                success = rowsUpdated > 0
                Log.d("MarkRepository", "Updated mark ${mark.markId}, rows affected: $rowsUpdated")
            }
        } catch (e: Exception) {
            Log.e("MarkRepository", "Error updating mark ${mark.markId}", e)
            success = false
        }

        return success
    }

    fun deleteMark(markId: Long): Boolean {
        val db = dbHandler.writableDatabase
        var success = false

        try {
            db.transaction {
                // Delete mark-notes relationships first
                delete(
                    DBConstants.MarkNotes.MARK_NOTES_TABLE_NAME,
                    "${DBConstants.MarkNotes.MARK_NOTES_MARK_ID_COL} = ?",
                    arrayOf(markId.toString())
                )

                // Delete the mark
                val rowsDeleted = delete(
                    DBConstants.Marks.MARKS_TABLE_NAME,
                    "${DBConstants.Marks.MARKS_ID_COL} = ?",
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

class MyDBHandler(context : Context?) : SQLiteOpenHelper(context, DBConstants.DB_NAME, null, DBConstants.DB_VERSION){
    override fun onCreate(db: SQLiteDatabase) {
        try {
            // Create marks table with fixed column type
            val marksTableQuery = """
                CREATE TABLE ${DBConstants.Marks.MARKS_TABLE_NAME} (
                    ${DBConstants.Marks.MARKS_ID_COL} INTEGER PRIMARY KEY AUTOINCREMENT,
                    ${DBConstants.Marks.MARKS_BUFFER_COL} INTEGER DEFAULT 0,
                    ${DBConstants.Marks.MARKS_TITLE_COL} TEXT,
                    ${DBConstants.Marks.MARKS_DESC_COL} TEXT,
                    ${DBConstants.Marks.MARKS_GOEM_JSON_COL} TEXT,
                    ${DBConstants.Marks.MARKS_ICON_COL} TEXT,
                    ${DBConstants.Marks.MARKS_COVER_IMG_COL} TEXT,
                    ${DBConstants.Marks.MARKS_CREATED_COL} TEXT DEFAULT (datetime('now')),
                    ${DBConstants.Marks.MARKS_UPDATED_COL} TEXT DEFAULT (datetime('now'))
                )
            """.trimIndent()

            // Create trigger for marks table
            val marksTableUpdateTriggerQuery = """
                CREATE TRIGGER marks_set_updated_timestamp
                AFTER UPDATE ON ${DBConstants.Marks.MARKS_TABLE_NAME}
                FOR EACH ROW
                BEGIN
                    UPDATE ${DBConstants.Marks.MARKS_TABLE_NAME} 
                    SET ${DBConstants.Marks.MARKS_UPDATED_COL} = datetime('now') 
                    WHERE ${DBConstants.Marks.MARKS_ID_COL} = NEW.${DBConstants.Marks.MARKS_ID_COL};
                END
            """.trimIndent()

            // Create notes table
            val notesTableQuery = """
                CREATE TABLE ${DBConstants.Notes.NOTES_TABLE_NAME} (
                    ${DBConstants.Notes.NOTES_ID_COL} INTEGER PRIMARY KEY AUTOINCREMENT,
                    ${DBConstants.Notes.NOTES_TITLE_COL} TEXT,
                    ${DBConstants.Notes.NOTES_BODY_COL} TEXT,
                    ${DBConstants.Notes.NOTES_ICON_COL} TEXT,
                    ${DBConstants.Notes.NOTES_COVER_IMG_COL} TEXT,
                    ${DBConstants.Notes.NOTES_CREATED_COL} TEXT DEFAULT (datetime('now')),
                    ${DBConstants.Notes.NOTES_UPDATED_COL} TEXT DEFAULT (datetime('now'))
                )
            """.trimIndent()

            // Create trigger for notes table
            val notesTableUpdateTriggerQuery = """
                CREATE TRIGGER notes_set_updated_timestamp
                AFTER UPDATE ON ${DBConstants.Notes.NOTES_TABLE_NAME}
                FOR EACH ROW
                BEGIN
                    UPDATE ${DBConstants.Notes.NOTES_TABLE_NAME} 
                    SET ${DBConstants.Notes.NOTES_UPDATED_COL} = datetime('now') 
                    WHERE ${DBConstants.Notes.NOTES_ID_COL} = NEW.${DBConstants.Notes.NOTES_ID_COL};
                END
            """.trimIndent()

            // Create junction table with correct foreign key names
            val markNotesTableQuery = """
                CREATE TABLE ${DBConstants.MarkNotes.MARK_NOTES_TABLE_NAME} (
                    ${DBConstants.MarkNotes.MARK_NOTES_MARK_ID_COL} INTEGER NOT NULL,
                    ${DBConstants.MarkNotes.MARK_NOTES_NOTE_ID_COL} INTEGER NOT NULL,
                    PRIMARY KEY (${DBConstants.MarkNotes.MARK_NOTES_MARK_ID_COL}, ${DBConstants.MarkNotes.MARK_NOTES_NOTE_ID_COL}),
                    FOREIGN KEY (${DBConstants.MarkNotes.MARK_NOTES_MARK_ID_COL}) REFERENCES ${DBConstants.Marks.MARKS_TABLE_NAME}(${DBConstants.Marks.MARKS_ID_COL}) ON DELETE CASCADE,
                    FOREIGN KEY (${DBConstants.MarkNotes.MARK_NOTES_NOTE_ID_COL}) REFERENCES ${DBConstants.Notes.NOTES_TABLE_NAME}(${DBConstants.Notes.NOTES_ID_COL}) ON DELETE CASCADE
                )
            """.trimIndent()

            // Execute all queries
            db.execSQL(marksTableQuery)
            db.execSQL(marksTableUpdateTriggerQuery)
            db.execSQL(notesTableQuery)
            db.execSQL(notesTableUpdateTriggerQuery)
            db.execSQL(markNotesTableQuery)

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
            db.execSQL("DROP TABLE IF EXISTS ${DBConstants.MarkNotes.MARK_NOTES_TABLE_NAME}")
            db.execSQL("DROP TABLE IF EXISTS ${DBConstants.Notes.NOTES_TABLE_NAME}")
            db.execSQL("DROP TABLE IF EXISTS ${DBConstants.Marks.MARKS_TABLE_NAME}")

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
    const val DB_VERSION = 4 // Increment version to trigger upgrade

    object Marks {
        const val MARKS_TABLE_NAME = "marks"
        const val MARKS_ID_COL = "id"
        const val MARKS_BUFFER_COL = "is_buffer"
        const val MARKS_TITLE_COL = "title"
        const val MARKS_DESC_COL = "description"
        const val MARKS_GOEM_JSON_COL = "geom_json"
        const val MARKS_ICON_COL = "icon_url"
        const val MARKS_COVER_IMG_COL = "cover_image_url"
        const val MARKS_CREATED_COL = "created"
        const val MARKS_UPDATED_COL = "updated"
    }

    object Notes {
        const val NOTES_TABLE_NAME = "notes"
        const val NOTES_ID_COL = "id"
        const val NOTES_TITLE_COL = "title"
        const val NOTES_BODY_COL = "body"
        const val NOTES_ICON_COL = "icon_url"
        const val NOTES_COVER_IMG_COL = "cover_image_url"
        const val NOTES_CREATED_COL = "created"
        const val NOTES_UPDATED_COL = "updated"
    }

    object MarkNotes {
        const val MARK_NOTES_TABLE_NAME = "mark_notes"
        const val MARK_NOTES_MARK_ID_COL = "mark_id"
        const val MARK_NOTES_NOTE_ID_COL = "note_id"
    }
}