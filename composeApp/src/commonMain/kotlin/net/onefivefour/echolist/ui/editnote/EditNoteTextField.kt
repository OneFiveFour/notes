package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun EditNoteTextField(
    state: TextFieldState,
    placeholder: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier.Companion
) {
    Box(modifier = modifier) {
        if (state.text.isEmpty()) {
            Text(
                text = placeholder,
                style = textStyle,
                color = EchoListTheme.materialColors.onSurface.copy(alpha = 0.45f)
            )
        }

        BasicTextField(
            modifier = Modifier.Companion.fillMaxSize(),
            state = state,
            textStyle = textStyle.copy(
                color = EchoListTheme.materialColors.onSurface
            )
        )
    }
}