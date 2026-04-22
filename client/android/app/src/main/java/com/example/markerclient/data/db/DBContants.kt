package com.example.markerclient.data.db

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