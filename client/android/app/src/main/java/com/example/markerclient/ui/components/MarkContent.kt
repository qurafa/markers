package com.example.markerclient.ui.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import com.example.markerclient.R
import com.example.markerclient.domain.Mark
import com.example.markerclient.domain.MarkViewModel
import com.mapbox.maps.extension.compose.style.layers.ModelIdValue
import java.nio.file.WatchEvent
import com.mapbox.geojson.Point

@Composable
fun MarkContent(modifier : Modifier = Modifier, mark : Mark = Mark(), markViewModel : MarkViewModel){
    var titleFieldState = rememberTextFieldState(
        initialText = mark.markTitle
    )
    val titleTextStyle = TextStyle(
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold
    )

    // Get Mark Point Geometry and coordinates
    val coord : Point = mark.markFeature?.geometry() as Point
    var coordText by remember {
        mutableStateOf(coord.coordinates().toString())
    }
    val coordTextStyle = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
    )

    var descFieldState = rememberTextFieldState(
        initialText = mark.markDescription
    )
    val descTextStyle = TextStyle(
        fontSize = 20.sp
    )

    // Content
    Column (
        modifier = modifier
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
        ){
            // Cover Image
            Image(
                painter = painterResource(id = R.drawable.mark),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                contentDescription = "CoverImage"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                            startY = 100f
                        )
                    )
            )

            Column(modifier = Modifier
                .align(Alignment.BottomStart)
                .wrapContentSize()
                .padding(16.dp)){

                Row(modifier = Modifier
                    .wrapContentSize()){
                    // Title Editable Text
                    EditableText(
                        modifier = Modifier
                            .wrapContentWidth()
                            .width(IntrinsicSize.Min),
                        textFieldState = titleFieldState,
                        textStyle = titleTextStyle
                    ){
                        mark.update(title = titleFieldState.text.toString())
                    }
                    // coord Text
                    Text(
                        text = coordText,
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(10.dp),
                        style = coordTextStyle
                    )
                }

                // Mark Tags...
                Row{}
            }

            // Save Mark Icon Button
            Row(
                modifier = Modifier
                .align(Alignment.TopEnd)
                .wrapContentSize()
            ) {
                if(mark.markIsBuffer) {
                    // Save Mark Button
                    IconButton(
                        onClick = {
                            mark.update(isBuffer = false)
                            markViewModel.updateMark(mark) // Update in repository immediately
                        }
                    ) {
                        Icon(
                            Icons.Filled.AddCircle,
                            contentDescription = stringResource(R.string.map_screen)
                        )
                    }
                }
                else {
                    IconButton(
                        onClick = {
                            mark.update(isBuffer = true)
                            markViewModel.updateMark(mark) // Update in repository immediately
                        }
                    ) {
                        Icon(
                            Icons.Filled.RemoveCircle,
                            contentDescription = stringResource(R.string.map_screen)
                        )
                    }
                }
            }
        }

        // Bottom Column
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp)
        ){
            // Description Text Field
            EditableText(
                textFieldState = descFieldState,
                textStyle = descTextStyle
            ){
                mark.update(desc = descFieldState.text.toString())
            }
        }
    }
}

@Preview
@Composable
fun MarkContentPreview()
{
//    MarkContent()
}

@Composable
fun EditableText(
    modifier: Modifier = Modifier,
    textFieldState : TextFieldState = rememberTextFieldState(),
    textStyle : TextStyle = TextStyle(),
    onDoneAction : () -> Unit = {}
) {
    var isEditing by remember {mutableStateOf(false)}
    val textFieldState = textFieldState

    if(isEditing){
        OutlinedTextField(
            modifier = modifier,
            state = textFieldState,
            textStyle = textStyle,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = {
                isEditing = false
                onDoneAction()
            }
        )
    }
    else
    {
        Text(
            modifier = modifier
                .clickable{
                    isEditing = true
                },
            text = textFieldState.text.toString(),
            style = textStyle
        )
    }
}