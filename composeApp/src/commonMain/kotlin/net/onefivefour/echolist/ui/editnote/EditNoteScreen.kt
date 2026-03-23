package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.ui.common.ElButton
import net.onefivefour.echolist.ui.common.GradientBackground
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

        EditNoteTitle(
            isCreateMode = uiState.isCreateMode,
            textFieldState = uiState.titleState
        )

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
        ) {
            Box(modifier = Modifier.padding(dimensions.m)) {
                when {
                    uiState.isLoading -> EditNoteLoading()

                    uiState.isPreview -> EditNotePreview(uiState)

                    else -> EditNoteTextField(textFieldState = uiState.contentState)
                }
            }
        }

        uiState.error?.let { errorMessage ->
            EditNoteError(errorMessage)
        }

        Spacer(modifier = Modifier.height(dimensions.m))

        EditNoteToolbar(
            uiState = uiState,
            onToolbarAction = onToolbarAction,
            onPreviewToggle = onPreviewToggle
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


@Preview
@Composable
private fun EditNoteScreenPreview() {
    EchoListTheme {
        GradientBackground {
            EditNoteScreen(
                uiState = EditNoteUiState(
                    titleState = TextFieldState(initialText = "Title"),
                    contentState = TextFieldState(initialText = "Content"),
                    mode = EditNoteMode.Edit(filePath = "/path/to/file"),
                    isLoading = false,
                    isSaving = false,
                    isPreview = true
                ),

                onPreviewToggle = {},
                onToolbarAction = {},
                onSaveClick = {}
            )
        }
    }
}
