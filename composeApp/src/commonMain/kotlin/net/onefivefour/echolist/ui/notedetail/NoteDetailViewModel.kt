package net.onefivefour.echolist.ui.notedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.onefivefour.echolist.data.repository.NotesRepository

class NoteDetailViewModel(
    private val noteId: String,
    private val repository: NotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoteDetailUiState>(NoteDetailUiState.Loading)
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadNote()
        }
    }

    private suspend fun loadNote() {
        val result = repository.getNote(noteId)
        result.fold(
            onSuccess = { note ->
                _uiState.value = NoteDetailUiState.Success(
                    title = note.title,
                    content = note.content,
                    lastUpdated = formatTimestamp(note.updatedAt)
                )
            },
            onFailure = { error ->
                _uiState.value = NoteDetailUiState.Error(
                    message = error.message ?: "Note not found"
                )
            }
        )
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    if (epochMillis == 0L) return ""
    val seconds = epochMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
        hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
        else -> "Just now"
    }
}
