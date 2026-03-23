package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun EditNotePreview(uiState: EditNoteUiState) {
    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (uiState.contentState.text.isEmpty()) {
            Text(
                text = "Nothing to preview yet.",
                style = EchoListTheme.typography.bodyMedium,
                color = EchoListTheme.materialColors.onSurface.copy(alpha = 0.6f)
            )
        } else {
            MarkdownPreview(
                document = uiState.contentState.text.toString(),
                modifier = Modifier.Companion.fillMaxWidth()
            )
        }
    }
}