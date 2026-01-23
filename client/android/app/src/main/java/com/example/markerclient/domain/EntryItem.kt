package com.example.markerclient.domain

sealed class EntryItem {
    abstract val entryItemId: Long
    abstract val entryId: Long
    // Add created, updated and isSynced

    data class TextItem(
        override val entryItemId: Long,
        override val entryId: Long,
        val textContent: String = ""
    ) : EntryItem()

    data class ImageItem(
        override val entryItemId: Long,
        override val entryId: Long,
        val textContent: String = "",
        val localUri: String? = null,
        val remoteUrl: String? = null,
    ) : EntryItem()

    override fun equals(other : Any?) : Boolean {
        return (other is EntryItem) && (this.entryItemId == other.entryItemId)
    }

    override fun hashCode(): Int {
        return entryItemId.hashCode()
    }
}