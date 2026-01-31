package com.example.markerclient.domain.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.markerclient.db.EntryItemRepository
import com.example.markerclient.db.MyDBHandler
import com.example.markerclient.domain.Entry
import com.example.markerclient.domain.EntryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EntryItemViewModel(application : Application, private val entry: Entry) : AndroidViewModel(application) {
    private val entryItemRepo = EntryItemRepository(MyDBHandler(application))

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
                val allEntryItems = entryItemRepo.getAllEntryItems(entry.entryId)
                _allEntryItems.value = allEntryItems
                Log.d("EntryItemViewModel", "Loaded ${allEntryItems.size} entry items for entry ${entry.entryId}")
            } catch (e: Exception) {
                Log.e("EntryItemViewModel", "Error loading entry items ", e)
            }
        }
    }

    fun addEntryItem(entryItem: EntryItem, onComplete: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entryItemId = entryItemRepo.addNewEntryItem(entryItem, entry.entryId)
                if(entryItemId > 0){
                    Log.d("EntryItemViewModel", "Added entryItem with ID: $entryItemId")
                    loadEntryItems()
                    onComplete(entryItemId)
                }
                else {
                    Log.d("EntryItemViewModel", "Failed to add new entryItem to entry ${entry.entryId}")
                    onComplete(-1L)
                }
            } catch (e: Exception) {
                Log.e("EntryItemViewModel", "Error adding entry item ", e)
            }
        }
    }

    fun updateEntryItem(entryItem: EntryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = entryItemRepo.updateEntryItem(entryItem, entry.entryId)
                if (success) {
                    Log.d("EntryItemViewModel", "Updated entryItem ${entryItem.entryItemId}")
                    loadEntryItems()
                } else {
                    Log.e("EntryItemViewModel", "Failed to update entryItem ${entryItem.entryItemId}")
                }
            } catch (e: Exception) {
                Log.e("EntryItemViewModel", "Error updating entryItem ", e)
            }
        }
    }

    fun deleteEntryItem(entryItem: EntryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = entryItemRepo.deleteEntryItem(entryItem)
                if (success) {
                    Log.d("EntryItemViewModel", "Deleted entryItem ${entryItem.entryItemId}")
                    // Update local state immediately for responsiveness
                    _allEntryItems.value = _allEntryItems.value.filter { it.entryItemId != entryItem.entryItemId }
                } else {
                    Log.e("EntryItemViewModel", "Failed to delete entryItem ${entryItem.entryItemId}")
                }
            } catch (e: Exception) {
                Log.e("EntryItemViewModel", "Error deleting entryItem ", e)
            }
        }
    }
}