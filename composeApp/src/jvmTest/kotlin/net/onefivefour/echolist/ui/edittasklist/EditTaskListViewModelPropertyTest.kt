package net.onefivefour.echolist.ui.edittasklist

import io.kotest.core.spec.style.FunSpec
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
import net.onefivefour.echolist.data.dto.ListTaskListsResult
import net.onefivefour.echolist.domain.model.TaskList
import net.onefivefour.echolist.data.models.UpdateTaskListParams
import net.onefivefour.echolist.domain.repository.TaskListRepository

// Feature: note-tasklist-editors, Property 1: Save guard — repository called if and only if trimmed text is non-blank

@OptIn(ExperimentalCoroutinesApi::class, io.kotest.common.ExperimentalKotest::class)
class EditTaskListViewModelPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    // -- Fake --

    class FakeTaskListRepository : TaskListRepository {
        val createTaskListCalls = mutableListOf<CreateTaskListParams>()

        override suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList> {
            createTaskListCalls.add(params)
            return Result.success(
                TaskList(
                    id = "generated-id",
                    filePath = "${params.path}/${params.name}",
                    name = params.name,
                    tasks = params.tasks,
                    updatedAt = 0L
                )
            )
        }

        override suspend fun getTaskList(taskListId: String): Result<TaskList> =
            Result.failure(UnsupportedOperationException())

        override suspend fun listTaskLists(parentDir: String): Result<ListTaskListsResult> =
            Result.success(ListTaskListsResult(taskLists = emptyList(), entries = emptyList()))

        override suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList> =
            Result.failure(UnsupportedOperationException())

        override suspend fun deleteTaskList(taskListId: String): Result<Unit> =
            Result.failure(UnsupportedOperationException())
    }

    // -- Property 1: Save guard --

    test("Property 1: Save guard — repository called if and only if trimmed text is non-blank") {
        // Validates: Requirements 6.3, 6.7
        checkAll(PropTestConfig(iterations = 100), Arb.string(0..50)) { generatedString ->
            runTest(testDispatcher) {
                val fakeRepo = FakeTaskListRepository()
                val parentPath = "test/path"
                val vm = EditTaskListViewModel(
                    parentPath = parentPath,
                    taskListRepository = fakeRepo
                )

                // Set the title text
                vm.uiState.value.titleState.edit {
                    replace(0, length, generatedString)
                }

                vm.onSaveClick()
                testScheduler.advanceUntilIdle()

                val trimmed = generatedString.trim()
                if (trimmed.isBlank()) {
                    fakeRepo.createTaskListCalls.size shouldBe 0
                } else {
                    fakeRepo.createTaskListCalls.size shouldBe 1
                    fakeRepo.createTaskListCalls[0].name shouldBe trimmed
                    fakeRepo.createTaskListCalls[0].path shouldBe parentPath
                    fakeRepo.createTaskListCalls[0].tasks shouldBe emptyList()
                }
            }
        }
    }

    // Feature: note-tasklist-editors, Property 2: Successful save emits navigate-back event

    test("Property 2: Successful save emits navigate-back event") {
        // Validates: Requirements 6.5
        checkAll(PropTestConfig(iterations = 100), Arb.string(0..50).filter { it.isNotBlank() }) { generatedTitle ->
            runTest(testDispatcher) {
                val fakeRepo = FakeTaskListRepository()
                val parentPath = "test/path"
                val vm = EditTaskListViewModel(
                    parentPath = parentPath,
                    taskListRepository = fakeRepo
                )

                // Start collecting navigateBack before triggering save
                val navigateBackDeferred = async {
                    vm.navigateBack.first()
                }

                // Set the title text
                vm.uiState.value.titleState.edit {
                    replace(0, length, generatedTitle)
                }

                vm.onSaveClick()
                testScheduler.advanceUntilIdle()

                // The deferred should complete with Unit, proving exactly one event was emitted
                navigateBackDeferred.await() shouldBe Unit
            }
        }
    }

    // Feature: note-tasklist-editors, Property 3: Failed save sets error and clears loading

    test("Property 3: Failed save sets error and clears loading") {
        // Validates: Requirements 6.6
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..50).filter { it.isNotBlank() },
            Arb.string(1..100)
        ) { generatedTitle, errorMessage ->
            runTest(testDispatcher) {
                val failingRepo = object : TaskListRepository {
                    override suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList> {
                        return Result.failure(RuntimeException(errorMessage))
                    }

                    override suspend fun getTaskList(taskListId: String): Result<TaskList> =
                        Result.failure(UnsupportedOperationException())

                    override suspend fun listTaskLists(parentDir: String): Result<ListTaskListsResult> =
                        Result.success(ListTaskListsResult(taskLists = emptyList(), entries = emptyList()))

                    override suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList> =
                        Result.failure(UnsupportedOperationException())

                    override suspend fun deleteTaskList(taskListId: String): Result<Unit> =
                        Result.failure(UnsupportedOperationException())
                }

                val parentPath = "test/path"
                val vm = EditTaskListViewModel(
                    parentPath = parentPath,
                    taskListRepository = failingRepo
                )

                // Set the title text
                vm.uiState.value.titleState.edit {
                    replace(0, length, generatedTitle)
                }

                vm.onSaveClick()
                testScheduler.advanceUntilIdle()

                vm.uiState.value.isLoading shouldBe false
                vm.uiState.value.error shouldBe errorMessage
            }
        }
    }
})
