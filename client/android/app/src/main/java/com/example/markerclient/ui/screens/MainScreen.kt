package com.example.markerclient.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.markerclient.R
import com.example.markerclient.domain.CustomDismissCard
import com.example.markerclient.domain.ToolbarButton
import com.example.markerclient.domain.viewModel.MarkViewModel
import com.example.markerclient.domain.ToolbarTrailingButton
import com.example.markerclient.domain.viewModel.EntryViewModel
import com.example.markerclient.ui.components.MarkerScreen
import com.example.markerclient.ui.components.AppFloatingNavigationToolBar
import com.example.markerclient.ui.components.CustomDismissableCard
import com.example.markerclient.utils.RequestLocationPermission
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val mapSnackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val mapViewportState = rememberMapViewportState()

    val markViewModel : MarkViewModel = viewModel()
    val entryViewModel : EntryViewModel = viewModel()
    val navigationToolbarTrailingButton = remember { ToolbarTrailingButton() }
    val customDismissCard = remember { CustomDismissCard() }
    val navigationToolbarButtons = listOf<ToolbarButton>(
        ToolbarButton().updateButton(
            icon = Icons.Filled.Audiotrack,
            action = {navController.navigate(MarkerScreen.Audio.name)},
            description = stringResource(R.string.audio_screen)
        ),
        ToolbarButton().updateButton(
            icon = Icons.Filled.Book,
            action = {navController.navigate(MarkerScreen.Journal.name)},
            description = stringResource(R.string.journal_screen)
        ),
        ToolbarButton().updateButton(
            icon = Icons.Filled.LocationOn,
            action = {navController.navigate(MarkerScreen.Map.name)},
            description = stringResource(R.string.map_screen)
        )
    )

    Box(
        modifier = Modifier
        .fillMaxSize()
    )
    {
        Box(
            modifier = Modifier
            .fillMaxSize()
        )
        {
            // NavHost to navigate across different screens
            NavHost(
                navController = navController,
                startDestination = MarkerScreen.Journal.name,
                modifier = Modifier.fillMaxSize()
            )
            {
                composable(route = MarkerScreen.Map.name) {
                    if (requestMapPermissions(mapSnackbarHostState, coroutineScope)) {
                        MapScreen(modifier = Modifier.zIndex(0f),
                            mapViewportState = mapViewportState,
                            coroutineScope = coroutineScope,
                            markViewModel = markViewModel,
                            navigationTrailingButton = navigationToolbarTrailingButton,
                            customDismissCard = customDismissCard)
                    }
                }
                composable(route = MarkerScreen.Journal.name) {
                    JournalScreen(modifier = Modifier.zIndex(0f),
                        entryViewModel = entryViewModel,
                        navigationTrailingButton = navigationToolbarTrailingButton,
                        customDismissCard = customDismissCard)
                }
                composable(route = MarkerScreen.Audio.name) {
                    AudioScreen(modifier = Modifier.zIndex(0f),
                        navigationTrailingButton = navigationToolbarTrailingButton,
                        customDismissCard = customDismissCard)
                }
            }

            // Add Floating Navigation Bar
            AppFloatingNavigationToolBar(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.BottomCenter)
                    .zIndex(1f)
                    .windowInsetsPadding(WindowInsets.systemBars),// so it doesn't overlap the system bars
                expanded = true,
                buttons = navigationToolbarButtons,
                trailingButton = navigationToolbarTrailingButton
            )

            // Add card for pop up information with it's default modifier to be stacked on top of later
            CustomDismissableCard(
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(2f)
                    .windowInsetsPadding(WindowInsets.systemBars),// so it doesn't overlap the system bars,
                customDismissCard = customDismissCard
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
                    actionLabel = "Audio"
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