package com.example.markerclient.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.markerclient.domain.CustomDismissCardViewModel
import kotlinx.coroutines.launch

@Composable
fun CustomDismissableCard(
    modifier : Modifier,
    customDismissCardViewModel : CustomDismissCardViewModel
) {
    val state = rememberSwipeToDismissBoxState(
//        positionalThreshold = {totalDistance ->
//            totalDistance * 3
//        }
    )
    val scope = rememberCoroutineScope()

    if(customDismissCardViewModel.cardVisible){
        SwipeToDismissBox(
            modifier = modifier,
            state = state,
            backgroundContent = {},
            enableDismissFromEndToStart = customDismissCardViewModel.rightToLeftDismiss,
            enableDismissFromStartToEnd = customDismissCardViewModel.leftToRightDismiss,
            onDismiss = { direction ->
                customDismissCardViewModel.cardOnDismiss()
                scope.launch { state.reset() }
            }
        ) {
            ElevatedCard (
                modifier = Modifier
                    .fillMaxSize(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp)
            ){
                customDismissCardViewModel.cardContent()
            }
        }
    }
}