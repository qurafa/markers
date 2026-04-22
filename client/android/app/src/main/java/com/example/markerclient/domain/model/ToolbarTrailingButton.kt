package com.example.markerclient.domain.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector

class ToolbarTrailingButton : ToolbarButton() {
    var buttonVisible by mutableStateOf(getDefaultButtonVisible())
        private set

    fun updateButton(visible : Boolean = buttonVisible,
                     icon : ImageVector = buttonIcon,
                     action : () -> Unit =  buttonAction,
                     description : String = buttonDescription
    ) : ToolbarTrailingButton {
        buttonVisible =  visible
        super.updateButton(icon = icon, action = action, description = description)

        return this
    }

    companion object {
        private const val DEFAULT_BUTTON_VISIBLE : Boolean = false

        fun getDefaultButtonVisible() : Boolean {
            return DEFAULT_BUTTON_VISIBLE
        }
     }
}