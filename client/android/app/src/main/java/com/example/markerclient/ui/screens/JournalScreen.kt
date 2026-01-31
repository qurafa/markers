package com.example.markerclient.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.markerclient.R
import com.example.markerclient.domain.CustomDismissCard
import com.example.markerclient.domain.Entry
import com.example.markerclient.domain.ToolbarTrailingButton
import com.example.markerclient.domain.viewModel.EntryViewModel
import com.example.markerclient.ui.components.EntryContent
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JournalScreen(modifier : Modifier,
                  entryViewModel: EntryViewModel,
                  navigationTrailingButton: ToolbarTrailingButton,
                  customDismissCard : CustomDismissCard)
{
    val scope = rememberCoroutineScope()

    // get all entries in the repository
    val entries = entryViewModel.entries.collectAsState()

    // Set Navigation trailing button and reset custom dismiss card
    LaunchedEffect(Unit) {
        customDismissCard.reset()

        navigationTrailingButton.updateButton(
            visible = true,
            icon = Icons.Filled.Add,
            action = {
                scope.launch {
                    // add a new entry to the repository and launch dismiss card if valid on complete
                    val entry = Entry()
                    entryViewModel.addEntry(entry){ entryId ->
                        if(entryId > 0){
                            customDismissCard.update(visible = true,
                                modifier = customDismissCard.cardModifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                content = { EntryContent(
                                    entry = entry.update(id = entryId)
                                ) },
                                rtlDismiss = true,
                                ltrDismiss = true ,
                                onDismiss = {
                                    entryViewModel.updateEntry(entry) // update entry in repository
                                    customDismissCard.reset()
                                    Log.d("JournalScreen", "EntryCard Dismiss")
                                }
                            )
                        }
                    }
                }
            }
        )
    }

    val titleTextStyle = TextStyle(
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Box(modifier = modifier
        .fillMaxSize()
    )
    {
        LazyColumn (modifier = Modifier
                .fillMaxSize()
                .padding(0.dp,0.dp,0.dp,95.dp)
                .align(Alignment.TopCenter)
                .zIndex(1f)
        )
        {
            item{
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f)
                        .windowInsetsPadding(WindowInsets.systemBars),// so it doesn't overlap the system bars
                    text = "Journal",
                    style = titleTextStyle
                )
            }

            items(items = entries.value,
                key = {"entry_${it.entryId}"}){ entry ->

                JournalEntryCard(
                    modifier = Modifier.fillMaxWidth()
                        .height(125.dp)
                        .padding(8.dp),
                    entry = entry,
                    entryViewModel = entryViewModel,
                    customDismissCard = customDismissCard
                )
            }
        }
    }
}

@Composable
fun JournalEntryCard(
    modifier: Modifier = Modifier,
    entry: Entry,
    entryViewModel: EntryViewModel,
    customDismissCard : CustomDismissCard){

    Box(modifier = modifier){
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier.fillMaxSize(),
            onClick = {
                customDismissCard.update(visible = true,
                    modifier = customDismissCard.cardModifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    content = {
                        EntryContent(
                            modifier = Modifier.windowInsetsPadding(WindowInsets.ime), // So our items aren't blocked by the keyboard
                            entry = entry
                        ) },
                    rtlDismiss = true,
                    ltrDismiss = true ,
                    onDismiss = {
                        entryViewModel.updateEntry(entry) // update entry in repository on dismiss
                        customDismissCard.reset()
                        Log.d("JournalScreen", "EntryCard Dismiss")
                    }
                )
            }
        ){
            Text(text = entry.entryTitle,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Left)
        }

        // Delete Entry Button
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd)
                .wrapContentSize(),
            onClick = {
                entryViewModel.deleteEntry(entry)
            }
        ) {
            Icon(
                Icons.Filled.RemoveCircle,
                contentDescription = "Remove Entry Button",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

