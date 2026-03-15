package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.text.input.TextFieldState

data class EditNoteUiState(
    val titleState: TextFieldState,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isSaveEnabled: Boolean
        get() = titleState.text.isNotBlank() && !isLoading
}
