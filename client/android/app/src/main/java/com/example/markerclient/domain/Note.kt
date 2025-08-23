package com.example.markerclient.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mapbox.geojson.Feature

class Note {
    var noteId by mutableLongStateOf(-1)
        private set
    var noteTitle by mutableStateOf("")
        private set
    var noteBody by mutableStateOf("")
        private set
    var noteIcon by mutableStateOf("")
        private set
    var noteCover by mutableStateOf("")
        private set
    var noteMarks by mutableStateOf<List<Mark?>>(emptyList())
        private set

    fun update(id: Long = noteId,
               title : String = noteTitle,
               body : String = noteBody,
               icon : String = noteIcon,
               cover : String = noteCover,
               marks : List<Mark?> = noteMarks
    ) : Note {
        noteId = id
        noteTitle = title
        noteBody = body
        noteIcon = icon
        noteCover = cover
        noteMarks = marks

        return this
    }
}