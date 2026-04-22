package com.example.markerclient.domain.model

sealed class EntryItem {
    abstract val id: Long
    abstract val entryId: Long
    // Add created, updated and isSynced

    data class TextItem(
        override val id: Long,
        override val entryId: Long,
        val textContent: String = ""
    ) : EntryItem()

    data class ImageItem(
        override val id: Long,
        override val entryId: Long,
        val caption: String = "",
        val imageUri: String
    ) : EntryItem()

    override fun equals(other : Any?) : Boolean {
        return (other is EntryItem) && (this.id == other.id) && (this.entryId == other.id)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}