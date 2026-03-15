package net.onefivefour.echolist.ui.edittasklist

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
import net.onefivefour.echolist.data.models.CreateTaskListParams
import net.onefivefour.echolist.data.repository.TaskListRepository

class EditTaskListViewModel(
    private val parentPath: String,
    private val taskListRepository: TaskListRepository
) : ViewModel() {

    private val titleState = TextFieldState()

    private val _uiState = MutableStateFlow(EditTaskListUiState(titleState = titleState))
    val uiState: StateFlow<EditTaskListUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    fun onSaveClick() {
        val trimmedTitle = titleState.text.toString().trim()
        if (trimmedTitle.isBlank()) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = taskListRepository.createTaskList(
                CreateTaskListParams(
                    name = trimmedTitle,
                    path = parentPath,
                    tasks = emptyList()
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
