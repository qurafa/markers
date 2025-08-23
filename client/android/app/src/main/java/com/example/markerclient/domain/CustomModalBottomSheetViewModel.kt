package com.example.markerclient.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CustomModalBottomSheetViewModel : ViewModel(){
    var sheetVisible by mutableStateOf(false)
        private set
    var sheetContent by mutableStateOf<@Composable () -> Unit>({})
        private set
    var sheetOnDismiss by mutableStateOf({})
        private set

    fun update(visible : Boolean = sheetVisible,
               content : @Composable () -> Unit = sheetContent,
               onDismiss : () -> Unit = sheetOnDismiss
    ){
        sheetVisible = visible
        sheetContent = content
        sheetOnDismiss = onDismiss
    }
}