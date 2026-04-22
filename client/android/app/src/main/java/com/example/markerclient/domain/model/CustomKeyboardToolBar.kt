package com.example.markerclient.domain.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.collections.emptyList

class CustomKeyboardToolBar (
    private val defaultItemsProvider: () -> List<KeyboardToolbarItem> = {emptyList<KeyboardToolbarItem>()},
    private val visibleItemsProvider: () -> List<KeyboardToolbarItem> = {emptyList<KeyboardToolbarItem>()},
    private val nonVisibleItemsProvider: () -> List<KeyboardToolbarItem> = {emptyList<KeyboardToolbarItem>()}
) {
    var keyboardVisible by mutableStateOf(getDefaultKeyBoardVisible())
        private set
    var keyboardToolBarItems by mutableStateOf(getDefaultKeyboardToolBarItems())
        private set

    data class KeyboardToolbarItem(
        val icon : ImageVector,
        val label : String,
        val onClick : () -> Unit
    )

    fun update(keyboardVisible : Boolean = this@CustomKeyboardToolBar.keyboardVisible) : CustomKeyboardToolBar
    {
        this@CustomKeyboardToolBar.keyboardVisible = keyboardVisible

        updateToolbarItems()

        return this
    }

    private fun updateToolbarItems() : List<KeyboardToolbarItem> {
        keyboardToolBarItems =
                if(keyboardVisible){
                    defaultItemsProvider().plus(visibleItemsProvider())
                } else {
                    defaultItemsProvider().plus(nonVisibleItemsProvider())
                }
        return keyboardToolBarItems
    }

    companion object {
        private const val DEFAULT_KEYBOARD_VISIBLE : Boolean = false

        private val DEFAULT_KEYBOARD_TOOLBAR_ITEMS : List<KeyboardToolbarItem> = emptyList<KeyboardToolbarItem>()

        fun getDefaultKeyBoardVisible() : Boolean {
            return DEFAULT_KEYBOARD_VISIBLE
        }

        fun getDefaultKeyboardToolBarItems() : List<KeyboardToolbarItem> {
            return DEFAULT_KEYBOARD_TOOLBAR_ITEMS
        }
    }
}