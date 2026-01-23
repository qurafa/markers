package com.example.markerclient.domain.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.markerclient.domain.Entry

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