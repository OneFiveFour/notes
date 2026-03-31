package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.data.models.CreateTaskListParams
import net.onefivefour.echolist.data.models.UpdateTaskListParams
import net.onefivefour.echolist.domain.repository.TaskListRepository

class EditTaskListViewModel(
    private val mode: EditTaskListMode,
    private val taskListRepository: TaskListRepository
) : ViewModel() {

    private val titleState = TextFieldState()
    private val tasks = mutableStateListOf<MainTaskDraft>()
    private var nextDraftId = 1L
    private var nextSubTaskDraftId = 1L

    private val dueDatePattern = Regex("""^\d{4}-\d{2}-\d{2}$""")

    private val _uiState = MutableStateFlow(
        EditTaskListUiState(
            titleState = titleState,
            tasks = tasks,
            mode = mode,
            isLoading = mode is EditTaskListMode.Edit
        )
    )
    val uiState: StateFlow<EditTaskListUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    init {
        if (mode is EditTaskListMode.Edit) {
            loadTaskList(mode.taskListId)
        }
    }

    fun onAddMainTask() {
        val draft = MainTaskDraft(id = nextDraftId++)
        tasks.add(draft)
        observeDueDateRecurrenceExclusion(draft)
        _uiState.update { it.copy(error = null) }
    }

    fun onRemoveMainTask(index: Int) {
        if (index !in tasks.indices) return
        tasks.removeAt(index)
        _uiState.update { it.copy(error = null) }
    }

    fun onAddSubTask(mainTaskIndex: Int) {
        val task = tasks.getOrNull(mainTaskIndex) ?: return
        task.subTasks.add(SubTaskDraft(id = nextSubTaskDraftId++))
        _uiState.update { it.copy(error = null) }
    }

    fun onRemoveSubTask(mainTaskIndex: Int, subTaskIndex: Int) {
        val task = tasks.getOrNull(mainTaskIndex) ?: return
        if (subTaskIndex !in task.subTasks.indices) return
        task.subTasks.removeAt(subTaskIndex)
        _uiState.update { it.copy(error = null) }
    }

    fun onSaveClick() {
        val trimmedTitle = titleState.text.toString().trim()
        if (trimmedTitle.isBlank()) return

        validateDrafts()?.let { message ->
            _uiState.update { it.copy(error = message) }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val result = when (val currentMode = mode) {
                is EditTaskListMode.Create -> taskListRepository.createTaskList(
                    CreateTaskListParams(
                        name = trimmedTitle,
                        path = currentMode.parentPath,
                        tasks = tasks.mapNotNull { it.toDomain() }
                    )
                )

                is EditTaskListMode.Edit -> taskListRepository.updateTaskList(
                    UpdateTaskListParams(
                        id = currentMode.taskListId,
                        title = trimmedTitle,
                        tasks = tasks.mapNotNull { it.toDomain() }
                    )
                )
            }
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSaving = false) }
                    _navigateBack.emit(Unit)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, isSaving = false, error = e.message) }
                }
            )
        }
    }

    fun onDeleteClick() {
        val editMode = mode as? EditTaskListMode.Edit ?: return

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            taskListRepository.deleteTaskList(editMode.taskListId).fold(
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

    private fun loadTaskList(taskListId: String) {
        viewModelScope.launch {
            taskListRepository.getTaskList(taskListId).fold(
                onSuccess = { taskList ->
                    titleState.edit {
                        replace(0, length, taskList.name)
                    }
                    tasks.clear()
                    taskList.tasks.forEach { task ->
                        val draft = MainTaskDraft.fromDomain(nextDraftId++, task)
                        tasks.add(draft)
                        observeDueDateRecurrenceExclusion(draft)
                    }
                    _uiState.update { it.copy(isLoading = false, error = null) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    private fun observeDueDateRecurrenceExclusion(draft: MainTaskDraft) {
        viewModelScope.launch {
            snapshotFlow { draft.dueDateState.text.toString() }
                .drop(1)
                .collect { value ->
                    if (value.trim().isNotBlank()) {
                        draft.recurrenceState.setTextAndPlaceCursorAtEnd("")
                    }
                }
        }
        viewModelScope.launch {
            snapshotFlow { draft.recurrenceState.text.toString() }
                .drop(1)
                .collect { value ->
                    val sanitized = value.singleLine()
                    if (sanitized != value) {
                        draft.recurrenceState.setTextAndPlaceCursorAtEnd(sanitized)
                    }
                    if (sanitized.isNotBlank()) {
                        draft.dueDateState.setTextAndPlaceCursorAtEnd("")
                    }
                }
        }
    }

    private fun validateDrafts(): String? {
        tasks.forEach { task ->
            val recurrence = task.recurrenceState.text.toString()
            val dueDate = task.dueDateState.text.toString().trim()

            if (recurrence.isNotBlank() && recurrence.any { it == '\n' || it == '\r' }) {
                return "Recurrence must be a single-line RRULE."
            }

            if (recurrence.isBlank() && dueDate.isNotBlank() && !dueDatePattern.matches(dueDate)) {
                return "Due date must use YYYY-MM-DD."
            }
        }
        return null
    }
}
