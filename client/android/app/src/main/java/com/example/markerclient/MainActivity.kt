package com.example.markerclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.markerclient.ui.elements.screens.MainScreen
import com.example.markerclient.ui.elements.theme.MarkerClientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
            setContent {
                MarkerClientTheme{
                    val navController = rememberNavController()
                    MainScreen(navController)
                }
            }

    }
}