package com.example.markerclient.ui.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.markerclient.data.dao.EntryDao
import com.example.markerclient.data.db.MyDBHandler
import com.example.markerclient.data.repositories.EntryRepository
import com.example.markerclient.domain.model.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EntryViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHandler = MyDBHandler.getInstance(application)
    private val entryDao = EntryDao(dbHandler)
    private val entryRepo = EntryRepository(entryDao)
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

    fun addEntry(title: String = "", onComplete: (Long) -> Unit) {
        var entryId : Long = -1
        viewModelScope.launch(Dispatchers.IO) {
            try {
                entryId = entryRepo.addNewEntry(title)
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

    fun getEntryById(id: Long): Entry? {
        var entry: Entry? = null;
        viewModelScope.launch(Dispatchers.IO) {
            try {
                entry = entryRepo.getEntry(id)
            } catch (e: Exception) {
                Log.e("EntryViewModel", "Error getting entry by id ", e)
            }
        }
        return entry
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

    fun deleteEntry(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = entryRepo.deleteEntry(id)
                if(success) {
                    Log.d("EntryViewModel", "Deleted entry ${id}")
                    // Update local state immediately for responsiveness
                    _allEntries.value = _allEntries.value.filter { it.entryId != id }
                } else {
                    Log.e("EntryViewModel", "Failed to delete entry ${id}")
                }
            } catch (e: Exception) {
                Log.e("EntryViewModel", "Error deleting entry ${id} ", e)
            }
            loadEntries() // Refresh from database
        }
    }
}