package com.example.markerclient.ui.viewModel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.markerclient.data.dao.EntryItemDao
import com.example.markerclient.data.db.MyDBHandler
import com.example.markerclient.data.repositories.EntryItemRepository
import com.example.markerclient.data.storage.ImageStorageManager
import com.example.markerclient.domain.model.Entry
import com.example.markerclient.domain.model.EntryItem
import com.mapbox.maps.extension.style.expressions.dsl.generated.image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EntryItemViewModel(application : Application, private val entry: Entry) : AndroidViewModel(application) {
    private val dbHandler = MyDBHandler.getInstance(application);
    private val entryItemDao = EntryItemDao(dbHandler);
    private val imageStorageManager = ImageStorageManager(application);
    private val repository = EntryItemRepository(entryItemDao, imageStorageManager);

    // All items
    private val _allEntryItems = MutableStateFlow<List<EntryItem>>(emptyList())

    val entryItems : StateFlow<List<EntryItem>> = _allEntryItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init { loadEntryItems() }

    fun loadEntryItems() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allEntryItems = repository.getAllEntryItems(entry.entryId)
                _allEntryItems.value = allEntryItems
                Log.d("EntryItemViewModel", "Loaded ${allEntryItems.size} entry items for entry ${entry.entryId}")
            } catch (e: Exception) {
                Log.e("EntryItemViewModel", "Error loading entry items ", e)
            }
        }
    }

    fun addImageItem(imageUri: Uri, caption: String = "",  onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.addImageItem(imageUri, entry.entryId, caption)
                    .onSuccess { id ->
                        Log.d("EntryItemViewModel", "Added entryItem with ID: ${id}")
                        loadEntryItems()
                        onComplete(id)
                    }
                    .onFailure {
                        Log.d("EntryItemViewModel", "Failed to add new image entryItem to entry ${entry.entryId}")
                        onComplete(-1L)
                    }
            } catch(e: Exception) {
                Log.e("EntryItemViewModel", "Error adding image entryItem ", e)
            }
        }
    }

    fun updateImageCaption(imageItem: EntryItem.ImageItem, newCaption: String) {
        updateImageEntryItem(
            EntryItem.ImageItem(
                id = imageItem.id,
                entryId = imageItem.entryId,
                caption = newCaption,
                imageItem.imageUri
            )
        )
    }

    fun updateImageEntryItem(entryItem: EntryItem.ImageItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateImageItem(entryItem)
                    .onSuccess { it ->
                        Log.d("EntryItemViewModel", "Updated entryItem ${entryItem.id}")
                        loadEntryItems()
                    }
                    .onFailure {
                        Log.e("EntryItemViewModel", "Failed to update entryItem ${entryItem.id}")
                    }
            } catch (e: Exception) {
                Log.e("EntryItemViewModel", "Error updating entryItem ", e)
            }
        }
    }

    fun addTextEntryItem(texContent: String, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addTextItem(texContent, entry.entryId)
                    .onSuccess { id ->
                        Log.d("EntryItemViewModel", "Added entryItem with ID: $id")
                        loadEntryItems()
                        onComplete(id)
                    }
                    .onFailure {
                        Log.d("EntryItemViewModel", "Failed to add new entryItem to entry ${entry.entryId}")
                        onComplete(-1L)
                        onComplete(-1L)
                    }
            } catch (e: Exception) {
                Log.e("EntryItemViewModel", "Error adding entry item ", e)
            }
        }
    }

    fun updateTextEntryItem(entryItem: EntryItem.TextItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateTextItem(entryItem)
                    .onSuccess { it ->
                        Log.d("EntryItemViewModel", "Updated entryItem ${entryItem.id}")
                        loadEntryItems()
                    }
                    .onFailure {
                        Log.e("EntryItemViewModel", "Failed to update entryItem ${entryItem.id}")
                    }
            } catch (e: Exception) {
                Log.e("EntryItemViewModel", "Error updating entryItem ", e)
            }
        }
    }

    fun deleteEntryItem(entryItem: EntryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                when(entryItem) {
                    is EntryItem.TextItem -> {
                        repository.deleteTextItem(entryItem)
                            .onSuccess {
                                Log.d("EntryItemViewModel", "Deleted entryItem ${entryItem.id}")
                                _allEntryItems.value = _allEntryItems.value.filter { it.id != entryItem.id }
                            }
                            .onFailure {
                                Log.e("EntryItemViewModel", "Failed to delete entryItem ${entryItem.id}")
                            }
                    }
                    is EntryItem.ImageItem -> {
                        repository.deleteImageItem(entryItem)
                            .onSuccess {
                                Log.d("EntryItemViewModel", "Deleted entryItem ${entryItem.id}")
                                _allEntryItems.value = _allEntryItems.value.filter { it.id != entryItem.id }
                            }
                            .onFailure {
                                Log.e("EntryItemViewModel", "Failed to delete entryItem ${entryItem.id}")
                            }
                    }
                }
            } catch(e: Exception) {
                Log.e("EntryItemViewModel", "Error deleting entryItem ", e)
            }
            loadEntryItems() // Refresh from DB
        }
    }
}