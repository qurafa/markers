package com.example.markerclient.ui.elements.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.markerclient.domain.model.EntryItem
import java.io.File
import kotlin.text.ifEmpty

@Composable
fun ImageItemComposable(
    imageItem: EntryItem.ImageItem,
    onCaptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val captionState = rememberTextFieldState(imageItem.caption)

    LaunchedEffect(captionState.text) {
        if (captionState.text.toString() != imageItem.caption) {
            onCaptionChange(captionState.text.toString())
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(File(imageItem.imageUri))
                .crossfade(true)
                .build(),
            contentDescription = imageItem.caption.ifEmpty { "Entry image" },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.FillWidth
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Caption
        EditableTextField(
            textFieldState = captionState,
            placeholder = "Add caption...",
            singleLine = false,
            minLines = 1,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}