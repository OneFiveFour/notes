package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.text.input.TextFieldState

data class EditNoteUiState(
    val titleState: TextFieldState,
    val contentState: TextFieldState = TextFieldState(),
    val mode: EditNoteMode = EditNoteMode.Create(""),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isPreview: Boolean = true,
    val error: String? = null
) {
    val isSaveEnabled: Boolean
        get() = titleState.text.isNotBlank() && !isLoading && !isSaving

    val isCreateMode: Boolean
        get() = mode is EditNoteMode.Create
}
