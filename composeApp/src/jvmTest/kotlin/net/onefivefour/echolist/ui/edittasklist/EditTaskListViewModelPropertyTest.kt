package net.onefivefour.echolist.ui.edittasklist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.data.models.CreateTaskListParams
import net.onefivefour.echolist.data.models.UpdateTaskListParams
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.SubTask
import net.onefivefour.echolist.domain.model.TaskList
import net.onefivefour.echolist.domain.model.TaskListEntry
import net.onefivefour.echolist.domain.repository.TaskListRepository

@OptIn(ExperimentalCoroutinesApi::class, io.kotest.common.ExperimentalKotest::class)
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
        private val taskLists = mutableMapOf<String, TaskList>()

        fun addTaskList(taskList: TaskList) {
            taskLists[taskList.id] = taskList
        }

        override suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList> {
            createTaskListCalls.add(params)
            val created = TaskList(
                id = "generated-${params.name}",
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
            return taskLists[taskListId]?.let { Result.success(it) }
                ?: Result.failure(NoSuchElementException("TaskList not found: $taskListId"))
        }

        override suspend fun listTaskLists(parentDir: String): Result<List<TaskListEntry>> =
            Result.success(emptyList())

        override suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList> {
            updateTaskListCalls.add(params)
            val existing = taskLists[params.id]
                ?: return Result.failure(NoSuchElementException("TaskList not found: ${params.id}"))
            val updated = existing.copy(name = params.title, tasks = params.tasks, updatedAt = existing.updatedAt + 1)
                .copy(isAutoDelete = params.isAutoDelete)
            taskLists[updated.id] = updated
            return Result.success(updated)
        }

        override suspend fun deleteTaskList(taskListId: String): Result<Unit> {
            deleteTaskListCalls.add(taskListId)
            taskLists.remove(taskListId)
            return Result.success(Unit)
        }
    }

    test("Property 1: create mode saves a new task list with nested tasks") {
        runTest(testDispatcher) {
            val fakeRepo = FakeTaskListRepository()
            val parentPath = "home/projects"
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create(parentPath),
                taskListRepository = fakeRepo
            )

            vm.uiState.value.titleState.edit {
                replace(0, length, "Sprint plan")
            }

            vm.onAddMainTask()
            val task = vm.uiState.value.mainTasks[0]
            task.descriptionState.edit { replace(0, length, "Plan release") }
            task.isDone = true
            task.dueDateState.edit { replace(0, length, "2026-04-01") }
            vm.onAddSubTask(0)
            vm.uiState.value.mainTasks[0].subTasks[0].descriptionState.edit { replace(0, length, "Write checklist") }

            vm.onSaveClick()
            testScheduler.advanceUntilIdle()

            fakeRepo.createTaskListCalls shouldHaveSize 1
            val params = fakeRepo.createTaskListCalls[0]
            params.name shouldBe "Sprint plan"
            params.path shouldBe parentPath
            params.tasks shouldHaveSize 1
            params.tasks[0].description shouldBe "Plan release"
            params.tasks[0].isDone shouldBe true
            params.tasks[0].dueDate shouldBe "2026-04-01"
            params.tasks[0].subTasks shouldHaveSize 1
            params.tasks[0].subTasks[0].description shouldBe "Write checklist"
            params.isAutoDelete shouldBe false
        }
    }

    test("Property 2: edit mode loads data and updates the existing task list") {
        runTest(testDispatcher) {
            val fakeRepo = FakeTaskListRepository()
            val taskList = TaskList(
                id = "task-list-1",
                filePath = "home/projects/task-list-1",
                name = "Trip prep",
                tasks = listOf(
                    MainTask(
                        description = "Book hotel",
                        isDone = false,
                        dueDate = "",
                        recurrence = "FREQ=WEEKLY;BYDAY=MO",
                        subTasks = listOf(SubTask(description = "Compare prices", isDone = true))
                    )
                ),
                updatedAt = 10L,
                isAutoDelete = true
            )
            fakeRepo.addTaskList(taskList)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(taskList.id),
                taskListRepository = fakeRepo
            )

            testScheduler.advanceUntilIdle()

            vm.uiState.value.titleState.text.toString() shouldBe taskList.name
            vm.uiState.value.mainTasks shouldHaveSize 1
            vm.uiState.value.mainTasks[0].descriptionState.text.toString() shouldBe "Book hotel"
            vm.uiState.value.mainTasks[0].recurrenceState.text.toString() shouldBe "FREQ=WEEKLY;BYDAY=MO"
            vm.uiState.value.mainTasks[0].subTasks shouldHaveSize 1
            vm.uiState.value.mainTasks[0].subTasks[0].descriptionState.text.toString() shouldBe "Compare prices"

            vm.uiState.value.titleState.edit {
                replace(0, length, "Trip prep v2")
            }
            vm.uiState.value.isAutoDelete shouldBe true
            vm.onToggleAutoDelete(false)
            vm.uiState.value.mainTasks[0].descriptionState.edit { replace(0, length, "Book hotel and flight") }
            vm.onAddSubTask(0)
            vm.uiState.value.mainTasks[0].subTasks[1].descriptionState.edit { replace(0, length, "Book flight") }

            vm.onSaveClick()
            testScheduler.advanceUntilIdle()

            fakeRepo.getTaskListCalls shouldBe listOf(taskList.id)
            fakeRepo.updateTaskListCalls shouldHaveSize 1
            val update = fakeRepo.updateTaskListCalls[0]
            update.id shouldBe taskList.id
            update.title shouldBe "Trip prep v2"
            update.tasks shouldHaveSize 1
            update.tasks[0].description shouldBe "Book hotel and flight"
            update.tasks[0].subTasks shouldHaveSize 2
            update.isAutoDelete shouldBe false
            vm.uiState.value.isSaving shouldBe false
        }
    }

    test("main task stays and updates done state when isAutoDelete is false") {
        runTest(testDispatcher) {
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create("home"),
                taskListRepository = FakeTaskListRepository()
            )

            vm.onAddMainTask()
            vm.uiState.value.mainTasks[0].descriptionState.edit { replace(0, length, "Keep me") }

            vm.onMainTaskCheckedChange(0, true)

            vm.uiState.value.mainTasks shouldHaveSize 1
            vm.uiState.value.mainTasks[0].isDone shouldBe true
        }
    }

    test("subtask stays and updates done state when isAutoDelete is false") {
        runTest(testDispatcher) {
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create("home"),
                taskListRepository = FakeTaskListRepository()
            )

            vm.onAddMainTask()
            vm.uiState.value.mainTasks[0].descriptionState.edit { replace(0, length, "Parent") }
            vm.onAddSubTask(0)
            vm.uiState.value.mainTasks[0].subTasks[0].descriptionState.edit { replace(0, length, "Keep me") }

            vm.onSubTaskCheckedChange(0, 0, true)

            vm.uiState.value.mainTasks[0].subTasks shouldHaveSize 1
            vm.uiState.value.mainTasks[0].subTasks[0].isDone shouldBe true
        }
    }

    test("main task stays and updates done state when isAutoDelete is true") {
        runTest(testDispatcher) {
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create("home"),
                taskListRepository = FakeTaskListRepository()
            )

            vm.onAddMainTask()
            vm.uiState.value.mainTasks[0].descriptionState.edit { replace(0, length, "Delete me") }
            vm.onAddSubTask(0)
            vm.uiState.value.mainTasks[0].subTasks[0].descriptionState.edit { replace(0, length, "Child") }
            vm.onToggleAutoDelete(true)

            vm.onMainTaskCheckedChange(0, true)

            vm.uiState.value.mainTasks shouldHaveSize 1
            vm.uiState.value.mainTasks[0].isDone shouldBe true
        }
    }

    test("subtask stays and updates done state when isAutoDelete is true") {
        runTest(testDispatcher) {
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create("home"),
                taskListRepository = FakeTaskListRepository()
            )

            vm.onAddMainTask()
            vm.uiState.value.mainTasks[0].descriptionState.edit { replace(0, length, "Parent") }
            vm.onAddSubTask(0)
            vm.uiState.value.mainTasks[0].subTasks[0].descriptionState.edit { replace(0, length, "Delete me") }
            vm.onToggleAutoDelete(true)

            vm.onSubTaskCheckedChange(0, 0, true)

            vm.uiState.value.mainTasks[0].subTasks shouldHaveSize 1
            vm.uiState.value.mainTasks[0].subTasks[0].isDone shouldBe true
        }
    }

    test("Property 3: delete emits navigate-back for edit mode") {
        runTest(testDispatcher) {
            val fakeRepo = FakeTaskListRepository()
            val taskList = TaskList(
                id = "task-list-delete",
                filePath = "home/projects/task-list-delete",
                name = "Delete me",
                tasks = emptyList(),
                updatedAt = 1L,
                isAutoDelete = false
            )
            fakeRepo.addTaskList(taskList)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(taskList.id),
                taskListRepository = fakeRepo
            )

            testScheduler.advanceUntilIdle()

            val navigateBackDeferred = async { vm.navigateBack.first() }

            vm.onDeleteClick()
            testScheduler.advanceUntilIdle()

            navigateBackDeferred.await() shouldBe Unit
            fakeRepo.deleteTaskListCalls shouldBe listOf(taskList.id)
        }
    }

    test("Property 4: recurrence clears due date before save") {
        checkAll(
            PropTestConfig(iterations = 50),
            Arb.string(0..20).filter { it.isNotBlank() }
        ) { recurrence ->
            runTest(testDispatcher) {
                val fakeRepo = FakeTaskListRepository()
                val vm = EditTaskListViewModel(
                    mode = EditTaskListMode.Create("home"),
                    taskListRepository = fakeRepo
                )

                vm.uiState.value.titleState.edit {
                    replace(0, length, "Recurring")
                }
                vm.onAddMainTask()
                val task = vm.uiState.value.mainTasks[0]
                task.descriptionState.edit { replace(0, length, "Repeat setup") }
                task.dueDateState.edit { replace(0, length, "2026-04-01") }
                task.recurrenceState.edit { replace(0, length, recurrence.singleLine()) }
                testScheduler.advanceUntilIdle()

                vm.onSaveClick()
                testScheduler.advanceUntilIdle()

                fakeRepo.createTaskListCalls shouldHaveSize 1
                fakeRepo.createTaskListCalls[0].tasks[0].dueDate shouldBe ""
                fakeRepo.createTaskListCalls[0].tasks[0].recurrence shouldBe recurrence.singleLine()
            }
        }
    }
})
