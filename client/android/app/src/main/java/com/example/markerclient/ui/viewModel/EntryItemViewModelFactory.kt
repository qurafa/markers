package com.example.markerclient.ui.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.markerclient.domain.model.Entry

// factory to build entry item of different types
class EntryItemViewModelFactory(
    private val application: Application,
    private val entry: Entry
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EntryItemViewModel(application, entry) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}