package com.example.markerclient.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.markerclient.domain.CustomDismissCardViewModel
import com.example.markerclient.domain.CustomModalBottomSheetViewModel
import com.example.markerclient.domain.MarkViewModel
import com.example.markerclient.domain.NavigationTrailingButtonViewModel
import com.example.markerclient.ui.components.MarkerScreen
import com.example.markerclient.ui.components.AppFloatingNavigationBar
import com.example.markerclient.ui.components.CustomDismissableCard
import com.example.markerclient.ui.theme.MarkerClientTheme
import com.example.markerclient.utils.RequestLocationPermission
import com.example.markerclient.utils.currentRoute
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.viewport.ViewportStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val mapSnackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val mapViewportState = rememberMapViewportState()

    val markViewModel : MarkViewModel = viewModel()
    val navigationTrailingButtonViewModel : NavigationTrailingButtonViewModel = viewModel()
    val customModalBottomSheetViewModel : CustomModalBottomSheetViewModel = viewModel()
    val customDismissCardViewModel : CustomDismissCardViewModel = viewModel()

    MarkerClientTheme {
        Scaffold(
            /*
            bottomBar = {
                AppNavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    onNotesClick = {navController.navigate(MarkerScreen.Notes.name)},
                    onMapClick = {navController.navigate(MarkerScreen.Map.name)},
                    onSettingsClick = {navController.navigate(MarkerScreen.Settings.name)}
                )
            },
            */
            /*
            floatingActionButton = {
                RouteFloatingActionButton(navController, mapViewportState)
            },
            */
            snackbarHost = {
                SnackbarHost(hostState = mapSnackbarHostState)
            }
        ) { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()){
                NavHost(
                    navController = navController,
                    startDestination = MarkerScreen.Map.name,
                    modifier = Modifier
                        .padding(innerPadding)
                ) {
                    composable(route = MarkerScreen.Map.name) {
                        if (requestMapPermissions(mapSnackbarHostState, coroutineScope)) {
                            MapScreen(modifier = Modifier.zIndex(0f),
                                mapViewportState = mapViewportState,
                                coroutineScope = coroutineScope,
                                markViewModel = markViewModel,
                                navigationTrailingButtonViewModel = navigationTrailingButtonViewModel,
                                customDismissCardViewModel = customDismissCardViewModel)
                        }
                    }
                    composable(route = MarkerScreen.Notes.name) {
                        NoteScreen(navigationTrailingButtonViewModel)
                    }
                    composable(route = MarkerScreen.Settings.name) {
                        SettingScreen(navigationTrailingButtonViewModel)
                    }
                }
                
                // Add Floating Navigation Bar
                AppFloatingNavigationBar(
                    modifier = Modifier
                        .wrapContentSize()
                        .offset(y = -ScreenOffset)
                        .align(Alignment.BottomCenter)
                        .zIndex(1f),
                    expanded = true,
                    trailingButtonViewModel = navigationTrailingButtonViewModel,
                    onNotesClick = {navController.navigate(MarkerScreen.Notes.name)},
                    onMapClick = {navController.navigate(MarkerScreen.Map.name)},
                    onSettingsClick = {navController.navigate(MarkerScreen.Settings.name)}
                )

                // Add card for pop up information
                CustomDismissableCard(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.75f)
                        .offset(y = -ScreenOffset)
                        .align(Alignment.Center)
                        .zIndex(0f),
                    customDismissCardViewModel
                )
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
                mapViewportState
                    .transitionToFollowPuckState(mapViewDefaultFollowPuckViewportStateOptions())
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
    scope: CoroutineScope
) : Boolean {
    var mapPermissionRequestCount by remember { mutableIntStateOf(1) }
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

private fun openAppSettings(context: Context) {
    val intent = Intent(
        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}