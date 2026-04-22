package com.example.markerclient

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.markerclient.ui.elements.screens.MainScreen
import com.example.markerclient.ui.elements.theme.MarkerClientTheme

class PreviewActivity: ComponentActivity() {
    @Preview(showBackground = true)
    @Composable
    fun MainActivityPreview() {
        MarkerClientTheme{
            val navController = rememberNavController()
            MainScreen(navController)
        }
    }
}