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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.data.dto.CreateTaskListParams
import net.onefivefour.echolist.data.models.UpdateTaskListParams
import net.onefivefour.echolist.domain.NotificationScheduler
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.TaskList
import net.onefivefour.echolist.domain.repository.TaskListRepository
import net.onefivefour.echolist.domain.scheduleTaskNotification
import net.onefivefour.echolist.ui.maintasksettings.MainTaskSettingsResultBus

internal class EditTaskListViewModel(
    private val mode: EditTaskListMode,
    private val taskListRepository: TaskListRepository,
    private val settingsResultBus: MainTaskSettingsResultBus,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private data class SyncSnapshot(
        val title: String,
        val mainTasks: List<MainTask>,
        val isAutoDelete: Boolean
    )

    private val titleState = TextFieldState()
    private val uiMainTasks = mutableStateListOf<UiMainTask>()
    private var persistedTaskListId: String? = (mode as? EditTaskListMode.Edit)?.taskListId
    private var lastSuccessfulSnapshot: SyncSnapshot? = null
    private var syncQueued = false
    private var syncJob: Job? = null
    private var skipNextScreenLeftCleanup = false
    private val pendingCompletionTaskIds = mutableSetOf<String>()

    val taskListId: String? get() = persistedTaskListId

    private val dueDatePattern = Regex("""^\d{4}-\d{2}-\d{2}$""")
    private val titleRequiredMessage = "Title cannot be blank."

    private val _uiState = MutableStateFlow(
        EditTaskListUiState(
            titleState = titleState,
            uiMainTasks = uiMainTasks,
            mode = mode,
            isPersisted = mode is EditTaskListMode.Edit,
            isLoading = mode is EditTaskListMode.Edit
        )
    )
    val uiState: StateFlow<EditTaskListUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    @OptIn(ExperimentalUuidApi::class)
    private fun nextDraftMainTaskId() = Uuid.random().toString()
    @OptIn(ExperimentalUuidApi::class)
    private fun nextDraftSubTaskId() = Uuid.random().toString()

    init {
        if (mode is EditTaskListMode.Edit) {
            loadTaskList(mode.taskListId)
        }

        viewModelScope.launch {
            settingsResultBus.results.collect { result ->
                val task = uiMainTasks.firstOrNull { it.id == result.mainTaskId } ?: return@collect
                task.dueDateState.setTextAndPlaceCursorAtEnd(result.dueDate)
                task.recurrenceState.setTextAndPlaceCursorAtEnd(result.recurrence)
                sortTasksByDueDate()
                _uiState.update { it.copy(error = null) }
                requestSync()
            }
        }
    }

    fun onAddMainTask() {
        val draft = UiMainTask(id = nextDraftMainTaskId())
        uiMainTasks.add(draft)
        sortTasksByDueDate()
        observeRecurrenceSanitization(draft)
        _uiState.update { it.copy(error = null) }
    }

    fun onRemoveMainTask(index: Int) {
        if (index !in uiMainTasks.indices) return
        val removedTaskId = uiMainTasks[index].id
        uiMainTasks.removeAt(index)
        viewModelScope.launch(Dispatchers.Default) {
            notificationScheduler.cancel(removedTaskId)
        }
        requestSync()
    }

    fun onMainTaskCheckedChange(index: Int, isChecked: Boolean) {
        val task = uiMainTasks.getOrNull(index) ?: return

        // Block re-taps on recurring tasks until the sync response arrives
        if (task.id in pendingCompletionTaskIds) return

        val isRecurring = task.recurrenceState.text.isNotEmpty()

        if (_uiState.value.isAutoDelete && isChecked) {
            uiMainTasks.removeAt(index)
        } else {
            task.isDone = isChecked
            if (isRecurring && isChecked) {
                pendingCompletionTaskIds.add(task.id)
            }
        }

        requestSync()
    }

    fun onAddSubTask(mainTaskIndex: Int) {
        val task = uiMainTasks.getOrNull(mainTaskIndex) ?: return
        task.subTasks.add(UiSubTask(id = nextDraftSubTaskId()))
        _uiState.update { it.copy(error = null) }
    }

    fun onRemoveSubTask(mainTaskIndex: Int, subTaskIndex: Int) {
        val task = uiMainTasks.getOrNull(mainTaskIndex) ?: return
        if (subTaskIndex !in task.subTasks.indices) return

        task.subTasks.removeAt(subTaskIndex)
        requestSync()
    }

    fun onSubTaskCheckedChange(mainTaskIndex: Int, subTaskIndex: Int, isChecked: Boolean) {
        val task = uiMainTasks.getOrNull(mainTaskIndex) ?: return
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

    fun onSettingsNavigationStarted() {
        skipNextScreenLeftCleanup = true
    }

    fun onScreenLeft() {
        if (skipNextScreenLeftCleanup) {
            skipNextScreenLeftCleanup = false
            return
        }

        stripEmptyTasks()
        requestSync()
    }

    fun onDeleteClick() {
        val taskListId = persistedTaskListId ?: return

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            taskListRepository.deleteTaskList(taskListId).fold(
                onSuccess = {
                    // Cancel notifications for all tasks before navigating back
                    launch(Dispatchers.Default) {
                        for (task in uiMainTasks) {
                            notificationScheduler.cancel(task.id)
                        }
                    }
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

                    uiMainTasks.clear()

                    taskList.tasks.sortedByDueDate().forEach { task ->
                        val draft = UiMainTask.fromDomain(task)
                        uiMainTasks.add(draft)
                        observeRecurrenceSanitization(draft)
                    }

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
                        tasks = snapshot.mainTasks,
                        isAutoDelete = snapshot.isAutoDelete
                    )
                )
            } ?: taskListRepository.createTaskList(
                CreateTaskListParams(
                    name = snapshot.title,
                    parentDir = (mode as EditTaskListMode.Create).parentDir,
                    tasks = snapshot.mainTasks,
                    isAutoDelete = snapshot.isAutoDelete
                )
            )

            result.fold(
                onSuccess = { taskList ->
                    persistedTaskListId = taskList.id
                    lastSuccessfulSnapshot = taskList.toSyncSnapshot()

                    // Apply backend-computed changes for recurring tasks that were just completed
                    if (pendingCompletionTaskIds.isNotEmpty()) {
                        for (serverTask in taskList.tasks) {
                            if (serverTask.id !in pendingCompletionTaskIds) continue
                            val uiTask = uiMainTasks.firstOrNull { it.id == serverTask.id } ?: continue
                            uiTask.isDone = serverTask.isDone
                            uiTask.dueDateState.setTextAndPlaceCursorAtEnd(serverTask.dueDate)
                        }
                        pendingCompletionTaskIds.clear()
                        sortTasksByDueDate()
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSaving = false,
                            isAutoDelete = taskList.isAutoDelete,
                            isPersisted = true,
                            error = null
                        )
                    }

                    // Schedule/cancel notifications for all tasks after successful sync.
                    // Merge local isNotificationEnabled state into server-returned tasks because
                    // the backend does not persist this field (it defaults to true in the mapper).
                    val tasksWithLocalNotificationState = taskList.tasks.map { serverTask ->
                        val localTask = uiMainTasks.firstOrNull { it.id == serverTask.id }
                        if (localTask != null) {
                            serverTask.copy(isNotificationEnabled = localTask.isNotificationEnabled)
                        } else {
                            serverTask
                        }
                    }

                    scheduleNotifications(
                        taskListName = taskList.name,
                        domainTasks = tasksWithLocalNotificationState
                    )
                },
                onFailure = { e ->
                    pendingCompletionTaskIds.clear()
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
        val normalizedTasks = uiMainTasks.mapNotNull { it.toDomain() }.sortedByDueDate()

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
            mainTasks = normalizedTasks,
            isAutoDelete = _uiState.value.isAutoDelete
        )
    }

    private fun stripEmptyTasks() {
        uiMainTasks.forEach { mainTask ->
            mainTask.subTasks.removeAll { it.descriptionState.text.toString().trim().isBlank() }
        }
        uiMainTasks.removeAll { it.descriptionState.text.toString().trim().isBlank() }
        sortTasksByDueDate()
    }

    private fun sortTasksByDueDate() {
        if (uiMainTasks.size < 2) return

        // Keep existing UiMainTask instances so TextFieldState, focus, and cursor state survive autosyncs.
        val sortedTasks = uiMainTasks.sortedByDueDate { it.dueDateState.text.toString() }

        if (uiMainTasks.toList() == sortedTasks) return

        uiMainTasks.clear()
        uiMainTasks.addAll(sortedTasks)
    }

    private fun observeRecurrenceSanitization(draft: UiMainTask) {
        viewModelScope.launch {
            snapshotFlow { draft.recurrenceState.text.toString() }
                .drop(1)
                .collect { value ->
                    val sanitized = value.singleLine()
                    if (sanitized != value) {
                        draft.recurrenceState.setTextAndPlaceCursorAtEnd(sanitized)
                    }
                }
        }
    }

    private fun validateDrafts(): String? {
        uiMainTasks.forEach { task ->
            if (task.descriptionState.text.toString().trim().isBlank()) return@forEach

            val recurrence = task.recurrenceState.text.toString()
            val dueDate = task.dueDateState.text.toString().trim()

            if (recurrence.isNotBlank() && recurrence.any { it == '\n' || it == '\r' }) {
                return "Recurrence must be a single-line RRULE."
            }

            if (dueDate.isNotBlank() && !dueDatePattern.matches(dueDate)) {
                return "Due date must use YYYY-MM-DD."
            }

            if (recurrence.isNotBlank() && dueDate.isBlank()) {
                return "A due date is required when recurrence is active."
            }
        }

        return null
    }

    private fun scheduleNotifications(taskListName: String, domainTasks: List<MainTask>) {
        viewModelScope.launch(Dispatchers.Default) {
            for (task in domainTasks) {
                scheduleTaskNotification(notificationScheduler, task, taskListName)
            }
        }
    }

    private fun TaskList.toSyncSnapshot(): SyncSnapshot = SyncSnapshot(
        title = name,
        mainTasks = tasks.map { serverTask ->
            // Merge local isNotificationEnabled into the snapshot so that diff-checks
            // against buildSyncSnapshot() don't trigger re-syncs for this on-device-only field.
            val localTask = this@EditTaskListViewModel.uiMainTasks.firstOrNull { it.id == serverTask.id }
            if (localTask != null) {
                serverTask.copy(isNotificationEnabled = localTask.isNotificationEnabled)
            } else {
                serverTask
            }
        }.sortedByDueDate(),
        isAutoDelete = isAutoDelete
    )

    private fun List<MainTask>.sortedByDueDate(): List<MainTask> =
        sortedByDueDate { it.dueDate }

    // Shared by domain payloads/snapshots and the mutable UI list; only due-date access differs.
    private fun <T> Iterable<T>.sortedByDueDate(dueDate: (T) -> String): List<T> =
        withIndex()
            .sortedWith(
                compareBy<IndexedValue<T>> { dueDate(it.value).dueDateSortKey() == null }
                    .thenBy { dueDate(it.value).dueDateSortKey() }
                    .thenBy { it.index }
            )
            .map { it.value }

    private fun String.dueDateSortKey(): String? =
        trim().takeIf { it.isNotEmpty() }
}
