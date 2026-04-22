package com.example.markerclient.ui.elements.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Editable Text
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
                }
                .padding(16.dp),
            text = textFieldState.text.toString(),
            style = textStyle
        )
    }
}

// Outlined TextField with transparent background when not highlighted
@Composable
fun EditableTextField(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState = rememberTextFieldState(),
    textStyle: TextStyle = TextStyle(),
    placeholder: String = "Tap to edit",
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    showBorder: Boolean = false,
    borderWidth: Dp = 1.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
    borderTop: Boolean = true,
    borderBottom: Boolean = true,
    borderStart: Boolean = true,
    borderEnd: Boolean = true,
    focusManager: FocusManager = LocalFocusManager.current,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    onFocusChanged: () -> Unit = {},
    onDoneAction: () -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    val keyboardVisible by rememberKeyboardAsState()

    val currentBorderColor = if (isEditing) focusedBorderColor else borderColor

    OutlinedTextField(
        state = textFieldState,
        modifier = modifier
            .then(
                if (showBorder) {
                    Modifier.selectiveBorder(
                        width = borderWidth,
                        color = currentBorderColor,
                        top = borderTop,
                        bottom = borderBottom,
                        start = borderStart,
                        end = borderEnd
                    )
                } else {
                    Modifier
                }
            )
            .onFocusChanged { focusState ->
                val wasFocused = isEditing
                isEditing = focusState.isFocused
                if (wasFocused != isEditing) {
                    if (!isEditing) {
                        onDoneAction()
                    }
                    onFocusChanged()
                }
            },
        textStyle = textStyle,
        lineLimits = if (singleLine) {
            TextFieldLineLimits.SingleLine
        } else {
            TextFieldLineLimits.MultiLine(
                minHeightInLines = minLines,
                maxHeightInLines = maxLines
            )
        },
        keyboardOptions = KeyboardOptions(
            imeAction = if (singleLine) ImeAction.Done else ImeAction.Default,
            capitalization = KeyboardCapitalization.Sentences
        ),
        onKeyboardAction = { action ->
            if (singleLine) {
                isEditing = false
                focusManager.clearFocus()
                keyboardController?.hide()
                onDoneAction()
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,// MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        placeholder = {
            Text(
                text = placeholder,
                style = textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    )

    LaunchedEffect(keyboardVisible) {
        if (!keyboardVisible && isEditing) {
            focusManager.clearFocus()
        }
    }
}

@Composable
fun rememberKeyboardAsState(): State<Boolean> {
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current

    return remember {
        derivedStateOf {
            imeInsets.getBottom(density) > 0
        }
    }
}

// Simpler version with same border on all selected sides
fun Modifier.selectiveBorder(
    width: Dp = 1.dp,
    color: Color = Color.Black,
    top: Boolean = false,
    bottom: Boolean = false,
    start: Boolean = false,
    end: Boolean = false
) = this.drawBehind {
    val strokeWidthPx = width.toPx()

    if (top) {
        drawLine(
            color = color,
            start = Offset(0f, strokeWidthPx / 2),
            end = Offset(size.width, strokeWidthPx / 2),
            strokeWidth = strokeWidthPx
        )
    }

    if (bottom) {
        drawLine(
            color = color,
            start = Offset(0f, size.height - strokeWidthPx / 2),
            end = Offset(size.width, size.height - strokeWidthPx / 2),
            strokeWidth = strokeWidthPx
        )
    }

    if (start) {
        drawLine(
            color = color,
            start = Offset(strokeWidthPx / 2, 0f),
            end = Offset(strokeWidthPx / 2, size.height),
            strokeWidth = strokeWidthPx
        )
    }

    if (end) {
        drawLine(
            color = color,
            start = Offset(size.width - strokeWidthPx / 2, 0f),
            end = Offset(size.width - strokeWidthPx / 2, size.height),
            strokeWidth = strokeWidthPx
        )
    }
}
