package net.onefivefour.echolist.data.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.data.models.CreateTaskListParams
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.SubTask
import net.onefivefour.echolist.data.models.UpdateTaskListParams
import net.onefivefour.echolist.data.source.network.FakeTaskListRemoteDataSource
import tasks.v1.CreateTaskListResponse
import tasks.v1.DeleteTaskListResponse
import tasks.v1.GetTaskListResponse
import tasks.v1.ListTaskListsResponse
import tasks.v1.UpdateTaskListResponse

/**
 * Feature: proto-api-update
 * Property 21: TaskListRepository creates task lists correctly
 * Property 22: TaskListRepository gets task lists correctly
 * Property 23: TaskListRepository lists task lists correctly
 * Property 24: TaskListRepository updates task lists correctly
 * Property 25: TaskListRepository deletes task lists correctly
 *
 * Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5
 */
class TaskListRepositoryImplPropertyTest : FunSpec({

    // -- Generators --

    val arbProtoSubTask = arbitrary {
        tasks.v1.SubTask(
            description = Arb.string(0..100).bind(),
            done = Arb.boolean().bind()
        )
    }

    val arbProtoMainTask = arbitrary {
        tasks.v1.MainTask(
            description = Arb.string(0..100).bind(),
            done = Arb.boolean().bind(),
            due_date = Arb.string(0..50).bind(),
            recurrence = Arb.string(0..50).bind(),
            sub_tasks = Arb.list(arbProtoSubTask, 0..3).bind()
        )
    }

    val arbProtoTaskList = arbitrary {
        tasks.v1.TaskList(
            id = Arb.string(1..50).bind(),
            file_path = Arb.string(1..100).bind(),
            title = Arb.string(1..100).bind(),
            tasks = Arb.list(arbProtoMainTask, 0..5).bind(),
            updated_at = Arb.long(0..Long.MAX_VALUE).bind()
        )
    }

    val arbDomainSubTask = arbitrary {
        SubTask(
            description = Arb.string(0..100).bind(),
            done = Arb.boolean().bind()
        )
    }

    val arbDomainMainTask = arbitrary {
        MainTask(
            description = Arb.string(0..100).bind(),
            done = Arb.boolean().bind(),
            dueDate = Arb.string(0..50).bind(),
            recurrence = Arb.string(0..50).bind(),
            subTasks = Arb.list(arbDomainSubTask, 0..3).bind()
        )
    }

    val arbCreateTaskListParams = arbitrary {
        CreateTaskListParams(
            name = Arb.string(1..100).bind(),
            path = Arb.string(1..100).bind(),
            tasks = Arb.list(arbDomainMainTask, 0..3).bind()
        )
    }

    val arbUpdateTaskListParams = arbitrary {
        UpdateTaskListParams(
            id = Arb.string(1..100).bind(),
            title = Arb.string(1..100).bind(),
            tasks = Arb.list(arbDomainMainTask, 0..3).bind()
        )
    }

    // ---------------------------------------------------------------
    // Property 21: TaskListRepository creates task lists correctly
    // Validates: Requirements 10.1
    // ---------------------------------------------------------------

    test(
        "Feature: proto-api-update, Property 21: " +
            "TaskListRepository creates task lists correctly - returns mapped TaskList"
    ) {
        checkAll(
            PropTestConfig(iterations = 100),
            arbCreateTaskListParams,
            arbProtoTaskList
        ) { params, protoTaskList ->
            val fake = FakeTaskListRemoteDataSource()
            fake.createTaskListResult = Result.success(CreateTaskListResponse(task_list = protoTaskList))
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.createTaskList(params)

            result.isSuccess shouldBe true
            val taskList = result.getOrThrow()
            taskList.id shouldBe protoTaskList.id
            taskList.filePath shouldBe protoTaskList.file_path
            taskList.name shouldBe protoTaskList.title
            taskList.tasks.size shouldBe protoTaskList.tasks.size
            taskList.updatedAt shouldBe protoTaskList.updated_at
        }
    }

    test(
        "Feature: proto-api-update, Property 21: TaskListRepository creates task lists correctly - maps request fields"
    ) {
        checkAll(
            PropTestConfig(iterations = 100),
            arbCreateTaskListParams,
            arbProtoTaskList
        ) { params, protoTaskList ->
            val fake = FakeTaskListRemoteDataSource()
            fake.createTaskListResult = Result.success(CreateTaskListResponse(task_list = protoTaskList))
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            repo.createTaskList(params)

            fake.lastCreateRequest?.title shouldBe params.name
            fake.lastCreateRequest?.parent_dir shouldBe params.path
            fake.lastCreateRequest?.tasks?.size shouldBe params.tasks.size
        }
    }

    // ---------------------------------------------------------------
    // Property 22: TaskListRepository gets task lists correctly
    // Validates: Requirements 10.2
    // ---------------------------------------------------------------

    test(
        "Feature: proto-api-update, Property 22: TaskListRepository gets task lists correctly - returns mapped TaskList"
    ) {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(1..100),
            arbProtoTaskList
        ) { taskListId, protoTaskList ->
            val fake = FakeTaskListRemoteDataSource()
            fake.getTaskListResult = Result.success(GetTaskListResponse(task_list = protoTaskList))
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.getTaskList(taskListId)

            result.isSuccess shouldBe true
            val taskList = result.getOrThrow()
            taskList.id shouldBe protoTaskList.id
            taskList.filePath shouldBe protoTaskList.file_path
            taskList.name shouldBe protoTaskList.title
            taskList.tasks.size shouldBe protoTaskList.tasks.size
            taskList.updatedAt shouldBe protoTaskList.updated_at
        }
    }

    test("Feature: proto-api-update, Property 22: TaskListRepository gets task lists correctly - maps request fields") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(1..100)
        ) { taskListId ->
            val fake = FakeTaskListRemoteDataSource()
            fake.getTaskListResult = Result.success(
                GetTaskListResponse(
                    task_list = tasks.v1.TaskList(
                        id = taskListId,
                        file_path = "/some/path.tl",
                        title = "t",
                        tasks = emptyList(),
                        updated_at = 0L
                    )
                )
            )
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            repo.getTaskList(taskListId)

            fake.lastGetRequest?.id shouldBe taskListId
        }
    }

    // ---------------------------------------------------------------
    // Property 23: TaskListRepository lists task lists correctly
    // Validates: Requirements 10.3
    // ---------------------------------------------------------------

    test(
        "Feature: proto-api-update, Property 23: TaskListRepository lists task lists correctly - returns mapped list"
    ) {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..100),
            Arb.list(arbProtoTaskList, 0..10)
        ) { path, protoTaskLists ->
            val fake = FakeTaskListRemoteDataSource()
            fake.listTaskListsResult = Result.success(
                ListTaskListsResponse(task_lists = protoTaskLists)
            )
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.listTaskLists(path)

            result.isSuccess shouldBe true
            val taskLists = result.getOrThrow()
            taskLists.size shouldBe protoTaskLists.size
            taskLists.forEachIndexed { i, entry ->
                entry.id shouldBe protoTaskLists[i].id
                entry.filePath shouldBe protoTaskLists[i].file_path
                entry.name shouldBe protoTaskLists[i].title
                entry.updatedAt shouldBe protoTaskLists[i].updated_at
            }
        }
    }

    test(
        "Feature: proto-api-update, Property 23: TaskListRepository lists task lists correctly - maps request fields"
    ) {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..100)
        ) { path ->
            val fake = FakeTaskListRemoteDataSource()
            fake.listTaskListsResult = Result.success(ListTaskListsResponse())
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            repo.listTaskLists(path)

            fake.lastListRequest?.parent_dir shouldBe path
        }
    }

    // ---------------------------------------------------------------
    // Property 24: TaskListRepository updates task lists correctly
    // Validates: Requirements 10.4
    // ---------------------------------------------------------------

    test(
        "Feature: proto-api-update, Property 24: " +
            "TaskListRepository updates task lists correctly - returns mapped TaskList"
    ) {
        checkAll(
            PropTestConfig(iterations = 100),
            arbUpdateTaskListParams,
            arbProtoTaskList
        ) { params, protoTaskList ->
            val fake = FakeTaskListRemoteDataSource()
            fake.updateTaskListResult = Result.success(UpdateTaskListResponse(task_list = protoTaskList))
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.updateTaskList(params)

            result.isSuccess shouldBe true
            val taskList = result.getOrThrow()
            taskList.id shouldBe protoTaskList.id
            taskList.filePath shouldBe protoTaskList.file_path
            taskList.name shouldBe protoTaskList.title
            taskList.tasks.size shouldBe protoTaskList.tasks.size
            taskList.updatedAt shouldBe protoTaskList.updated_at
        }
    }

    test(
        "Feature: proto-api-update, Property 24: TaskListRepository updates task lists correctly - maps request fields"
    ) {
        checkAll(
            PropTestConfig(iterations = 100),
            arbUpdateTaskListParams,
            arbProtoTaskList
        ) { params, protoTaskList ->
            val fake = FakeTaskListRemoteDataSource()
            fake.updateTaskListResult = Result.success(UpdateTaskListResponse(task_list = protoTaskList))
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            repo.updateTaskList(params)

            fake.lastUpdateRequest?.id shouldBe params.id
            fake.lastUpdateRequest?.title shouldBe params.title
            fake.lastUpdateRequest?.tasks?.size shouldBe params.tasks.size
        }
    }

    // ---------------------------------------------------------------
    // Property 25: TaskListRepository deletes task lists correctly
    // Validates: Requirements 10.5
    // ---------------------------------------------------------------

    test("Feature: proto-api-update, Property 25: TaskListRepository deletes task lists correctly - returns success") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(1..100)
        ) { taskListId ->
            val fake = FakeTaskListRemoteDataSource()
            fake.deleteTaskListResult = Result.success(DeleteTaskListResponse())
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.deleteTaskList(taskListId)

            result.isSuccess shouldBe true
            result.getOrThrow() shouldBe Unit
        }
    }

    test(
        "Feature: proto-api-update, Property 25: TaskListRepository deletes task lists correctly - maps request fields"
    ) {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(1..100)
        ) { taskListId ->
            val fake = FakeTaskListRemoteDataSource()
            fake.deleteTaskListResult = Result.success(DeleteTaskListResponse())
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            repo.deleteTaskList(taskListId)

            fake.lastDeleteRequest?.id shouldBe taskListId
        }
    }
})