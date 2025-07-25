package com.example.markerclient.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.markerclient.ui.components.MarkerScreen
import com.example.markerclient.ui.components.Navigation
import com.example.markerclient.ui.theme.MarkerClientTheme
import com.example.markerclient.utils.RequestLocationPermission
import com.example.markerclient.utils.currentRoute
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.viewport.ViewportStatus
import kotlinx.coroutines.launch

@Composable
fun MainScreen(navController: NavHostController) {
    val mapSnackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val mapViewportState = rememberMapViewportState()

    MarkerClientTheme {
        Scaffold(
            bottomBar = {
                NavigationBar()
            },
            floatingActionButton = {
                RouteFloatingActionButton(navController, mapViewportState)
            },
            snackbarHost = {
                SnackbarHost(hostState = mapSnackbarHostState)
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MarkerScreen.Map.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = MarkerScreen.Map.name) {
                    if(requestMapPermissions(mapSnackbarHostState, scope)){
                        MapScreen(mapViewportState)
                    }
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

@Composable
private fun RouteFloatingActionButton(navController: NavHostController, mapViewportState: MapViewportState) {
    when(currentRoute(navController)){
        MarkerScreen.Map.name -> {
            MapFloatingActionButton(mapViewportState)
        }
        MarkerScreen.Notes.name -> {
        }
        MarkerScreen.Settings.name -> {
        }
    }
}

@Composable
private fun MapFloatingActionButton(mapViewportState: MapViewportState) {
    if (mapViewportState.mapViewportStatus == ViewportStatus.Idle) {
        FloatingActionButton(
            onClick = {
                mapViewportState.transitionToFollowPuckState()
            }
        ){
            Image(
                painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                contentDescription = "Locate button"
            )
        }
    }
}

@Composable
private fun requestMapPermissions(
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) : Boolean {
    var mapPermissionRequestCount by remember { mutableStateOf(1) }
    var showMap by remember { mutableStateOf(false) }
    val context = LocalContext.current

    RequestLocationPermission(
        requestCount = mapPermissionRequestCount,
        onPermissionDenied = {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Location permission is required to show the map.",
                    actionLabel = "Settings"
//                    withDismissAction = true
                )

                when (result){
                    SnackbarResult.ActionPerformed -> {
                        openAppSettings(context)
                    }
                    SnackbarResult.Dismissed -> {
                        // User dismissed the snackbar
                    }
                }
            }
            mapPermissionRequestCount++
        },
        onPermissionReady = {
            showMap = true
        }
    )

    // clear snackbar if it's active
    if(showMap) {
        snackbarHostState.currentSnackbarData?.dismiss()
    }

    return showMap
}

@Composable
private fun NavigationBar() {
    Navigation(
        onNotesClick = {},
        onMapClick = {},
        onSettingsClick = {},
        modifier = Modifier
            .fillMaxWidth()
    )
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}