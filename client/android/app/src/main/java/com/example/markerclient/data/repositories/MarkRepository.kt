package com.example.markerclient.data.repositories

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import androidx.core.database.sqlite.transaction
import com.example.markerclient.data.dao.EntryItemDao
import com.example.markerclient.data.dao.MarkDao
import com.example.markerclient.data.db.DBConstants
import com.example.markerclient.data.db.MyDBHandler
import com.example.markerclient.domain.model.Entry
import com.example.markerclient.domain.model.Mark
import com.mapbox.geojson.Feature

class MarkRepository(private val markDao: MarkDao) {
    fun addNewMark(
        isBuffer: Boolean = true,
        title: String = "",
        desc: String = "",
        feature: Feature,
        icon: String = "",
        cover: String = ""
    ): Long {
        return markDao.addNewMark(
            isBuffer,
            title,
            desc,
            feature,
            icon,
            cover
        )
    }

    fun getMark(id: Long): Mark? {
        return markDao.getMark(id);
    }

    fun getEntries(id: Long): List<Entry> {
        return markDao.getEntries(id);
    }

    fun getAllMarks(): List<Mark> {
        return markDao.getAllMarks()
    }

    fun clearBufferMarks(): Int {
        return markDao.clearBufferMarks();
    }

    fun updateMark(mark: Mark): Boolean {
        return markDao.updateMark(mark);
    }

    fun deleteMark(id: Long): Boolean {
        return markDao.deleteMark(id);
    }
}
