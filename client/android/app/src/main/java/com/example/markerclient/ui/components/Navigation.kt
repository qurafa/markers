package com.example.markerclient.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost


enum class MarkerScreen(@StringRes val title: Int){
    Map(1),
    Notes(2),
    Settings(3)
}


@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    onNotesClick: () -> Unit,
    onMapClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    BottomAppBar(
        modifier = modifier,
        containerColor = Color(1,0,0,1)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        )
        {
            FloatingActionButton(onClick = onNotesClick) {
                Icon(Icons.Filled.Edit, contentDescription = "Notes")
            }
            FloatingActionButton(onClick = onMapClick) {
                Icon(Icons.Filled.LocationOn, contentDescription = "Main Map")
            }
            FloatingActionButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
    }
//    NavHost()
}