package com.example.markerclient.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.markerclient.domain.NavigationTrailingButtonViewModel

@Composable
fun NoteScreen(navigationTrailingButtonViewModel: NavigationTrailingButtonViewModel) {
    // Set Navigation FAB
    LaunchedEffect(Unit) {
        navigationTrailingButtonViewModel.updateButton(
            visible = true,
            icon = Icons.Filled.Add,
            action = {}
        )
    }
    // TO-DO: Remove later
    Text(modifier = Modifier.fillMaxWidth(), text = "Notes Screen")
}