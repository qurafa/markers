package com.example.markerclient.domain.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.markerclient.db.EntryRepository
import com.example.markerclient.db.MyDBHandler
import com.example.markerclient.domain.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EntryViewModel(application: Application) : AndroidViewModel(application) {
    private val entryRepo = EntryRepository(MyDBHandler(application))

    // All entries
    private val _allEntries = MutableStateFlow<List<Entry>>(emptyList())

    val entries : StateFlow<List<Entry>> = _allEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init { loadEntries() }

    fun loadEntries() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allEntries = entryRepo.getAllEntries()
                _allEntries.value = allEntries
                Log.d("EntryViewModel", "Loaded ${allEntries.size} entries")
            } catch (e: Exception) {
                Log.e("EntryViewModel", "Error loading entries ", e)
            }
        }
    }

    fun addEntry(entry: Entry, onComplete: (Long) -> Unit) {
        var entryId : Long = -1
        viewModelScope.launch(Dispatchers.IO) {
            try {
                entryId = entryRepo.addNewEntry(entry)
                if(entryId > 0) {
                    Log.d("EntryViewModel", "Added entry with ID: $entryId")
                    loadEntries()
                    withContext(Dispatchers.Main) {
                        onComplete(entryId)
                    }
                } else {
                    Log.d("EntryViewModel", "Failed to add entry")
                    withContext(Dispatchers.Main) {
                        onComplete(entryId)
                    }
                }
            } catch (e: Exception) {
                Log.e("EntryViewModel", "Error adding entry ", e)
            }
        }
    }

    fun getEntryById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {

            } catch (e: Exception) {
                Log.e("EntryViewModel", "Error getting entry by entryItemId ", e)
            }
        }
    }

    fun updateEntry(entry : Entry) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = entryRepo.updateEntry(entry)
                if (success) {
                    Log.d("EntryViewModel", "Updated entry ${entry.entryId}")
                    loadEntries()
                } else {
                    Log.e("EntryViewModel", "Failed to update entry ${entry.entryId}")
                }
            } catch (e: Exception) {
                Log.e("EntryViewModel", "Error updating entry ", e)
            }
        }
    }

    fun deleteEntry(entry : Entry) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = entryRepo.deleteEntry(entry)
                if(success) {
                    Log.d("EntryViewModel", "Deleted entry ${entry.entryId}")
                    // Update local state immediately for responsiveness
                    _allEntries.value = _allEntries.value.filter { it.entryId != entry.entryId }
                } else {
                    Log.e("EntryViewModel", "Failed to delete entry ${entry.entryId}")
                }
            } catch (e: Exception) {
                Log.e("EntryViewModel", "Error deleting entry ${entry.entryId} ", e)
            }
        }
    }
}