package com.example.markerclient.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel

class NavigationTrailingButtonViewModel : ViewModel() {
    var buttonVisible by mutableStateOf(false)
        private set
    var buttonIcon by mutableStateOf(Icons.Default.Add)
        private  set
    var buttonAction: () -> Unit by mutableStateOf({})
        private set

    fun updateButton(visible : Boolean = buttonVisible,
                     icon : ImageVector = buttonIcon,
                     action : () -> Unit =  buttonAction
    ) {
        buttonVisible =  visible
        buttonIcon = icon
        buttonAction = action
    }
}