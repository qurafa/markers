package com.example.markerclient.ui.components

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.markerclient.domain.CustomKeyboardToolBar
import com.example.markerclient.domain.CustomKeyboardToolBar.KeyboardToolbarItem
import com.example.markerclient.domain.Entry
import com.example.markerclient.domain.EntryItem
import com.example.markerclient.domain.viewModel.EntryItemViewModel
import com.example.markerclient.domain.viewModel.EntryItemViewModelFactory

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun EntryContent(modifier : Modifier = Modifier, entry : Entry) {

    val context = LocalContext.current.applicationContext as Application
    // Initialize ViewModel using the factory
    val entryItemViewModel : EntryItemViewModel = viewModel(
        key = "entryItemViewModel_${entry.entryId}",
        factory = EntryItemViewModelFactory(context, entry)
    )

    var entryTitleFieldState = rememberTextFieldState(
        initialText = entry.entryTitle
    )
    val entryTitleTextStyle = TextStyle(
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold
    )

    val entryItemTextStyle = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic
    )

    val textFocusRequester = remember { FocusRequester() }  // set as for all text fields in this entry
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val entryItems = entryItemViewModel.entryItems.collectAsState()

    Box(modifier = modifier.fillMaxSize())
    {
        // Column of items
        LazyColumn(modifier = Modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime
                .add(WindowInsets(0.dp,0.dp,0.dp,72.dp))))
        {
            item{
                // Title box, always at the top
                Box(modifier = Modifier.fillMaxWidth()
                    .fillMaxHeight(0.1f))
                {
                    EditableTextField(modifier = Modifier
                        .align(Alignment.BottomStart)
                        .wrapContentWidth()
                        .width(IntrinsicSize.Min)
                        .padding(16.dp)
                        .focusRequester(textFocusRequester),
                        singleLine = true,
                        textFieldState = entryTitleFieldState,
                        textStyle = entryTitleTextStyle,
                        focusManager = focusManager,
                        keyboardController = keyboardController)
                    {
                        entry.update(title = entryTitleFieldState.text.toString())
                    }
                }

                // Divider after title //
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.surface)
                )
            }

            items(items = entryItems.value,
                key = { "entryItem_${it.entryItemId}" })
            { entryItem ->
                when(entryItem)
                {
                    is EntryItem.TextItem -> {
                        val textFieldState = remember(key1 = "entryItemText_${entryItem.entryItemId}", key2 = entryItems)
                        {
                            TextFieldState(initialText = entryItem.textContent)
                        }
                        EditableTextField(modifier = Modifier
                            .wrapContentWidth()
                            .width(IntrinsicSize.Min)
                            .padding(4.dp)
                            .focusRequester(textFocusRequester), // set for all text fields
                            textFieldState = textFieldState,
                            textStyle = entryItemTextStyle,
                            showBorder = true,
                            borderWidth = 2.5.dp,
                            borderTop = false,
                            borderBottom = false,
                            borderStart = true,
                            borderEnd = true,
                            focusManager = focusManager,
                            keyboardController = keyboardController
                        )
                        {
                            entryItemViewModel.updateEntryItem(
                                entryItem = entryItem.copy(textContent = textFieldState.text.toString())
                            )
                        }
                    }
                    else -> {
                        Text(text = "Under construction...")
                    }
                }
            }
        }

        // Keyboard Toolbar
        val keyboardToolBar = remember{
            CustomKeyboardToolBar(
                defaultItemsProvider = {
                    listOf(
                        KeyboardToolbarItem(Icons.Default.AddComment, "TextSection"){
                            entryItemViewModel.addEntryItem(
                                EntryItem.TextItem(entryItemId = -1, entryId = entry.entryId)
                            ){ entryItemId ->

                            }
                        }
                    )
                },
                visibleItemsProvider = {
                    listOf(
                        KeyboardToolbarItem(Icons.Default.Book, "EntryReference", {}),
                        KeyboardToolbarItem(Icons.Default.AddLocation, "MarkReference", {}),
                        KeyboardToolbarItem(Icons.Default.Image, "Image", {}),
                        KeyboardToolbarItem(Icons.Default.FormatBold, "Bold", {}),
                        KeyboardToolbarItem(Icons.Default.FormatUnderlined, "Underline", {}),
                        KeyboardToolbarItem(Icons.Default.FormatItalic, "Italic", {})
                    )
                },
                nonVisibleItemsProvider = {
                    listOf(
                        KeyboardToolbarItem(Icons.Default.BorderColor, "Highlight", {}),
                        KeyboardToolbarItem(Icons.Default.Draw, "Draw", {}),
                        KeyboardToolbarItem(Icons.Default.RemoveCircle, "Erase", {})
                    )
                }
            )
        }

        KeyboardToolBar(keyboardToolBar = keyboardToolBar,
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(25.dp, 25.dp, 0.dp, 0.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(5.dp)
                .align(Alignment.BottomCenter),
            focusRequester = textFocusRequester,
            focusManager = focusManager,
            keyboardController = keyboardController
        )
    }
}