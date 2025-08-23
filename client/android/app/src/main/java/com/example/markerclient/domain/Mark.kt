package com.example.markerclient.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.interactions.FeatureState
import com.mapbox.maps.interactions.FeaturesetFeature

class Mark {
    var markId by mutableLongStateOf(getDefaultMarkId())
        private set
    var markIsBuffer by mutableStateOf(getDefaultMarkIsBuffer())
        private set
    var markTitle by mutableStateOf(getDefaultMarkTitle())
        private set
    var markDescription by mutableStateOf(getDefaultMarkDesc())
        private set
    var markFeature by mutableStateOf<Feature>(Feature.fromGeometry(null))
        private set
    var markIconImage by mutableStateOf("")
        private set
    var markCoverImage by mutableStateOf("")
        private set
    var markNotes by mutableStateOf<List<Note?>>(emptyList())
        private set

    fun update(id: Long = markId,
        isBuffer : Boolean = markIsBuffer,
        title : String = markTitle,
        desc : String = markDescription,
        feature : Feature = markFeature,
        iconImage : String = markIconImage,
        coverImage : String = markCoverImage,
        notes : List<Note?> = markNotes
   ) : Mark {
        markId = id
        markIsBuffer = isBuffer
        markTitle = title
        markDescription = desc
        markFeature = feature
        markIconImage = iconImage
        markCoverImage = coverImage
        markNotes = notes

        return this
    }

    companion object {
        private val _defaultMarkId : Long = -1
        private val _defaultMarkIsBuffer = true
        private val _defaultMarkTitle = "Title"
        private val _defaultMarkDesc = LoremIpsum(25).values.first()
        private val _defaultFeatureMarkIdKey = "markId"
        private val _defaultFeatureTitleKey = "title"
        private val _defaultFeatureDescKey = "description"

        fun getDefaultMarkId() : Long {
            return _defaultMarkId
        }
        fun getDefaultMarkIsBuffer() : Boolean {
            return _defaultMarkIsBuffer
        }
        fun getDefaultMarkTitle() : String {
            return _defaultMarkTitle
        }
        fun getDefaultMarkDesc() : String {
            return _defaultMarkDesc
        }
        fun getDefaultFeatureMarkIdKey() : String {
            return _defaultFeatureMarkIdKey
        }

        fun getDefaultFeatureTitleKey() : String {
            return _defaultFeatureTitleKey
        }
        fun getDefaultFeatureDescKey() : String {
            return _defaultFeatureDescKey
        }
    }
}