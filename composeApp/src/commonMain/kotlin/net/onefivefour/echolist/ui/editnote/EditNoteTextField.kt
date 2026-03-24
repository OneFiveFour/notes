package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun EditNoteTextField(
    textFieldState: TextFieldState,
    requestFocus: Boolean = true
) {

    val focusRequester = remember { FocusRequester() }

    if (requestFocus) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (textFieldState.text.isEmpty()) {
            Text(
                text = "Write your note in markdown...",
                style = EchoListTheme.typography.bodyMedium,
                color = EchoListTheme.materialColors.onSurface.copy(alpha = 0.45f)
            )
        }

        BasicTextField(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester),
            state = textFieldState,
            textStyle = EchoListTheme.typography.bodyMedium.copy(
                color = EchoListTheme.materialColors.onSurface
            )
        )
    }
}