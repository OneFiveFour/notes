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
internal fun EditNoteTextField(textFieldState: TextFieldState) {

    Box(modifier = Modifier.fillMaxSize()) {
        if (textFieldState.text.isEmpty()) {
            Text(
                text = "Write your note in markdown...",
                style = EchoListTheme.typography.bodyMedium,
                color = EchoListTheme.materialColors.onSurface.copy(alpha = 0.45f)
            )
        }

        BasicTextField(
            modifier = Modifier.fillMaxSize(),
            state = textFieldState,
            textStyle = EchoListTheme.typography.bodyMedium.copy(
                color = EchoListTheme.materialColors.onSurface
            )
        )
    }
}