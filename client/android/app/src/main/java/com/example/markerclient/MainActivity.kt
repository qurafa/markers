package com.example.markerclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.markerclient.ui.screens.MainScreen

public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            MainScreen(navController)
        }
    }

    @Preview
    @Composable
    fun BottomBarPreview() {
        val navController = rememberNavController()
        MainScreen(navController)
    }
}