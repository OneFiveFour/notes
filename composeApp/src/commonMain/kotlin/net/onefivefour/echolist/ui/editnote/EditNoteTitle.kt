package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun EditNoteTitle(
    isCreateMode: Boolean,
    textFieldState: TextFieldState
) {
    if (isCreateMode) {
        BasicTextField(
            modifier = Modifier.fillMaxWidth(),
            state = textFieldState,
            textStyle = EchoListTheme.typography.titleLarge.copy(
                color = EchoListTheme.materialColors.onSurface
            )
        )
    } else {
        Text(
            text = textFieldState.text.toString(),
            style = EchoListTheme.typography.titleLarge,
            color = EchoListTheme.materialColors.primary
        )
    }
}