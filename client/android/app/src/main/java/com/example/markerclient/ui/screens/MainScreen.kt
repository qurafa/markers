package com.example.markerclient.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.markerclient.ui.components.MarkerScreen
import com.example.markerclient.ui.components.Navigation
import com.example.markerclient.ui.theme.MarkerClientTheme

@Composable
fun MainScreen(navController: NavHostController) {
    MarkerClientTheme {
        Scaffold(
            bottomBar = {
                Navigation(
                    onNotesClick = {},
                    onMapClick = {},
                    onSettingsClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MarkerScreen.Map.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = MarkerScreen.Map.name) {
                    MapScreen()
                }
                composable(route = MarkerScreen.Notes.name) {
                    NoteScreen()
                }
                composable(route = MarkerScreen.Settings.name) {
                    SettingScreen()
                }
            }
        }
    }
}