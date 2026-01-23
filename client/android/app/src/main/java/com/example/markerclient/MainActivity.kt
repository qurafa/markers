package com.example.markerclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.internal.enableLiveLiterals
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.markerclient.ui.screens.MainScreen
import com.example.markerclient.ui.theme.MarkerClientTheme
import com.example.markerclient.ui.theme.NavStatBarDarkColorScheme
import com.example.markerclient.ui.theme.NavStatBarLightColorScheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
//        val lightColorScheme = NavStatBarLightColorScheme
//        val darkColorScheme = NavStatBarDarkColorScheme

//        enableEdgeToEdge(
//            statusBarStyle = SystemBarStyle.auto(
//                lightScrim = lightColorScheme.primary.toArgb(),
//                darkScrim = darkColorScheme.primary.toArgb()
//            ),
//            navigationBarStyle = SystemBarStyle.auto(
//                lightScrim = lightColorScheme.primary.toArgb(),
//                darkScrim = darkColorScheme.primary.toArgb()
//            )
//        )

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
            setContent {
                MarkerClientTheme{
                    val navController = rememberNavController()
                    MainScreen(navController)
                }
            }

    }

    @Preview
    @Composable
    fun MainActivityPreview() {
        onCreate(null)
    }
}