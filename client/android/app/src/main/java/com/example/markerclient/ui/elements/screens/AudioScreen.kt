package com.example.markerclient.ui.elements.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.markerclient.domain.model.CustomDismissCard
import com.example.markerclient.domain.model.ToolbarTrailingButton

@Composable
fun AudioScreen(modifier : Modifier = Modifier,
                customDismissCard : CustomDismissCard,
                navigationTrailingButton : ToolbarTrailingButton)
{
    // Set Navigation trailing button and reset custom dismiss card
    LaunchedEffect(Unit) {
        customDismissCard.reset()

        navigationTrailingButton.updateButton(
            visible = true,
            icon = Icons.Filled.Add,
            action = {}
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    )
    {
        // TO-DO: Remove later
        Text(modifier = Modifier.align(Alignment.Center) , text = "Under construction...")
    }
}