package com.example.markerclient.domain.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mapbox.geojson.Feature

class Mark {
    var markId by mutableLongStateOf(getDefaultMarkId())
        private set
    var markIsBuffer by mutableStateOf(getDefaultMarkIsBuffer())
        private set
    var markTitle by mutableStateOf(getDefaultMarkTitle())
        private set
    var markDescription by mutableStateOf(getDefaultMarkDesc())
        private set
    var markFeature by mutableStateOf(getDefaultFeature())
        private set
    var markIconImage by mutableStateOf(getDefaultIconImage())
        private set
    var markCoverImage by mutableStateOf(getDefaultCoverImage())
        private set
    var markEntries by mutableStateOf(getDefaultEntries())
        private set

    fun update(id: Long = markId,
               isBuffer : Boolean = markIsBuffer,
               title : String = markTitle,
               desc : String = markDescription,
               feature : Feature = markFeature,
               iconImage : String = markIconImage,
               coverImage : String = markCoverImage,
               entries : List<Entry> = markEntries
   ) : Mark {
        markId = id
        markIsBuffer = isBuffer
        markTitle = title
        markDescription = desc
        markFeature = feature
        markIconImage = iconImage
        markCoverImage = coverImage
        markEntries = entries

        return this
    }

    companion object {
        private const val DEFAULT_MARK_ID : Long = -1
        private const val DEFAULT_MARK_IS_BUFFER = true
        private const val DEFAULT_MARK_TITLE = ""
        private const val DEFAULT_MARK_DESC = ""
        private const val DEFAULT_FEATURE_MARK_ID_KEY = "markId"
        private const val DEFAULT_FEATURE_TITLE_KEY = "title"
        private const val DEFAULT_FEATURE_DESC_KEY = "description"
        private val _defaultFeature = Feature.fromGeometry(null).apply {
            addNumberProperty(DEFAULT_FEATURE_MARK_ID_KEY, DEFAULT_MARK_ID)
            addStringProperty(DEFAULT_FEATURE_TITLE_KEY, DEFAULT_MARK_TITLE)
            addStringProperty(DEFAULT_FEATURE_DESC_KEY, DEFAULT_MARK_DESC)
        }
        private const val DEFAULT_ICON_IMG = ""
        private const val DEFAULT_COVER_IMG = ""
        private val DEFAULT_ENTRIES = emptyList<Entry>()

        fun getDefaultMarkId() : Long {
            return DEFAULT_MARK_ID
        }
        fun getDefaultMarkIsBuffer() : Boolean {
            return DEFAULT_MARK_IS_BUFFER
        }
        fun getDefaultMarkTitle() : String {
            return DEFAULT_MARK_TITLE
        }
        fun getDefaultMarkDesc() : String {
            return DEFAULT_MARK_DESC
        }
        fun getDefaultFeatureMarkIdKey() : String {
            return DEFAULT_FEATURE_MARK_ID_KEY
        }

        fun getDefaultFeatureTitleKey() : String {
            return DEFAULT_FEATURE_TITLE_KEY
        }
        fun getDefaultFeatureDescKey() : String {
            return DEFAULT_FEATURE_DESC_KEY
        }
        fun getDefaultFeature() : Feature {
            return _defaultFeature
        }
        fun getDefaultIconImage() : String {
            return DEFAULT_ICON_IMG
        }
        fun getDefaultCoverImage() : String {
            return DEFAULT_COVER_IMG
        }
        fun getDefaultEntries() : List<Entry> {
            return DEFAULT_ENTRIES
        }
    }
}