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
import net.onefivefour.echolist.data.models.CreateTaskListParams
import net.onefivefour.echolist.data.models.MainTask
import net.onefivefour.echolist.data.models.SubTask
import net.onefivefour.echolist.data.models.UpdateTaskListParams

/**
 * Feature: proto-api-update
 * Property 4: TaskList mapper proto-to-domain field preservation
 * Property 5: TaskList mapper domain-to-proto field preservation
 * Property 6: TaskList mapping round-trip
 *
 * Validates: Requirements 10.1–10.9
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
            sub_tasks = Arb.list(arbProtoSubTask, 0..5).bind()
        )
    }

    val arbProtoTaskList = arbitrary {
        tasks.v1.TaskList(
            file_path = Arb.string(1..100).bind(),
            title = Arb.string(1..100).bind(),
            tasks = Arb.list(arbProtoMainTask, 0..5).bind(),
            updated_at = Arb.long(0L..Long.MAX_VALUE).bind()
        )
    }

    val arbDomainSubTask = arbitrary {
        SubTask(
            description = Arb.string(0..200).bind(),
            done = Arb.boolean().bind()
        )
    }

    val arbDomainMainTask = arbitrary {
        MainTask(
            description = Arb.string(0..200).bind(),
            done = Arb.boolean().bind(),
            dueDate = Arb.string(0..50).bind(),
            recurrence = Arb.string(0..50).bind(),
            subTasks = Arb.list(arbDomainSubTask, 0..5).bind()
        )
    }

    val arbCreateTaskListParams = arbitrary {
        CreateTaskListParams(
            name = Arb.string(1..100).bind(),
            path = Arb.string(1..100).bind(),
            tasks = Arb.list(arbDomainMainTask, 0..5).bind()
        )
    }

    val arbUpdateTaskListParams = arbitrary {
        UpdateTaskListParams(
            filePath = Arb.string(1..100).bind(),
            tasks = Arb.list(arbDomainMainTask, 0..5).bind()
        )
    }

    // -- Helper to compare SubTask lists recursively --

    fun assertSubTasksMatch(domainList: List<SubTask>, protoList: List<tasks.v1.SubTask>) {
        domainList shouldHaveSize protoList.size
        domainList.zip(protoList).forEach { (d, p) ->
            d.description shouldBe p.description
            d.done shouldBe p.done
        }
    }

    fun assertMainTasksMatch(domainList: List<MainTask>, protoList: List<tasks.v1.MainTask>) {
        domainList shouldHaveSize protoList.size
        domainList.zip(protoList).forEach { (d, p) ->
            d.description shouldBe p.description
            d.done shouldBe p.done
            d.dueDate shouldBe p.due_date
            d.recurrence shouldBe p.recurrence
            assertSubTasksMatch(d.subTasks, p.sub_tasks)
        }
    }

    // -- Property 4: Proto -> Domain field preservation --

    test("Feature: proto-api-update, Property 4: proto MainTask with nested SubTasks -> domain preserves all fields recursively") {
        checkAll(PropTestConfig(iterations = 100), arbProtoMainTask) { protoTask ->
            val domain = TaskListMapper.toDomain(protoTask)
            domain.description shouldBe protoTask.description
            domain.done shouldBe protoTask.done
            domain.dueDate shouldBe protoTask.due_date
            domain.recurrence shouldBe protoTask.recurrence
            assertSubTasksMatch(domain.subTasks, protoTask.sub_tasks)
        }
    }

    test("Feature: proto-api-update, Property 4: CreateTaskListResponse -> domain TaskList preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbProtoTaskList) { protoTaskList ->
            val response = tasks.v1.CreateTaskListResponse(task_list = protoTaskList)
            val domain = TaskListMapper.toDomain(response)
            domain.filePath shouldBe protoTaskList.file_path
            domain.name shouldBe protoTaskList.title
            domain.updatedAt shouldBe protoTaskList.updated_at
            assertMainTasksMatch(domain.tasks, protoTaskList.tasks)
        }
    }

    test("Feature: proto-api-update, Property 4: GetTaskListResponse -> domain TaskList preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbProtoTaskList) { protoTaskList ->
            val response = tasks.v1.GetTaskListResponse(task_list = protoTaskList)
            val domain = TaskListMapper.toDomain(response)
            domain.filePath shouldBe protoTaskList.file_path
            domain.name shouldBe protoTaskList.title
            domain.updatedAt shouldBe protoTaskList.updated_at
            assertMainTasksMatch(domain.tasks, protoTaskList.tasks)
        }
    }

    test("Feature: proto-api-update, Property 4: UpdateTaskListResponse -> domain TaskList preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbProtoTaskList) { protoTaskList ->
            val response = tasks.v1.UpdateTaskListResponse(task_list = protoTaskList)
            val domain = TaskListMapper.toDomain(response)
            domain.filePath shouldBe protoTaskList.file_path
            domain.name shouldBe protoTaskList.title
            domain.updatedAt shouldBe protoTaskList.updated_at
            assertMainTasksMatch(domain.tasks, protoTaskList.tasks)
        }
    }

    test("Feature: proto-api-update, Property 4: ListTaskListsResponse -> domain ListTaskListsResult preserves all fields") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(arbProtoTaskList, 0..10)
        ) { protoTaskLists ->
            val response = tasks.v1.ListTaskListsResponse(task_lists = protoTaskLists)
            val result = TaskListMapper.toDomain(response)
            result.taskLists shouldHaveSize protoTaskLists.size
            result.taskLists.zip(protoTaskLists).forEach { (d, p) ->
                d.filePath shouldBe p.file_path
                d.name shouldBe p.title
                d.updatedAt shouldBe p.updated_at
            }
        }
    }

    // -- Property 5: Domain -> Proto field preservation --

    test("Feature: proto-api-update, Property 5: CreateTaskListParams -> CreateTaskListRequest preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbCreateTaskListParams) { params ->
            val proto = TaskListMapper.toProto(params)
            proto.title shouldBe params.name
            proto.parent_dir shouldBe params.path
            proto.tasks shouldHaveSize params.tasks.size
            proto.tasks.zip(params.tasks).forEach { (p, d) ->
                p.description shouldBe d.description
                p.done shouldBe d.done
                p.due_date shouldBe d.dueDate
                p.recurrence shouldBe d.recurrence
                p.sub_tasks shouldHaveSize d.subTasks.size
                p.sub_tasks.zip(d.subTasks).forEach { (ps, ds) ->
                    ps.description shouldBe ds.description
                    ps.done shouldBe ds.done
                }
            }
        }
    }

    test("Feature: proto-api-update, Property 5: UpdateTaskListParams -> UpdateTaskListRequest preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbUpdateTaskListParams) { params ->
            val proto = TaskListMapper.toProto(params)
            proto.file_path shouldBe params.filePath
            proto.tasks shouldHaveSize params.tasks.size
            proto.tasks.zip(params.tasks).forEach { (p, d) ->
                p.description shouldBe d.description
                p.done shouldBe d.done
                p.due_date shouldBe d.dueDate
                p.recurrence shouldBe d.recurrence
                p.sub_tasks shouldHaveSize d.subTasks.size
                p.sub_tasks.zip(d.subTasks).forEach { (ps, ds) ->
                    ps.description shouldBe ds.description
                    ps.done shouldBe ds.done
                }
            }
        }
    }

    // -- Property 6: Round-trip --

    test("Feature: proto-api-update, Property 6: domain MainTask -> proto -> domain round-trip produces equivalent object") {
        checkAll(PropTestConfig(iterations = 100), arbDomainMainTask) { original ->
            val proto = TaskListMapper.toProto(original)
            val roundTripped = TaskListMapper.toDomain(proto)
            roundTripped shouldBe original
        }
    }

    test("Feature: proto-api-update, Property 6: domain SubTask -> proto -> domain round-trip produces equivalent object") {
        checkAll(PropTestConfig(iterations = 100), arbDomainSubTask) { original ->
            val proto = TaskListMapper.toProto(original)
            val roundTripped = TaskListMapper.toDomain(proto)
            roundTripped shouldBe original
        }
    }
})
