package net.onefivefour.echolist.ui.home

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
import net.onefivefour.echolist.data.dto.UpdateFolderParams
import net.onefivefour.echolist.data.repository.normalizePath
import net.onefivefour.echolist.domain.repository.FileRepository

class RenameFolderViewModel(
    private val parentDir: String,
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RenameFolderUiState())
    val uiState: StateFlow<RenameFolderUiState> = _uiState.asStateFlow()

    private val _navigateToFolder = MutableSharedFlow<String>()
    val navigateToFolder: SharedFlow<String> = _navigateToFolder.asSharedFlow()

    fun showDialog() {
        val currentName = parentDir.trimEnd('/').substringAfterLast('/')
        _uiState.update {
            RenameFolderUiState(isVisible = true, folderName = currentName)
        }
    }

    fun dismissDialog() {
        _uiState.update {
            RenameFolderUiState()
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(folderName = value, error = null) }
    }

    fun onConfirm() {
        val trimmedName = _uiState.value.folderName.trim()
        if (trimmedName.isBlank()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val normalizedPath = normalizePath(parentDir)
            val result = fileRepository.updateFolder(
                UpdateFolderParams(folderPath = normalizedPath, newName = trimmedName)
            )
            result.fold(
                onSuccess = { folder ->
                    _uiState.update { RenameFolderUiState() }
                    _navigateToFolder.emit(normalizePath(folder.path))
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message)
                    }
                }
            )
        }
    }
}
