package net.onefivefour.echolist.ui.maintasksettings

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.data.dto.CreateTaskListParams
import net.onefivefour.echolist.data.models.UpdateTaskListParams
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.TaskList
import net.onefivefour.echolist.domain.model.TaskListEntry
import net.onefivefour.echolist.domain.repository.TaskListRepository
import net.onefivefour.echolist.ui.edittasklist.EditTaskListMode
import net.onefivefour.echolist.ui.edittasklist.EditTaskListViewModel

/**
 * Regression test for the due-date round-trip bug:
 * When a due date is set via MainTaskSettings, persisted through EditTaskList sync,
 * and then MainTaskSettings is opened again for the same task, the calendar should
 * show the previously selected date (initialDateMillis should be non-null).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainTaskSettingsDueDateRoundTripTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    /**
     * A fake repository that stores task lists in memory.
     * getMainTask looks up the task from stored task lists (simulating what the real
     * network-backed repository would return after a successful updateTaskList).
     */
    class FakeTaskListRepository : TaskListRepository {
        val taskLists = mutableMapOf<String, TaskList>()

        fun addTaskList(taskList: TaskList) {
            taskLists[taskList.id] = taskList
        }

        override suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList> {
            val created = TaskList(
                id = "created-1",
                parentDir = params.parentDir,
                name = params.name,
                tasks = params.tasks,
                updatedAt = 1L,
                isAutoDelete = params.isAutoDelete
            )
            taskLists[created.id] = created
            return Result.success(created)
        }

        override suspend fun getTaskList(taskListId: String): Result<TaskList> {
            return taskLists[taskListId]?.let(Result.Companion::success)
                ?: Result.failure(NoSuchElementException("TaskList not found: $taskListId"))
        }

        override suspend fun getMainTask(mainTaskId: String): Result<MainTask> {
            val task = taskLists.values
                .flatMap { it.tasks }
                .firstOrNull { it.id == mainTaskId }
            return task?.let(Result.Companion::success)
                ?: Result.failure(NoSuchElementException("MainTask not found: $mainTaskId"))
        }

        override suspend fun listTaskLists(parentDir: String): Result<List<TaskListEntry>> =
            Result.success(emptyList())

        override suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList> {
            val existing = taskLists[params.id]
                ?: return Result.failure(NoSuchElementException("TaskList not found: ${params.id}"))

            val updated = existing.copy(
                name = params.title,
                tasks = params.tasks,
                updatedAt = existing.updatedAt + 1,
                isAutoDelete = params.isAutoDelete
            )
            taskLists[updated.id] = updated
            return Result.success(updated)
        }

        override suspend fun deleteTaskList(taskListId: String): Result<Unit> {
            taskLists.remove(taskListId)
            return Result.success(Unit)
        }
    }

    test("Round trip: selecting a due date persists it so re-opening settings shows it in the calendar") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val resultBus = MainTaskSettingsResultBus()

            // Set up existing task list with a task that has no due date
            val initialTask = MainTask(
                id = "task-1",
                description = "Buy groceries",
                isDone = false,
                dueDate = "",
                recurrence = "",
                subTasks = emptyList()
            )
            val taskList = TaskList(
                id = "tl-1",
                parentDir = "home",
                name = "Shopping",
                tasks = listOf(initialTask),
                updatedAt = 1L,
                isAutoDelete = false
            )
            repo.addTaskList(taskList)

            // --- Step 1: EditTaskListViewModel loads the task list ---
            val editVm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit("tl-1"),
                taskListRepository = repo,
                settingsResultBus = resultBus
            )
            testScheduler.advanceUntilIdle()

            // --- Step 2: First navigation to MainTaskSettings ---
            val settingsVm1 = MainTaskSettingsViewModel(
                mainTaskId = "task-1",
                taskListRepository = repo,
                resultBus = resultBus
            )
            testScheduler.advanceUntilIdle()

            // Verify it loaded with no date
            val readyState1 = settingsVm1.uiState.value
            readyState1.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
            readyState1.initialDateMillis shouldBe null
            readyState1.selectedDueDate shouldBe ""

            // --- Step 3: User selects a date (2026-07-15) ---
            val selectedMillis = dueDateToUtcMillis("2026-07-15")!!
            settingsVm1.onDateSelected(selectedMillis)
            testScheduler.advanceUntilIdle()

            // The result bus should have fired, EditTaskListViewModel should have synced
            // Verify the repository now has the updated due date
            val updatedTask = repo.taskLists["tl-1"]!!.tasks.first { it.id == "task-1" }
            updatedTask.dueDate shouldBe "2026-07-15"

            // --- Step 4: User navigates back, then opens MainTaskSettings again ---
            // (This simulates creating a new ViewModel instance for the same task)
            val settingsVm2 = MainTaskSettingsViewModel(
                mainTaskId = "task-1",
                taskListRepository = repo,
                resultBus = resultBus
            )
            testScheduler.advanceUntilIdle()

            // --- Step 5: Assert the calendar should show the previously selected date ---
            val readyState2 = settingsVm2.uiState.value
            readyState2.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
            readyState2.initialDateMillis.shouldNotBeNull()
            readyState2.initialDateMillis shouldBe selectedMillis
            readyState2.selectedDueDate shouldBe "2026-07-15"
        }
    }

    test("Bug scenario: ViewModel reused across navigations does not update initialDateMillis") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val resultBus = MainTaskSettingsResultBus()

            // Set up existing task list with a task that has no due date
            val initialTask = MainTask(
                id = "task-1",
                description = "Buy groceries",
                isDone = false,
                dueDate = "",
                recurrence = "",
                subTasks = emptyList()
            )
            val taskList = TaskList(
                id = "tl-1",
                parentDir = "home",
                name = "Shopping",
                tasks = listOf(initialTask),
                updatedAt = 1L,
                isAutoDelete = false
            )
            repo.addTaskList(taskList)

            // EditTaskListViewModel handles sync
            val editVm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit("tl-1"),
                taskListRepository = repo,
                settingsResultBus = resultBus
            )
            testScheduler.advanceUntilIdle()

            // First navigation — ViewModel is created
            val settingsVm = MainTaskSettingsViewModel(
                mainTaskId = "task-1",
                taskListRepository = repo,
                resultBus = resultBus
            )
            testScheduler.advanceUntilIdle()

            // User selects a date
            val selectedMillis = dueDateToUtcMillis("2026-07-15")!!
            settingsVm.onDateSelected(selectedMillis)
            testScheduler.advanceUntilIdle()

            // Verify: after selecting a date, the uiState's initialDateMillis should reflect
            // the selected date so that if the composable recomposes (or re-enters composition
            // with the same ViewModel), the DatePicker would show the correct date.
            val stateAfterSelection = settingsVm.uiState.value
            stateAfterSelection.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()

            // THIS IS THE BUG: initialDateMillis is never updated after date selection
            // The composable uses rememberDatePickerState(initialSelectedDateMillis = uiState.initialDateMillis)
            // If the same ViewModel instance is reused (Koin cache), or if the composable
            // re-enters composition reading initialDateMillis, it will be null even though
            // a date was selected.
            stateAfterSelection.initialDateMillis shouldBe selectedMillis
        }
    }

    test("Round trip: selecting a due date with recurrence persists both correctly") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val resultBus = MainTaskSettingsResultBus()

            val initialTask = MainTask(
                id = "task-2",
                description = "Water plants",
                isDone = false,
                dueDate = "",
                recurrence = "",
                subTasks = emptyList()
            )
            val taskList = TaskList(
                id = "tl-2",
                parentDir = "home",
                name = "Garden",
                tasks = listOf(initialTask),
                updatedAt = 1L,
                isAutoDelete = false
            )
            repo.addTaskList(taskList)

            // EditTaskListViewModel to handle sync
            val editVm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit("tl-2"),
                taskListRepository = repo,
                settingsResultBus = resultBus
            )
            testScheduler.advanceUntilIdle()

            // First navigation to settings
            val settingsVm1 = MainTaskSettingsViewModel(
                mainTaskId = "task-2",
                taskListRepository = repo,
                resultBus = resultBus
            )
            testScheduler.advanceUntilIdle()

            // Select a date first
            val selectedMillis = dueDateToUtcMillis("2026-08-01")!!
            settingsVm1.onDateSelected(selectedMillis)
            testScheduler.advanceUntilIdle()

            // Then select weekly recurrence
            settingsVm1.onRecurrenceIntervalSelected(
                net.onefivefour.echolist.ui.recurrence.RecurrenceInterval.Weekly
            )
            settingsVm1.onRecurrenceDetailChanged(
                net.onefivefour.echolist.ui.recurrence.RecurrenceState.Weekly(everyNWeeks = 2)
            )
            testScheduler.advanceUntilIdle()

            // Verify persistence
            val updatedTask = repo.taskLists["tl-2"]!!.tasks.first { it.id == "task-2" }
            updatedTask.dueDate shouldBe "2026-08-01"
            updatedTask.recurrence shouldBe "FREQ=WEEKLY;INTERVAL=2"

            // Second navigation — new ViewModel
            val settingsVm2 = MainTaskSettingsViewModel(
                mainTaskId = "task-2",
                taskListRepository = repo,
                resultBus = resultBus
            )
            testScheduler.advanceUntilIdle()

            val readyState2 = settingsVm2.uiState.value
            readyState2.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
            readyState2.initialDateMillis shouldBe selectedMillis
            readyState2.selectedDueDate shouldBe "2026-08-01"
            readyState2.recurrenceState shouldBe
                net.onefivefour.echolist.ui.recurrence.RecurrenceState.Weekly(everyNWeeks = 2)
        }
    }
})
