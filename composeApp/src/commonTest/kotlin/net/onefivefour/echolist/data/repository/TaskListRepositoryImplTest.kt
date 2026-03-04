package net.onefivefour.echolist.data.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.data.models.CreateTaskListParams
import net.onefivefour.echolist.data.models.MainTask
import net.onefivefour.echolist.data.models.SubTask
import net.onefivefour.echolist.data.models.UpdateTaskListParams
import net.onefivefour.echolist.data.source.network.FakeTaskListRemoteDataSource
import net.onefivefour.echolist.network.error.NetworkException
import tasks.v1.CreateTaskListResponse
import tasks.v1.DeleteTaskListResponse
import tasks.v1.GetTaskListResponse
import tasks.v1.ListTaskListsResponse
import tasks.v1.UpdateTaskListResponse

class TaskListRepositoryImplTest : FunSpec({

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
            filePath = Arb.string(1..100).bind(),
            tasks = Arb.list(arbDomainMainTask, 0..3).bind()
        )
    }

    // -- CreateTaskList --

    test("createTaskList returns mapped task list on success").config(invocations = 20) {
        checkAll(arbCreateTaskListParams, arbProtoTaskList) { params, protoTaskList ->
            val fake = FakeTaskListRemoteDataSource()
            fake.createTaskListResult = Result.success(CreateTaskListResponse(task_list = protoTaskList))
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.createTaskList(params)

            result.isSuccess shouldBe true
            val taskList = result.getOrThrow()
            taskList.filePath shouldBe protoTaskList.file_path
            taskList.name shouldBe protoTaskList.title
            taskList.tasks.size shouldBe protoTaskList.tasks.size
            taskList.updatedAt shouldBe protoTaskList.updated_at
        }
    }

    test("createTaskList forwards correct proto fields to data source").config(invocations = 20) {
        checkAll(arbCreateTaskListParams) { params ->
            val fake = FakeTaskListRemoteDataSource()
            fake.createTaskListResult = Result.success(
                CreateTaskListResponse(
                    task_list = tasks.v1.TaskList(
                        file_path = "/test.json",
                        title = "t",
                        tasks = emptyList(),
                        updated_at = 0L
                    )
                )
            )
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            repo.createTaskList(params)

            fake.lastCreateRequest?.title shouldBe params.name
            fake.lastCreateRequest?.parent_dir shouldBe params.path
            fake.lastCreateRequest?.tasks?.size shouldBe params.tasks.size
        }
    }

    test("createTaskList returns failure when network throws") {
        val fake = FakeTaskListRemoteDataSource()
        fake.createTaskListResult = Result.failure(NetworkException.ServerError(500, "boom"))
        val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

        val result = repo.createTaskList(CreateTaskListParams("n", "/p", emptyList()))

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ServerError>()
    }

    // -- GetTaskList --

    test("getTaskList returns mapped task list on success").config(invocations = 20) {
        checkAll(Arb.string(1..100), arbProtoTaskList) { filePath, protoTaskList ->
            val fake = FakeTaskListRemoteDataSource()
            fake.getTaskListResult = Result.success(GetTaskListResponse(task_list = protoTaskList))
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.getTaskList(filePath)

            result.isSuccess shouldBe true
            val taskList = result.getOrThrow()
            taskList.filePath shouldBe protoTaskList.file_path
            taskList.name shouldBe protoTaskList.title
            taskList.tasks.size shouldBe protoTaskList.tasks.size
            taskList.updatedAt shouldBe protoTaskList.updated_at
        }
    }

    test("getTaskList forwards correct file_path to data source") {
        val fake = FakeTaskListRemoteDataSource()
        fake.getTaskListResult = Result.success(
            GetTaskListResponse(
                task_list = tasks.v1.TaskList(
                    file_path = "/x.json",
                    title = "t",
                    tasks = emptyList(),
                    updated_at = 0L
                )
            )
        )
        val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

        repo.getTaskList("/x.json")

        fake.lastGetRequest?.file_path shouldBe "/x.json"
    }

    test("getTaskList returns failure when network throws") {
        val fake = FakeTaskListRemoteDataSource()
        fake.getTaskListResult = Result.failure(NetworkException.ClientError(404, "not found"))
        val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

        val result = repo.getTaskList("/missing.json")

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ClientError>()
    }

    // -- ListTaskLists --

    test("listTaskLists returns mapped result on success") {
        val tl1 = tasks.v1.TaskList(
            file_path = "/lists/list1.json",
            title = "List 1",
            tasks = emptyList(),
            updated_at = 100L
        )
        val tl2 = tasks.v1.TaskList(
            file_path = "/lists/list2.json",
            title = "List 2",
            tasks = emptyList(),
            updated_at = 200L
        )
        val fake = FakeTaskListRemoteDataSource()
        fake.listTaskListsResult = Result.success(ListTaskListsResponse(task_lists = listOf(tl1, tl2)))
        val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

        val result = repo.listTaskLists("/lists")

        result.isSuccess shouldBe true
        val listResult = result.getOrThrow()
        listResult.taskLists.size shouldBe 2
        listResult.taskLists[0].filePath shouldBe "/lists/list1.json"
        listResult.taskLists[0].name shouldBe "List 1"
        listResult.taskLists[1].filePath shouldBe "/lists/list2.json"
        listResult.taskLists[1].name shouldBe "List 2"
    }

    test("listTaskLists forwards correct parent_dir to data source") {
        val fake = FakeTaskListRemoteDataSource()
        fake.listTaskListsResult = Result.success(ListTaskListsResponse())
        val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

        repo.listTaskLists("/some/path")

        fake.lastListRequest?.parent_dir shouldBe "/some/path"
    }

    test("listTaskLists returns empty result when response has no task lists") {
        val fake = FakeTaskListRemoteDataSource()
        fake.listTaskListsResult = Result.success(ListTaskListsResponse(task_lists = emptyList()))
        val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

        val result = repo.listTaskLists("")

        result.isSuccess shouldBe true
        result.getOrThrow().taskLists shouldBe emptyList()
    }

    test("listTaskLists returns failure when network throws") {
        val fake = FakeTaskListRemoteDataSource()
        fake.listTaskListsResult = Result.failure(NetworkException.TimeoutError("timed out"))
        val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

        val result = repo.listTaskLists("/any")

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.TimeoutError>()
    }

    // -- UpdateTaskList --

    test("updateTaskList returns mapped task list on success").config(invocations = 20) {
        checkAll(arbUpdateTaskListParams, arbProtoTaskList) { params, protoTaskList ->
            val fake = FakeTaskListRemoteDataSource()
            fake.updateTaskListResult = Result.success(UpdateTaskListResponse(task_list = protoTaskList))
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.updateTaskList(params)

            result.isSuccess shouldBe true
            val taskList = result.getOrThrow()
            taskList.filePath shouldBe protoTaskList.file_path
            taskList.name shouldBe protoTaskList.title
            taskList.tasks.size shouldBe protoTaskList.tasks.size
            taskList.updatedAt shouldBe protoTaskList.updated_at
        }
    }

    test("updateTaskList forwards correct proto fields to data source").config(invocations = 20) {
        checkAll(arbUpdateTaskListParams) { params ->
            val fake = FakeTaskListRemoteDataSource()
            fake.updateTaskListResult = Result.success(
                UpdateTaskListResponse(
                    task_list = tasks.v1.TaskList(
                        file_path = "/test.json",
                        title = "t",
                        tasks = emptyList(),
                        updated_at = 0L
                    )
                )
            )
            val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

            repo.updateTaskList(params)

            fake.lastUpdateRequest?.file_path shouldBe params.filePath
            fake.lastUpdateRequest?.tasks?.size shouldBe params.tasks.size
        }
    }

    test("updateTaskList returns failure when network throws") {
        val fake = FakeTaskListRemoteDataSource()
        fake.updateTaskListResult = Result.failure(NetworkException.ClientError(400, "bad request"))
        val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

        val result = repo.updateTaskList(UpdateTaskListParams("/p.json", emptyList()))

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ClientError>()
    }

    // -- DeleteTaskList --

    test("deleteTaskList returns Unit on success") {
        val fake = FakeTaskListRemoteDataSource()
        fake.deleteTaskListResult = Result.success(DeleteTaskListResponse())
        val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

        val result = repo.deleteTaskList("/list.json")

        result.isSuccess shouldBe true
        result.getOrThrow() shouldBe Unit
    }

    test("deleteTaskList forwards correct file_path to data source") {
        val fake = FakeTaskListRemoteDataSource()
        fake.deleteTaskListResult = Result.success(DeleteTaskListResponse())
        val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

        repo.deleteTaskList("/target.json")

        fake.lastDeleteRequest?.file_path shouldBe "/target.json"
    }

    test("deleteTaskList returns failure when network throws") {
        val fake = FakeTaskListRemoteDataSource()
        fake.deleteTaskListResult = Result.failure(NetworkException.NetworkError("timeout"))
        val repo = TaskListRepositoryImpl(fake, Dispatchers.Unconfined)

        val result = repo.deleteTaskList("/p.json")

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.NetworkError>()
    }
})
