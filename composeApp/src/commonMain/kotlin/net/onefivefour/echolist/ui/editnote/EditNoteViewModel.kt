package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.text.input.TextFieldState
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
import net.onefivefour.echolist.data.models.CreateNoteParams
import net.onefivefour.echolist.data.repository.NotesRepository

class EditNoteViewModel(
    private val parentPath: String,
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val titleState = TextFieldState()

    private val _uiState = MutableStateFlow(EditNoteUiState(titleState = titleState))
    val uiState: StateFlow<EditNoteUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    fun onSaveClick() {
        val trimmedTitle = titleState.text.toString().trim()
        if (trimmedTitle.isBlank()) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = notesRepository.createNote(
                CreateNoteParams(
                    title = trimmedTitle,
                    content = "",
                    parentDir = parentPath
                )
            )
            result.fold(
                onSuccess = { _navigateBack.emit(Unit) },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }
}
