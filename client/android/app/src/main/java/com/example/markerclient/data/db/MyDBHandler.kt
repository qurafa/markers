package com.example.markerclient.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class MyDBHandler private constructor(context: Context)
    : SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {
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

            // Create trigger for marks table for time of update
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

            // Create trigger for entries table for time of update
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

            // create entry items table
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

            // Create trigger for entry items table for time of update
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

            // Create junction table for marks and their related entries
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

    companion object {
        private const val DB_NAME = DBConstants.DB_NAME;
        private const val DB_VERSION = DBConstants.DB_VERSION;

        @Volatile
        private var INSTANCE: MyDBHandler? = null

        fun getInstance(context: Context): MyDBHandler {
            return INSTANCE ?: synchronized(this) {
                val instance = MyDBHandler(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}