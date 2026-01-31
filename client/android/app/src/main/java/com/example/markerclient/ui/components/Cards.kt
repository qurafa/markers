package com.example.markerclient.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.markerclient.domain.CustomDismissCard
import kotlinx.coroutines.launch

@Composable
fun CustomDismissableCard(
    modifier : Modifier = Modifier,
    customDismissCard : CustomDismissCard
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = {totalDistance ->
            totalDistance * 0.5f
        }
    )
    val scope = rememberCoroutineScope()

    if(customDismissCard.cardVisible){
        SwipeToDismissBox(
            modifier = modifier.then(
                customDismissCard.cardModifier),
            state = dismissState,
            backgroundContent = {},
            enableDismissFromEndToStart = customDismissCard.rightToLeftDismiss,
            enableDismissFromStartToEnd = customDismissCard.leftToRightDismiss,
            onDismiss = { direction ->
                customDismissCard.cardOnDismiss()
                scope.launch { dismissState.reset() }
            }
        ) {
            ElevatedCard (
                modifier = Modifier
                    .fillMaxSize()
//                    .windowInsetsPadding(WindowInsets.ime) // Adjust for ime padding
                    .imePadding(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp)
            ){
                customDismissCard.cardContent()
            }
        }
    }
}