package com.example.markerclient.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.markerclient.domain.NavigationTrailingButtonViewModel

@Composable
fun SettingScreen(navigationTrailingButtonViewModel : NavigationTrailingButtonViewModel) {
    // Set Navigation FAB
    LaunchedEffect(Unit) {
        navigationTrailingButtonViewModel.updateButton(
            visible = false
        )
    }
    // TO-DO: Remove later
    Text("Settings Screen")
}