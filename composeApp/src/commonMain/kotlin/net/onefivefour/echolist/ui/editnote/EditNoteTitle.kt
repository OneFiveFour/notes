package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun EditNoteTitle(uiState: EditNoteUiState) {
    if (uiState.isCreateMode) {
        EditNoteTextField(
            modifier = Modifier.Companion.fillMaxWidth(),
            state = uiState.titleState,
            placeholder = "Note title",
            textStyle = EchoListTheme.typography.titleLarge
        )
    } else {
        Text(
            text = uiState.titleState.text.toString(),
            style = EchoListTheme.typography.titleLarge,
            color = EchoListTheme.materialColors.primary
        )
    }
}