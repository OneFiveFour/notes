package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.data.dto.CreateNoteParams
import net.onefivefour.echolist.data.dto.UpdateNoteParams
import net.onefivefour.echolist.domain.repository.NotesRepository

class EditNoteViewModel(
    private val mode: EditNoteMode,
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val titleState = TextFieldState()

    private val _uiState = MutableStateFlow(
        EditNoteUiState(
            titleState = titleState,
            mode = mode,
            isLoading = mode is EditNoteMode.Edit
        )
    )
    val uiState: StateFlow<EditNoteUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    init {
        if (mode is EditNoteMode.Edit) {
            loadNote(mode.noteId)
        }
    }

    fun onPreviewToggle() {
        _uiState.update { it.copy(isPreview = !it.isPreview) }
    }

    fun onToolbarAction(action: MarkdownToolbarAction) {
        val contentState = _uiState.value.contentState
        val text = contentState.text.toString()
        val formatted = MarkdownFormatter.apply(
            action = action,
            text = text,
            selection = contentState.selection
        )

        contentState.edit {
            replace(0, length, formatted.text)
            selection = TextRange(formatted.selectionStart, formatted.selectionEnd)
        }
    }

    fun onSaveClick() {
        val currentMode = _uiState.value.mode
        val trimmedTitle = titleState.text.toString().trim()
        if (trimmedTitle.isBlank()) return

        val content = _uiState.value.contentState.text.toString()

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val result = when (currentMode) {
                is EditNoteMode.Create -> notesRepository.createNote(
                    CreateNoteParams(
                        title = trimmedTitle,
                        content = content,
                        parentDir = currentMode.parentPath
                    )
                )
                is EditNoteMode.Edit -> notesRepository.updateNote(
                    UpdateNoteParams(
                        id = currentMode.noteId,
                        content = content
                    )
                )
            }
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false) }
                    _navigateBack.emit(Unit)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
            )
        }
    }

    private fun loadNote(noteId: String) {
        viewModelScope.launch {
            val result = notesRepository.getNote(noteId)
            result.fold(
                onSuccess = { note ->
                    titleState.edit {
                        replace(0, length, note.title)
                    }
                    _uiState.value.contentState.edit {
                        replace(0, length, note.content)
                    }
                    _uiState.update { it.copy(isLoading = false, error = null) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }
}
