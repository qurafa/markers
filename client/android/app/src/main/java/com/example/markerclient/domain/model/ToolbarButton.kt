package com.example.markerclient.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adb
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector

open class ToolbarButton {
    var buttonIcon by mutableStateOf(getDefaultButtonIcon())
        private  set
    var buttonAction by mutableStateOf(getDefaultButtonAction())
        private set
    var buttonDescription by mutableStateOf(getDefaultButtonDescription())

    fun updateButton(icon : ImageVector = buttonIcon,
                     action : () -> Unit =  buttonAction,
                     description : String = buttonDescription
    ) : ToolbarButton {
        buttonIcon = icon
        buttonAction = action
        buttonDescription = description

        return this
    }

    companion object {
        private val DEFAULT_BUTTON_ICON : ImageVector = Icons.Filled.Adb
        private val DEFAULT_BUTTON_ACTION : () -> Unit = {}
        private const val DEFAULT_BUTTON_DESCRIPTION : String = "Default Button"

        fun getDefaultButtonIcon() : ImageVector {
            return DEFAULT_BUTTON_ICON
        }
        fun getDefaultButtonAction() : () -> Unit {
            return DEFAULT_BUTTON_ACTION
        }

        fun getDefaultButtonDescription() : String {
            return DEFAULT_BUTTON_DESCRIPTION
        }
    }
}