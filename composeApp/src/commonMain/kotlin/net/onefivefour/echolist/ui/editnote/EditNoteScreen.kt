package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.ui.common.GradientBackground
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
fun EditNoteScreen(
    uiState: EditNoteUiState,
    onPreviewToggle: () -> Unit,
    onBeginEdit: () -> Unit,
    onToolbarAction: (MarkdownToolbarAction) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = EchoListTheme.dimensions
    val contentFocusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(
                horizontal = dimensions.xl,
                vertical = dimensions.l
            )
    ) {

        EditNoteTitle(
            textFieldState = uiState.titleState,
            requestFocus = uiState.isCreateMode,
            onNext = { contentFocusRequester.requestFocus() }
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
            Box(modifier = Modifier
                .padding(dimensions.m)
                .fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> EditNoteLoading()

                    uiState.isPreview -> EditNotePreview(
                        uiState,
                        onBeginEdit = onBeginEdit
                    )

                    else -> EditNoteTextField(
                        textFieldState = uiState.contentState,
                        requestFocus = !uiState.isCreateMode,
                        focusRequester = contentFocusRequester
                    )
                }
            }
        }

        uiState.error?.let { errorMessage ->
            EditNoteError(errorMessage)
        }

        if (!uiState.isPreview) {
            Spacer(modifier = Modifier.height(dimensions.m))
            MarkdownToolbar(
                modifier = Modifier.fillMaxWidth(),
                onToolbarAction = onToolbarAction
            )
        }

        Spacer(modifier = Modifier.height(dimensions.m))

        EditNoteToolbar(
            onSaveClick = onSaveClick,
            onDeleteClick = onDeleteClick,
            onPreviewToggle = onPreviewToggle,
            uiState = uiState
        )
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
                    mode = EditNoteMode.Edit(noteId = "preview-note-id"),
                    isLoading = false,
                    isSaving = false,
                    isPreview = true
                ),

                onPreviewToggle = {},
                onToolbarAction = {},
                onSaveClick = {},
                onDeleteClick = {},
                onBeginEdit = {}
            )
        }
    }
}
