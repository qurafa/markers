package com.example.markerclient.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarHorizontalFabPosition
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.markerclient.R
import com.example.markerclient.domain.NavigationTrailingButtonViewModel


enum class MarkerScreen(@StringRes val title: Int){
    Map(title = R.string.map_screen),
    Notes(title = R.string.note_screen),
    Settings(title = R.string.setting_screen)
}

@Composable
fun AppBottomAppBar(
    modifier: Modifier = Modifier,
    onNotesClick: () -> Unit,
    onMapClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    BottomAppBar(
        modifier = modifier,
        containerColor = Color(255,255,255,0)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        )
        {
            FloatingActionButton(onClick = onNotesClick) {
            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.note_screen))
            }
            FloatingActionButton(onClick = onMapClick) {
                Icon(Icons.Filled.LocationOn, contentDescription = stringResource(R.string.map_screen))
            }
            FloatingActionButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.setting_screen))
            }
        }
    }
}

@Composable
fun AppNavigationBar(
    modifier: Modifier = Modifier,
    onNotesClick: () -> Unit,
    onMapClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color(255,255,255,0)
    ) {
        NavigationBarItem(
            icon = {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.note_screen))
                   },
            selected = false,
            onClick = onNotesClick
        )
        NavigationBarItem(
            icon = {
                Icon(Icons.Filled.LocationOn, contentDescription = stringResource(R.string.map_screen))
            },
            selected = true,
            onClick = onMapClick
        )
        NavigationBarItem(
            icon = {
                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.setting_screen))
            },
            selected = false,
            onClick = onSettingsClick
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppFloatingNavigationBar(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    trailingButtonViewModel: NavigationTrailingButtonViewModel,
    onNotesClick: () -> Unit,
    onMapClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    HorizontalFloatingToolbar(
        expanded = expanded,
        modifier = modifier,
        trailingContent = {
            if(trailingButtonViewModel.buttonVisible){
                IconButton(
                    onClick = trailingButtonViewModel.buttonAction
                ) {
                    Icon(trailingButtonViewModel.buttonIcon, contentDescription = null)
                }
            }
        },
        expandedShadowElevation = 10.dp,
        collapsedShadowElevation = 6.dp
//        floatingActionButton = {
//            if(fabViewModel.fabVisible){
//                FloatingToolbarDefaults.VibrantFloatingActionButton(
//                    onClick = fabViewModel.fabAction
//                ) {
//                    Icon(fabViewModel.fabIcon, contentDescription = null)
//                }
//            }
//        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.7f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = onNotesClick
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.note_screen)
                )
            }
            FilledIconButton(onClick = onMapClick) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = stringResource(R.string.map_screen)
                )
            }
            FilledIconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.setting_screen)
                )
            }
        }
    }
}