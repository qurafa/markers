package com.example.markerclient.data.repositories

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import androidx.core.database.sqlite.transaction
import com.example.markerclient.data.dao.EntryDao
import com.example.markerclient.data.db.DBConstants
import com.example.markerclient.data.db.MyDBHandler
import com.example.markerclient.domain.model.Entry
import com.example.markerclient.domain.model.Mark
import com.mapbox.geojson.Feature

class EntryRepository(private val entryDao: EntryDao) {
    fun addNewEntry(title: String): Long {
        return entryDao.addNewEntry(
            Entry().update(title = title)
        )
    }

    fun getEntry(id: Long): Entry? {
        return entryDao.getEntry(id);
    }

    fun getAllEntries(): List<Entry> {
        return entryDao.getAllEntries()
    }

    fun getMarks(entryId : Long): List<Mark> {
        return entryDao.getMarks(entryId);
    }

    fun updateEntry(entry : Entry) : Boolean {
        return entryDao.updateEntry(entry);
    }

    fun updateEntryMarks(entry : Entry) {
        entryDao.updateEntryMarks(entry);
    }

    fun deleteEntry(entryId : Long) : Boolean {
        return entryDao.deleteEntry(entryId);
    }

    fun deleteEntry(entry : Entry) : Boolean = deleteEntry(entry.entryId)
}
