package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.ui.common.ElButton
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
fun EditNoteScreen(
    uiState: EditNoteUiState,
    onPreviewToggle: () -> Unit,
    onToolbarAction: (MarkdownToolbarAction) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = EchoListTheme.dimensions

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = dimensions.xl,
                vertical = dimensions.l
            )
    ) {

        EditNoteTitle(uiState)

        Spacer(modifier = Modifier.height(dimensions.m))

        Surface(
            shape = EchoListTheme.shapes.medium,
            color = EchoListTheme.materialColors.surface,
            modifier = modifier
                .weight(1f)
                .border(
                    width = EchoListTheme.dimensions.borderWidth,
                    color = EchoListTheme.materialColors.surfaceVariant,
                    shape = EchoListTheme.shapes.medium
                )
                .padding(EchoListTheme.dimensions.m)
        ) {
            when {
                uiState.isLoading -> EditNoteLoading()

                uiState.isPreview -> EditNotePreview(uiState)

                else -> EditNoteTextField(
                    state = uiState.contentState,
                    placeholder = "Write your note in markdown...",
                    modifier = Modifier.fillMaxSize(),
                    textStyle = EchoListTheme.typography.bodyMedium
                )
            }
        }

        uiState.error?.let { errorMessage ->
            EditNoteError(dimensions, errorMessage)
        }

        Spacer(modifier = Modifier.height(dimensions.m))

        EditNoteToolbar(
            uiState,
            onToolbarAction,
            onPreviewToggle
        )

        Spacer(modifier = Modifier.height(dimensions.m))

        ElButton(
            onClick = onSaveClick,
            isEnabled = uiState.isSaveEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (uiState.isSaving) "Saving..." else "Save",
                style = EchoListTheme.typography.labelMedium
            )
        }
    }
}

