package com.example.markerclient.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CustomDismissCardViewModel : ViewModel(){
    var cardVisible by mutableStateOf(false)
        private set
    var cardContent by mutableStateOf<@Composable () -> Unit>({})
        private set
    var leftToRightDismiss by mutableStateOf(true)
        private set
    var rightToLeftDismiss by mutableStateOf(true)
        private set
    var cardOnDismiss by mutableStateOf({})
        private set

    fun update(visible : Boolean = cardVisible,
               content : @Composable () -> Unit = cardContent,
               ltrDismiss : Boolean = leftToRightDismiss,
               rtlDismiss : Boolean = rightToLeftDismiss,
               onDismiss : () -> Unit = cardOnDismiss
    ){
        cardVisible = visible
        cardContent = content
        leftToRightDismiss = ltrDismiss
        rightToLeftDismiss = rtlDismiss
        cardOnDismiss = onDismiss
    }

    fun reset() {
        cardVisible = false
        cardContent = {}
        leftToRightDismiss = true
        rightToLeftDismiss = true
        cardOnDismiss = {}
    }
}