package net.onefivefour.echolist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.domain.repository.FileRepository

class CreateFolderViewModel(
    private val currentPath: String,
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateFolderUiState())
    val uiState: StateFlow<CreateFolderUiState> = _uiState.asStateFlow()

    fun showDialog() {
        _uiState.update {
            CreateFolderUiState(isVisible = true)
        }
    }

    fun dismissDialog() {
        _uiState.update {
            CreateFolderUiState()
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
            val result = fileRepository.createFolder(
                CreateFolderParams(parentDir = currentPath, name = trimmedName)
            )
            result.fold(
                onSuccess = {
                    _uiState.update { CreateFolderUiState() }
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
