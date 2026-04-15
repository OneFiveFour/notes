package net.onefivefour.echolist.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
fun ElTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    style: TextStyle = EchoListTheme.typography.bodyMedium,
    color: Color = EchoListTheme.materialColors.onSurface,
    singleLine: Boolean = false,
    imeAction: ImeAction = ImeAction.Default,
    onKeyboardAction: (() -> Unit)? = null,
    onFocusLost: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null
) {
    var wasFocused by remember(state) { mutableStateOf(false) }

    BasicTextField(
        state = state,
        modifier = modifier
            .then(if (focusRequester == null) Modifier else Modifier.focusRequester(focusRequester))
            .then(
                if (onFocusLost == null) {
                    Modifier
                } else {
                    Modifier.onFocusChanged { focusState ->
                        if (wasFocused && !focusState.isFocused) {
                            onFocusLost()
                        }
                        wasFocused = focusState.isFocused
                    }
                }
            ),
        textStyle = style.copy(color = color),
        cursorBrush = SolidColor(EchoListTheme.materialColors.primary),
        lineLimits = if (singleLine) TextFieldLineLimits.SingleLine else TextFieldLineLimits.MultiLine(),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        onKeyboardAction = onKeyboardAction?.let { keyboardAction ->
            { keyboardAction() }
        }
    )
}

@Preview
@Composable
private fun ElTextFieldPreview() {
    val state = remember { TextFieldState(initialText = "Sample text") }
    EchoListTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ElTextField(state = state)
        }
    }
}
