package net.onefivefour.notes.ui.notedetail

sealed interface NoteDetailUiState {
    data object Loading : NoteDetailUiState
    data class Success(
        val title: String,
        val content: String,
        val lastUpdated: String
    ) : NoteDetailUiState
    data class Error(val message: String) : NoteDetailUiState
}
