package com.example.markerclient.domain.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.markerclient.db.MarkRepository
import com.example.markerclient.db.MyDBHandler
import com.example.markerclient.domain.Mark
import com.mapbox.geojson.Feature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MarkViewModel(application: Application) : AndroidViewModel(application) {
    private val markRepo = MarkRepository(MyDBHandler(application))

    // Single source of truth
    private val _allMarks = MutableStateFlow<List<Mark>>(emptyList())

    // Buffer marks (isBuffer = true)
    val bufferMarks: StateFlow<List<Mark>> = _allMarks.map { marks ->
        marks.filter { it.markIsBuffer }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Regular marks (isBuffer = false)
    val marks: StateFlow<List<Mark>> = _allMarks.map { marks ->
        marks.filter { !it.markIsBuffer }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Buffer mark features for map display
    val bufferMarkFeatures: StateFlow<List<Feature>> = bufferMarks.map { marks ->
        marks.mapNotNull { mark ->
            try {
                mark.markFeature.apply{
                    addNumberProperty(Mark.Companion.getDefaultFeatureMarkIdKey(), mark.markId) // Set markId so we know what mark to reference for updates
                }
            } catch (e: Exception) {
                Log.w("MarkViewModel", "Invalid feature for buffer mark ${mark.markId}", e)
                null
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Regular mark features for map display
    val markFeatures: StateFlow<List<Feature>> = marks.map { marks ->
        marks.mapNotNull { mark ->
            try {
                mark.markFeature.apply{
                    addNumberProperty("markId", mark.markId) // Set markId so we know what mark to reference for updates
                }
            } catch (e: Exception) {
                Log.w("MarkViewModel", "Invalid feature for mark ${mark.markId}", e)
                null
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = markRepo.clearBufferMarks()
                Log.d("MarkViewModel", "Cleared all buffer Marks $res")
            } catch (e: Exception) {
                Log.e("MarkViewModel", "Error clearing buffer marks", e)
            }
        }
        loadMarks()
    }

    fun loadMarks() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allMarks = markRepo.getAllMarks()
                _allMarks.value = allMarks
                Log.d("MarkViewModel", "Loaded ${allMarks.size} marks")
            } catch (e: Exception) {
                Log.e("MarkViewModel", "Error loading marks", e)
            }
        }
    }

    fun addMark(mark: Mark, onComplete: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
               val markId = markRepo.addNewMark(mark)
                if (markId > 0) {
                    Log.d("MarkViewModel", "Added mark with ID: $markId")
                    loadMarks() // Refresh from database
                    onComplete(markId)
                } else {
                    Log.e("MarkViewModel", "Failed to add mark")
                    onComplete(-1L)
                }
            } catch (e: Exception) {
                Log.e("MarkViewModel", "Error adding mark", e)
            }
        }
    }

    fun updateMark(mark: Mark) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = markRepo.updateMark(mark)
                if (success) {
                    Log.d("MarkViewModel", "Updated mark ${mark.markId}")
                    loadMarks() // Refresh from database
                } else {
                    Log.e("MarkViewModel", "Failed to update mark ${mark.markId}")
                }
            } catch (e: Exception) {
                Log.e("MarkViewModel", "Error updating mark ${mark.markId}", e)
            }
        }
    }

    fun deleteMark(mark: Mark) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = markRepo.deleteMark(mark)
                if (success) {
                    Log.d("MarkViewModel", "Deleted mark ${mark.markId}")
                    // Update local state immediately for responsiveness
                    _allMarks.value = _allMarks.value.filter { it.markId != mark.markId }
                } else {
                    Log.e("MarkViewModel", "Failed to delete mark ${mark.markId}")
                }
            } catch (e: Exception) {
                Log.e("MarkViewModel", "Error deleting mark ${mark.markId}", e)
            }
        }
    }

    // Convenience method to promote buffer mark to regular mark
    fun promoteBufferMarkToMark(mark: Mark) {
        if (!mark.markIsBuffer) {
            Log.w("MarkViewModel", "Trying to promote non-buffer mark ${mark.markId}")
            return
        }

        mark.update(isBuffer = false)
        updateMark(mark)
    }

    // Convenience method to get mark by ID
    fun getMarkById(id: Long): Mark? {
        return _allMarks.value.find { it.markId == id }
    }

    // Debugging methods
    fun getAllMarks(): List<Mark> = marks.value
    fun getAllBufferMarks(): List<Mark> = bufferMarks.value

    fun debugPrintMarks() {
        Log.d("MarkViewModel", "=== DEBUG MARKS ===")
        Log.d("MarkViewModel", "All marks: ${_allMarks.value.size}")
        Log.d("MarkViewModel", "Buffer marks: ${bufferMarks.value.size}")
        Log.d("MarkViewModel", "Regular marks: ${marks.value.size}")
        Log.d("MarkViewModel", "Buffer features: ${bufferMarkFeatures.value.size}")
        Log.d("MarkViewModel", "Mark features: ${markFeatures.value.size}")

        _allMarks.value.forEach { mark ->
            Log.d("MarkViewModel", "Mark ${mark.markId}: isBuffer=${mark.markIsBuffer}, title=${mark.markTitle}")
        }
    }

}