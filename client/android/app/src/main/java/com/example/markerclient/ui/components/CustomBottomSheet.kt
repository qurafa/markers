package com.example.markerclient.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.markerclient.domain.CustomModalBottomSheetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(modifier: Modifier, bottomSheetViewModel : CustomModalBottomSheetViewModel){
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Only set when visible is set to true
    if(bottomSheetViewModel.sheetVisible){
        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = bottomSheetViewModel.sheetOnDismiss,
            sheetState = bottomSheetState
        ) {
            // set the content of the modal bottom sheet
            bottomSheetViewModel.sheetContent
        }
    }
}