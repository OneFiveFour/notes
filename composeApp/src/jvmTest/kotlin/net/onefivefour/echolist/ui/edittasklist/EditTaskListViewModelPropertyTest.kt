package net.onefivefour.echolist.ui.edittasklist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.data.dto.CreateTaskListParams
import net.onefivefour.echolist.data.models.UpdateTaskListParams
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.SubTask
import net.onefivefour.echolist.domain.model.TaskList
import net.onefivefour.echolist.domain.model.TaskListEntry
import net.onefivefour.echolist.domain.repository.TaskListRepository

@OptIn(ExperimentalCoroutinesApi::class)
class EditTaskListViewModelPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    class FakeTaskListRepository : TaskListRepository {
        val createTaskListCalls = mutableListOf<CreateTaskListParams>()
        val updateTaskListCalls = mutableListOf<UpdateTaskListParams>()
        val deleteTaskListCalls = mutableListOf<String>()
        val getTaskListCalls = mutableListOf<String>()
        val taskLists = mutableMapOf<String, TaskList>()
        var nextCreatedId = 1
        var blockNextUpdate: CompletableDeferred<Unit>? = null

        fun addTaskList(taskList: TaskList) {
            taskLists[taskList.id] = taskList
        }

        override suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList> {
            createTaskListCalls.add(params)
            val created = TaskList(
                id = "created-${nextCreatedId++}",
                filePath = "${params.path}/${params.name}",
                name = params.name,
                tasks = params.tasks,
                updatedAt = 0L,
                isAutoDelete = params.isAutoDelete
            )
            taskLists[created.id] = created
            return Result.success(created)
        }

        override suspend fun getTaskList(taskListId: String): Result<TaskList> {
            getTaskListCalls.add(taskListId)
            return taskLists[taskListId]?.let(Result.Companion::success)
                ?: Result.failure(NoSuchElementException("TaskList not found: $taskListId"))
        }

        override suspend fun listTaskLists(parentDir: String): Result<List<TaskListEntry>> =
            Result.success(emptyList())

        override suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList> {
            updateTaskListCalls.add(params)
            blockNextUpdate?.let { deferred ->
                deferred.await()
                if (blockNextUpdate === deferred) {
                    blockNextUpdate = null
                }
            }

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
            deleteTaskListCalls.add(taskListId)
            taskLists.remove(taskListId)
            return Result.success(Unit)
        }
    }

    fun taskList(
        id: String,
        name: String = "Existing list",
        tasks: List<MainTask> = listOf(
            MainTask(
                description = "Existing task",
                isDone = false,
                dueDate = "",
                recurrence = "",
                subTasks = emptyList()
            )
        ),
        isAutoDelete: Boolean = false
    ): TaskList = TaskList(
        id = id,
        filePath = "home/$id",
        name = name,
        tasks = tasks,
        updatedAt = 1L,
        isAutoDelete = isAutoDelete
    )

    test("create mode auto-creates on the first valid blur and becomes persisted") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create("home"),
                taskListRepository = repo
            )

            vm.uiState.value.titleState.edit { replace(0, length, "Sprint plan") }
            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            repo.createTaskListCalls shouldHaveSize 0

            vm.onAddMainTask()
            vm.uiState.value.mainTasks[0].descriptionState.edit {
                replace(0, length, "Plan release")
            }
            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            repo.createTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls shouldHaveSize 0
            repo.createTaskListCalls[0].name shouldBe "Sprint plan"
            repo.createTaskListCalls[0].tasks.single().description shouldBe "Plan release"
            vm.uiState.value.isPersisted shouldBe true
        }
    }

    test("later blur after auto-create updates instead of creating again") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create("home"),
                taskListRepository = repo
            )

            vm.uiState.value.titleState.edit { replace(0, length, "Sprint plan") }
            vm.onAddMainTask()
            vm.uiState.value.mainTasks[0].descriptionState.edit {
                replace(0, length, "Plan release")
            }
            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            vm.uiState.value.mainTasks[0].descriptionState.edit {
                replace(0, length, "Plan release v2")
            }
            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            repo.createTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].title shouldBe "Sprint plan"
            repo.updateTaskListCalls[0].tasks.single().description shouldBe "Plan release v2"
        }
    }

    test("title blur updates an existing task list") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(id = "task-list-1", name = "Trip prep")
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo
            )

            testScheduler.advanceUntilIdle()

            vm.uiState.value.titleState.edit { replace(0, length, "Trip prep v2") }
            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].title shouldBe "Trip prep v2"
        }
    }

    test("main task checkbox changes sync immediately") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(id = "task-list-check")
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo
            )

            testScheduler.advanceUntilIdle()

            vm.onMainTaskCheckedChange(0, true)
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].tasks.single().isDone shouldBe true
        }
    }

    test("manual delete syncs immediately") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(
                id = "task-list-delete-row",
                tasks = listOf(
                    MainTask("Task 1", false, "", "", emptyList()),
                    MainTask("Task 2", false, "", "", emptyList())
                )
            )
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo
            )

            testScheduler.advanceUntilIdle()

            vm.onRemoveMainTask(1)
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].tasks.map { it.description } shouldBe listOf("Task 1")
        }
    }

    test("auto-delete removes completed main tasks locally and syncs the reduced list") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(
                id = "task-list-auto-main",
                tasks = listOf(
                    MainTask("Task 1", false, "", "", emptyList()),
                    MainTask("Task 2", false, "", "", emptyList())
                ),
                isAutoDelete = true
            )
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo
            )

            testScheduler.advanceUntilIdle()

            vm.onMainTaskCheckedChange(0, true)
            testScheduler.advanceUntilIdle()

            vm.uiState.value.mainTasks.map { it.descriptionState.text.toString() } shouldBe listOf("Task 2")
            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].tasks.map { it.description } shouldBe listOf("Task 2")
        }
    }

    test("auto-delete removes completed subtasks locally and syncs the reduced list") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(
                id = "task-list-auto-sub",
                tasks = listOf(
                    MainTask(
                        description = "Parent",
                        isDone = false,
                        dueDate = "",
                        recurrence = "",
                        subTasks = listOf(
                            SubTask("Sub 1", false),
                            SubTask("Sub 2", false)
                        )
                    )
                ),
                isAutoDelete = true
            )
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo
            )

            testScheduler.advanceUntilIdle()

            vm.onSubTaskCheckedChange(0, 0, true)
            testScheduler.advanceUntilIdle()

            vm.uiState.value.mainTasks[0].subTasks.map { it.descriptionState.text.toString() } shouldBe listOf("Sub 2")
            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].tasks[0].subTasks.map { it.description } shouldBe listOf("Sub 2")
        }
    }

    test("auto-delete toggle syncs immediately") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(id = "task-list-toggle", isAutoDelete = false)
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo
            )

            testScheduler.advanceUntilIdle()

            vm.onToggleAutoDelete(true)
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].isAutoDelete shouldBe true
        }
    }

    test("invalid due date blocks sync and preserves the validation error") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create("home"),
                taskListRepository = repo
            )

            vm.uiState.value.titleState.edit { replace(0, length, "Sprint plan") }
            vm.onAddMainTask()
            vm.uiState.value.mainTasks[0].descriptionState.edit {
                replace(0, length, "Plan release")
            }
            vm.uiState.value.mainTasks[0].dueDateState.edit {
                replace(0, length, "2026/04/01")
            }

            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            repo.createTaskListCalls shouldHaveSize 0
            repo.updateTaskListCalls shouldHaveSize 0
            vm.uiState.value.error shouldBe "Due date must use YYYY-MM-DD."
        }
    }

    test("repeated blur without changes does not send duplicate requests") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(id = "task-list-dedupe", name = "Trip prep")
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo
            )

            testScheduler.advanceUntilIdle()

            vm.uiState.value.titleState.edit { replace(0, length, "Trip prep v2") }
            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 1
        }
    }

    test("queued edits run after an in-flight sync finishes") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(id = "task-list-queue", name = "Trip prep")
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo
            )

            testScheduler.advanceUntilIdle()

            val gate = CompletableDeferred<Unit>()
            repo.blockNextUpdate = gate

            vm.uiState.value.titleState.edit { replace(0, length, "Trip prep v2") }
            vm.onFieldFocusLost()
            testScheduler.runCurrent()

            vm.uiState.value.titleState.edit { replace(0, length, "Trip prep v3") }
            vm.onFieldFocusLost()

            gate.complete(Unit)
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 2
            repo.updateTaskListCalls.last().title shouldBe "Trip prep v3"
        }
    }

    test("delete emits navigate-back for a persisted task list") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(id = "task-list-delete")
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo
            )

            testScheduler.advanceUntilIdle()

            val navigateBackDeferred = async { vm.navigateBack.first() }

            vm.onDeleteClick()
            testScheduler.advanceUntilIdle()

            navigateBackDeferred.await() shouldBe Unit
            repo.deleteTaskListCalls shouldBe listOf(existing.id)
        }
    }
})
