package com.example.markerclient.ui.elements.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.markerclient.R
import com.example.markerclient.domain.model.CustomKeyboardToolBar
import com.example.markerclient.domain.model.ToolbarButton
import com.example.markerclient.domain.model.ToolbarTrailingButton

@Composable
fun AppBottomAppBar(
    modifier: Modifier = Modifier,
    onJournalClick: () -> Unit,
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
            FloatingActionButton(onClick = onJournalClick) {
            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.journal_screen))
            }
            FloatingActionButton(onClick = onMapClick) {
                Icon(Icons.Filled.LocationOn, contentDescription = stringResource(R.string.map_screen))
            }
            FloatingActionButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.audio_screen))
            }
        }
    }
}

@Composable
fun AppNavigationBar(
    modifier: Modifier = Modifier,
    onJournalClick: () -> Unit,
    onMapClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color(255,255,255,0)
    ) {
        NavigationBarItem(
            icon = {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.journal_screen))
                   },
            selected = false,
            onClick = onJournalClick
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
                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.audio_screen))
            },
            selected = false,
            onClick = onSettingsClick
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppFloatingNavigationToolBar(
    modifier: Modifier = Modifier,
    buttonModifier : Modifier? = null,
    expanded: Boolean,
    buttons: List<ToolbarButton>,
    trailingButton: ToolbarTrailingButton
) {
    val buttonModifier : Modifier = buttonModifier ?: Modifier.size(60.dp).padding(2.dp)

    HorizontalFloatingToolbar(
        expanded = expanded,
        modifier = modifier.wrapContentSize(),
        trailingContent = {
            if(trailingButton.buttonVisible){
                FilledIconButton(
                    modifier = buttonModifier,
                    onClick = trailingButton.buttonAction
                ) {
                    Icon(
                        trailingButton.buttonIcon,
                        contentDescription = trailingButton.buttonDescription
                    )
                }
            }
        },
        expandedShadowElevation = 10.dp,
        collapsedShadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.5f),
            horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ){
            buttons.forEach { button ->
                IconButton(
                    modifier = buttonModifier,
                    onClick = button.buttonAction
                ) {
                    Icon(
                        button.buttonIcon,
                        contentDescription = button.buttonDescription
                    )
                }
            }
        }
    }
}

@Composable
fun KeyboardToolBar(
    keyboardToolBar: CustomKeyboardToolBar,
    modifier : Modifier = Modifier,
    focusRequester: FocusRequester,
    focusManager: FocusManager = LocalFocusManager.current,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current
) {
    val keyboardVisible by rememberKeyboardAsState()

    LaunchedEffect(keyboardVisible)
    {
        keyboardToolBar.update(keyboardVisible)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    )
    {
        // Keyboard toggle button
        FilledIconButton(
            onClick = {
                if (keyboardVisible) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                } else {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            },
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.CenterVertically)
                .padding(8.dp),
            colors = IconButtonDefaults.filledIconButtonColors()
        ) {
            Icon(
                imageVector = if(keyboardToolBar.keyboardVisible){
                    Icons.Default.KeyboardHide
                } else {
                    Icons.Default.Keyboard
                },
                contentDescription = if(keyboardToolBar.keyboardVisible){
                    "Hide Keyboard"
                } else {
                    "Show Keyboard"
                },
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Horizontal Divider
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(36.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )

        // Entry toolbar options
        LazyRow (
            modifier = Modifier.align(Alignment.CenterVertically),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(keyboardToolBar.keyboardToolBarItems){ item ->
                IconButton(
                    onClick = item.onClick,
                    modifier = Modifier.size(56.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}