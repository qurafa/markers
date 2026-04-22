package com.example.markerclient.domain.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Entry {
    var entryId by mutableLongStateOf(getDefaultEntryId())
        private set
    var entryTitle by mutableStateOf(getDefaultEntryTitle())
        private set
    var entryIcon by mutableStateOf(getDefaultEntryIcon())
        private set
    var entryCover by mutableStateOf(getDefaultEntryCover())
        private set

    fun update(
        id: Long = entryId,
        title : String = entryTitle,
        icon : String = entryIcon,
        cover : String = entryCover
    ) : Entry {
        entryId = id
        entryTitle = title
        entryIcon = icon
        entryCover = cover

        return this
    }

    fun addItem(item : EntryItem) : Entry {
        return this
    }

    fun getItems() : List<EntryItem> {
        return emptyList<EntryItem>()
    }

    fun getMarks() : List<Mark> {
        return emptyList<Mark>()
    }

    fun removeItem(item : EntryItem) : Entry {
        return this
    }

    companion object {
        private const val DEFAULT_ENTRY_ID : Long = -1
        private const val DEFAULT_ENTRY_TITLE : String = ""
        private const val DEFAULT_ENTRY_ICON : String = ""
        private const val DEFAULT_ENTRY_COVER : String = ""
        private val DEFAULT_ENTRY_ITEMS_ID : Long = -1

        fun getDefaultEntryId() : Long {
            return DEFAULT_ENTRY_ID
        }

        fun getDefaultEntryTitle() : String {
            return DEFAULT_ENTRY_TITLE
        }

        fun getDefaultEntryIcon() : String {
            return DEFAULT_ENTRY_ICON
        }

        fun getDefaultEntryCover() : String {
            return DEFAULT_ENTRY_COVER
        }

        fun getDefaultEntryItemsId() : Long {
            return DEFAULT_ENTRY_ITEMS_ID
        }

    }
}