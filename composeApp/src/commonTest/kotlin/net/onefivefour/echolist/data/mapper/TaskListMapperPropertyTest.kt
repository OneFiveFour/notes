package net.onefivefour.echolist.data.mapper

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.echolist.data.dto.CreateTaskListParams
import net.onefivefour.echolist.data.models.UpdateTaskListParams
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.SubTask

/**
 * Feature: proto-api-update
 * Property 16: TaskListMapper transforms MainTask proto messages correctly
 * Property 17: TaskListMapper transforms SubTask proto messages correctly
 * Property 18: TaskListMapper transforms TaskList response messages correctly
 * Property 19: TaskListMapper transforms ListTaskListsResponse correctly
 * Property 20: TaskListMapper round-trip transformation preserves data
 *
 * Validates: Requirements 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7
 */
class TaskListMapperPropertyTest : FunSpec({

    // -- Generators --

    val arbProtoSubTask = arbitrary {
        tasks.v1.SubTask(
            description = Arb.string(0..200).bind(),
            done = Arb.boolean().bind()
        )
    }

    val arbProtoMainTask = arbitrary {
        tasks.v1.MainTask(
            description = Arb.string(0..200).bind(),
            done = Arb.boolean().bind(),
            due_date = Arb.string(0..50).bind(),
            recurrence = Arb.string(0..50).bind(),
            sub_tasks = Arb.list(arbProtoSubTask, 0..10).bind()
        )
    }

    val arbProtoTaskList = arbitrary {
        tasks.v1.TaskList(
            id = Arb.string(1..50).bind(),
            file_path = Arb.string(1..100).bind(),
            title = Arb.string(1..100).bind(),
            tasks = Arb.list(arbProtoMainTask, 0..5).bind(),
            updated_at = Arb.long(0L..Long.MAX_VALUE).bind(),
            is_auto_delete = Arb.boolean().bind()
        )
    }

    val arbDomainSubTask = arbitrary {
        SubTask(
            description = Arb.string(0..200).bind(),
            isDone = Arb.boolean().bind()
        )
    }

    val arbDomainMainTask = arbitrary {
        MainTask(
            description = Arb.string(0..200).bind(),
            isDone = Arb.boolean().bind(),
            dueDate = Arb.string(0..50).bind(),
            recurrence = Arb.string(0..50).bind(),
            subTasks = Arb.list(arbDomainSubTask, 0..10).bind()
        )
    }

    val arbCreateTaskListParams = arbitrary {
        CreateTaskListParams(
            name = Arb.string(1..100).bind(),
            path = Arb.string(0..100).bind(),
            tasks = Arb.list(arbDomainMainTask, 0..5).bind(),
            isAutoDelete = Arb.boolean().bind()
        )
    }

    val arbUpdateTaskListParams = arbitrary {
        UpdateTaskListParams(
            id = Arb.string(1..100).bind(),
            title = Arb.string(1..100).bind(),
            tasks = Arb.list(arbDomainMainTask, 0..5).bind(),
            isAutoDelete = Arb.boolean().bind()
        )
    }

    // -- Helper assertions --

    fun assertSubTasksMatch(domainList: List<SubTask>, protoList: List<tasks.v1.SubTask>) {
        domainList shouldHaveSize protoList.size
        domainList.zip(protoList).forEach { (d, p) ->
            d.description shouldBe p.description
            d.isDone shouldBe p.done
        }
    }

    fun assertMainTasksMatch(domainList: List<MainTask>, protoList: List<tasks.v1.MainTask>) {
        domainList shouldHaveSize protoList.size
        domainList.zip(protoList).forEach { (d, p) ->
            d.description shouldBe p.description
            d.isDone shouldBe p.done
            d.dueDate shouldBe p.due_date
            d.recurrence shouldBe p.recurrence
            assertSubTasksMatch(d.subTasks, p.sub_tasks)
        }
    }

    // ---------------------------------------------------------------
    // Property 16: TaskListMapper transforms MainTask proto messages correctly
    // Validates: Requirements 9.1
    // ---------------------------------------------------------------

    test("Feature: proto-api-update, Property 16: TaskListMapper transforms MainTask proto messages correctly") {
        checkAll(PropTestConfig(iterations = 100), arbProtoMainTask) { protoTask ->
            val domain = TaskListMapper.toDomain(protoTask)
            domain.description shouldBe protoTask.description
            domain.isDone shouldBe protoTask.done
            domain.dueDate shouldBe protoTask.due_date
            domain.recurrence shouldBe protoTask.recurrence
            assertSubTasksMatch(domain.subTasks, protoTask.sub_tasks)
        }
    }

    // ---------------------------------------------------------------
    // Property 17: TaskListMapper transforms SubTask proto messages correctly
    // Validates: Requirements 9.2
    // ---------------------------------------------------------------

    test("Feature: proto-api-update, Property 17: TaskListMapper transforms SubTask proto messages correctly") {
        checkAll(PropTestConfig(iterations = 100), arbProtoSubTask) { protoSubTask ->
            val domain = TaskListMapper.toDomain(protoSubTask)
            domain.description shouldBe protoSubTask.description
            domain.isDone shouldBe protoSubTask.done
        }
    }

    // ---------------------------------------------------------------
    // Property 18: TaskListMapper transforms TaskList response messages correctly
    // Validates: Requirements 9.3, 9.4, 9.6
    // ---------------------------------------------------------------

    test("Feature: proto-api-update, Property 18: CreateTaskListResponse -> domain TaskList preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbProtoTaskList) { protoTaskList ->
            val response = tasks.v1.CreateTaskListResponse(task_list = protoTaskList)
            val domain = TaskListMapper.toDomain(response)
            domain.id shouldBe protoTaskList.id
            domain.filePath shouldBe protoTaskList.file_path
            domain.name shouldBe protoTaskList.title
            domain.updatedAt shouldBe protoTaskList.updated_at
            domain.isAutoDelete shouldBe protoTaskList.is_auto_delete
            assertMainTasksMatch(domain.tasks, protoTaskList.tasks)
        }
    }

    test("Feature: proto-api-update, Property 18: GetTaskListResponse -> domain TaskList preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbProtoTaskList) { protoTaskList ->
            val response = tasks.v1.GetTaskListResponse(task_list = protoTaskList)
            val domain = TaskListMapper.toDomain(response)
            domain.id shouldBe protoTaskList.id
            domain.filePath shouldBe protoTaskList.file_path
            domain.name shouldBe protoTaskList.title
            domain.updatedAt shouldBe protoTaskList.updated_at
            domain.isAutoDelete shouldBe protoTaskList.is_auto_delete
            assertMainTasksMatch(domain.tasks, protoTaskList.tasks)
        }
    }

    test("Feature: proto-api-update, Property 18: UpdateTaskListResponse -> domain TaskList preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbProtoTaskList) { protoTaskList ->
            val response = tasks.v1.UpdateTaskListResponse(task_list = protoTaskList)
            val domain = TaskListMapper.toDomain(response)
            domain.id shouldBe protoTaskList.id
            domain.filePath shouldBe protoTaskList.file_path
            domain.name shouldBe protoTaskList.title
            domain.updatedAt shouldBe protoTaskList.updated_at
            domain.isAutoDelete shouldBe protoTaskList.is_auto_delete
            assertMainTasksMatch(domain.tasks, protoTaskList.tasks)
        }
    }

    test("Feature: proto-api-update, Property 18: CreateTaskListParams -> proto request preserves isAutoDelete") {
        checkAll(PropTestConfig(iterations = 100), arbCreateTaskListParams) { params ->
            TaskListMapper.toProto(params).is_auto_delete shouldBe params.isAutoDelete
        }
    }

    test("Feature: proto-api-update, Property 18: UpdateTaskListParams -> proto request preserves isAutoDelete") {
        checkAll(PropTestConfig(iterations = 100), arbUpdateTaskListParams) { params ->
            TaskListMapper.toProto(params).is_auto_delete shouldBe params.isAutoDelete
        }
    }

    // ---------------------------------------------------------------
    // Property 19: TaskListMapper transforms ListTaskListsResponse correctly
    // Validates: Requirements 9.5
    // ---------------------------------------------------------------

    test("Feature: proto-api-update, Property 19: TaskListMapper transforms ListTaskListsResponse correctly") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(arbProtoTaskList, 0..100)
        ) { protoTaskLists ->
            val response = tasks.v1.ListTaskListsResponse(task_lists = protoTaskLists)
            val result = TaskListMapper.toDomain(response)
            result shouldHaveSize protoTaskLists.size
            result.zip(protoTaskLists).forEach { (d, p) ->
                d.id shouldBe p.id
                d.filePath shouldBe p.file_path
                d.name shouldBe p.title
                d.updatedAt shouldBe p.updated_at
            }
        }
    }

    // ---------------------------------------------------------------
    // Property 20: TaskListMapper round-trip transformation preserves data
    // Validates: Requirements 9.7
    // ---------------------------------------------------------------

    // ---------------------------------------------------------------
    // Feature: tasklist-auto-delete, Property 3: Mapper create round-trip preserves isAutoDelete
    // Validates: Requirements 6.1, 3.3
    // ---------------------------------------------------------------

    test(
        "Feature: tasklist-auto-delete, Property 3: Mapper create round-trip preserves isAutoDelete"
    ) {
        checkAll(
            PropTestConfig(iterations = 100),
            arbCreateTaskListParams,
            arbProtoTaskList
        ) { params, baseProtoTaskList ->
            // Step 1: Map CreateTaskListParams -> CreateTaskListRequest
            val request = TaskListMapper.toProto(params)

            // Step 2: Simulate backend response by constructing a TaskList proto
            // with the same is_auto_delete as the request
            val responseProto = baseProtoTaskList.copy(
                is_auto_delete = request.is_auto_delete
            )

            // Step 3: Map the response TaskList proto back to domain
            val domain = TaskListMapper.toDomain(responseProto)

            // The round-tripped isAutoDelete must equal the original
            domain.isAutoDelete shouldBe params.isAutoDelete
        }
    }

    // ---------------------------------------------------------------
    // Feature: tasklist-auto-delete, Property 4: Mapper update round-trip preserves isAutoDelete
    // Validates: Requirements 6.2, 4.2
    // ---------------------------------------------------------------

    test(
        "Feature: tasklist-auto-delete, Property 4: Mapper update round-trip preserves isAutoDelete"
    ) {
        checkAll(
            PropTestConfig(iterations = 100),
            arbUpdateTaskListParams,
            arbProtoTaskList
        ) { params, baseProtoTaskList ->
            // Step 1: Map UpdateTaskListParams -> UpdateTaskListRequest
            val request = TaskListMapper.toProto(params)

            // Step 2: Simulate backend response by constructing a TaskList proto
            // with the same is_auto_delete as the request
            val responseProto = baseProtoTaskList.copy(
                is_auto_delete = request.is_auto_delete
            )

            // Step 3: Map the response TaskList proto back to domain
            val domain = TaskListMapper.toDomain(responseProto)

            // The round-tripped isAutoDelete must equal the original
            domain.isAutoDelete shouldBe params.isAutoDelete
        }
    }

    test(
        "Feature: proto-api-update, Property 20: domain MainTask -> proto -> " +
            "domain round-trip produces equivalent object"
    ) {
        checkAll(PropTestConfig(iterations = 100), arbDomainMainTask) { original ->
            val proto = TaskListMapper.toProto(original)
            val roundTripped = TaskListMapper.toDomain(proto)
            roundTripped shouldBe original
        }
    }

    test(
        "Feature: proto-api-update, Property 20: domain SubTask -> proto -> " +
            "domain round-trip produces equivalent object"
    ) {
        checkAll(PropTestConfig(iterations = 100), arbDomainSubTask) { original ->
            val proto = TaskListMapper.toProto(original)
            val roundTripped = TaskListMapper.toDomain(proto)
            roundTripped shouldBe original
        }
    }
})
