package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.data.dto.CreateTaskListParams
import net.onefivefour.echolist.data.models.UpdateTaskListParams
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.TaskList
import net.onefivefour.echolist.domain.repository.TaskListRepository

internal class EditTaskListViewModel(
    private val mode: EditTaskListMode,
    private val taskListRepository: TaskListRepository
) : ViewModel() {

    private data class SyncSnapshot(
        val title: String,
        val tasks: List<MainTask>,
        val isAutoDelete: Boolean
    )

    private val titleState = TextFieldState()
    private val tasks = mutableStateListOf<UiMainTask>()
    private var nextDraftId = 1L
    private var nextSubTaskDraftId = 1L
    private var persistedTaskListId: String? = (mode as? EditTaskListMode.Edit)?.taskListId
    private var lastSuccessfulSnapshot: SyncSnapshot? = null
    private var syncQueued = false
    private var syncJob: Job? = null

    private val dueDatePattern = Regex("""^\d{4}-\d{2}-\d{2}$""")
    private val titleRequiredMessage = "Title cannot be blank."

    private val _uiState = MutableStateFlow(
        EditTaskListUiState(
            titleState = titleState,
            mainTasks = tasks,
            mode = mode,
            isPersisted = mode is EditTaskListMode.Edit,
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
        val draft = UiMainTask(id = nextDraftId++)
        tasks.add(draft)
        observeDueDateRecurrenceExclusion(draft)
        _uiState.update { it.copy(error = null) }
    }

    fun onRemoveMainTask(index: Int) {
        if (index !in tasks.indices) return
        tasks.removeAt(index)
        requestSync()
    }

    fun onMainTaskCheckedChange(index: Int, isChecked: Boolean) {
        val task = tasks.getOrNull(index) ?: return

        if (_uiState.value.isAutoDelete && isChecked) {
            tasks.removeAt(index)
        } else {
            task.isDone = isChecked
        }

        requestSync()
    }

    fun onAddSubTask(mainTaskIndex: Int) {
        val task = tasks.getOrNull(mainTaskIndex) ?: return
        task.subTasks.add(UiSubTask(subTaskId = nextSubTaskDraftId++))
        _uiState.update { it.copy(error = null) }
    }

    fun onSubTaskCheckedChange(mainTaskIndex: Int, subTaskIndex: Int, isChecked: Boolean) {
        val task = tasks.getOrNull(mainTaskIndex) ?: return
        if (subTaskIndex !in task.subTasks.indices) return

        if (_uiState.value.isAutoDelete && isChecked) {
            task.subTasks.removeAt(subTaskIndex)
        } else {
            task.subTasks[subTaskIndex].isDone = isChecked
        }

        requestSync()
    }

    fun onToggleAutoDelete(isAutoDelete: Boolean) {
        _uiState.update { it.copy(isAutoDelete = isAutoDelete) }
        requestSync()
    }

    fun onFieldFocusLost() {
        requestSync()
    }

    fun onDeleteClick() {
        val taskListId = persistedTaskListId ?: return

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            taskListRepository.deleteTaskList(taskListId).fold(
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

                    var maxSubTaskId = 0L
                    taskList.tasks.forEach { task ->
                        val draft = UiMainTask.fromDomain(nextDraftId++, task)
                        tasks.add(draft)
                        maxSubTaskId = maxOf(
                            maxSubTaskId,
                            draft.subTasks.maxOfOrNull { it.subTaskId } ?: 0L
                        )
                        observeDueDateRecurrenceExclusion(draft)
                    }

                    nextSubTaskDraftId = maxOf(nextSubTaskDraftId, maxSubTaskId + 1L)
                    lastSuccessfulSnapshot = taskList.toSyncSnapshot()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAutoDelete = taskList.isAutoDelete,
                            isPersisted = true,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    private fun requestSync() {
        syncQueued = true
        if (syncJob?.isActive == true) return

        syncJob = viewModelScope.launch {
            processSyncQueue()
        }
    }

    private suspend fun processSyncQueue() {
        while (syncQueued) {
            syncQueued = false

            val snapshot = buildSyncSnapshot() ?: continue
            if (snapshot == lastSuccessfulSnapshot) {
                _uiState.update { it.copy(error = null) }
                continue
            }

            _uiState.update { it.copy(isSaving = true, error = null) }

            val result = persistedTaskListId?.let { taskListId ->
                taskListRepository.updateTaskList(
                    UpdateTaskListParams(
                        id = taskListId,
                        title = snapshot.title,
                        tasks = snapshot.tasks,
                        isAutoDelete = snapshot.isAutoDelete
                    )
                )
            } ?: taskListRepository.createTaskList(
                CreateTaskListParams(
                    name = snapshot.title,
                    path = (mode as EditTaskListMode.Create).parentPath,
                    tasks = snapshot.tasks,
                    isAutoDelete = snapshot.isAutoDelete
                )
            )

            result.fold(
                onSuccess = { taskList ->
                    persistedTaskListId = taskList.id
                    lastSuccessfulSnapshot = taskList.toSyncSnapshot()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSaving = false,
                            isAutoDelete = taskList.isAutoDelete,
                            isPersisted = true,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSaving = false,
                            error = e.message
                        )
                    }
                }
            )
        }
    }

    private fun buildSyncSnapshot(): SyncSnapshot? {
        validateDrafts()?.let { message ->
            _uiState.update { it.copy(error = message, isSaving = false) }
            return null
        }

        val trimmedTitle = titleState.text.toString().trim()
        val normalizedTasks = tasks.mapNotNull { it.toDomain() }

        if (trimmedTitle.isBlank()) {
            if (persistedTaskListId != null) {
                _uiState.update { it.copy(error = titleRequiredMessage, isSaving = false) }
            } else {
                _uiState.update { it.copy(error = null, isSaving = false) }
            }
            return null
        }

        if (persistedTaskListId == null && normalizedTasks.isEmpty()) {
            _uiState.update { it.copy(error = null, isSaving = false) }
            return null
        }

        return SyncSnapshot(
            title = trimmedTitle,
            tasks = normalizedTasks,
            isAutoDelete = _uiState.value.isAutoDelete
        )
    }

    private fun observeDueDateRecurrenceExclusion(draft: UiMainTask) {
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
            if (task.descriptionState.text.toString().trim().isBlank()) return@forEach

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

    private fun TaskList.toSyncSnapshot(): SyncSnapshot = SyncSnapshot(
        title = name,
        tasks = tasks,
        isAutoDelete = isAutoDelete
    )
}
