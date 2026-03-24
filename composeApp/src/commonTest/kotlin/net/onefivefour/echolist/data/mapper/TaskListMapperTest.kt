package net.onefivefour.echolist.data.mapper

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.onefivefour.echolist.data.models.CreateTaskListParams
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.SubTask
import net.onefivefour.echolist.data.models.UpdateTaskListParams

/**
 * Unit tests for TaskListMapper transformations.
 * Tests specific examples, edge cases, field name conversions,
 * and bidirectional transformations (domain → proto → domain).
 */
class TaskListMapperTest : FunSpec({

    // -- Proto -> Domain: SubTask --

    test("toDomain transforms proto SubTask to domain SubTask") {
        val proto = tasks.v1.SubTask(
            description = "Buy groceries",
            done = false
        )

        val domain = TaskListMapper.toDomain(proto)

        domain.description shouldBe "Buy groceries"
        domain.done shouldBe false
    }

    test("toDomain transforms completed proto SubTask") {
        val proto = tasks.v1.SubTask(
            description = "Send email",
            done = true
        )

        val domain = TaskListMapper.toDomain(proto)

        domain.description shouldBe "Send email"
        domain.done shouldBe true
    }

    // -- Proto -> Domain: MainTask --

    test("toDomain transforms proto MainTask with field name conversions (due_date, sub_tasks)") {
        val subTask = tasks.v1.SubTask(description = "Sub item", done = false)
        val proto = tasks.v1.MainTask(
            description = "Main task",
            done = false,
            due_date = "2026-03-15",
            recurrence = "weekly",
            sub_tasks = listOf(subTask)
        )

        val domain = TaskListMapper.toDomain(proto)

        domain.description shouldBe "Main task"
        domain.done shouldBe false
        domain.dueDate shouldBe "2026-03-15"
        domain.recurrence shouldBe "weekly"
        domain.subTasks shouldHaveSize 1
        domain.subTasks[0].description shouldBe "Sub item"
        domain.subTasks[0].done shouldBe false
    }

    test("toDomain transforms proto MainTask with multiple sub_tasks") {
        val proto = tasks.v1.MainTask(
            description = "Project planning",
            done = false,
            due_date = "2026-04-01",
            recurrence = "",
            sub_tasks = listOf(
                tasks.v1.SubTask(description = "Define scope", done = true),
                tasks.v1.SubTask(description = "Create timeline", done = false),
                tasks.v1.SubTask(description = "Assign resources", done = false)
            )
        )

        val domain = TaskListMapper.toDomain(proto)

        domain.subTasks shouldHaveSize 3
        domain.subTasks[0].description shouldBe "Define scope"
        domain.subTasks[0].done shouldBe true
        domain.subTasks[1].description shouldBe "Create timeline"
        domain.subTasks[2].description shouldBe "Assign resources"
    }

    test("toDomain transforms proto MainTask with empty sub_tasks list") {
        val proto = tasks.v1.MainTask(
            description = "Simple task",
            done = true,
            due_date = "",
            recurrence = "",
            sub_tasks = emptyList()
        )

        val domain = TaskListMapper.toDomain(proto)

        domain.description shouldBe "Simple task"
        domain.done shouldBe true
        domain.subTasks.shouldBeEmpty()
    }

    // -- Proto -> Domain: TaskList --

    test("toDomain transforms proto TaskList to domain TaskList with field name conversions") {
        val protoTaskList = tasks.v1.TaskList(
            id = "tl-uuid-1",
            file_path = "/home/user/lists/shopping.json",
            title = "Shopping List",
            tasks = listOf(
                tasks.v1.MainTask(
                    description = "Buy milk",
                    done = false,
                    due_date = "2026-03-10",
                    recurrence = "",
                    sub_tasks = emptyList()
                )
            ),
            updated_at = 1704067200000L
        )

        val domain = TaskListMapper.toDomain(protoTaskList)

        domain.id shouldBe "tl-uuid-1"
        domain.filePath shouldBe "/home/user/lists/shopping.json"
        domain.name shouldBe "Shopping List"
        domain.tasks shouldHaveSize 1
        domain.tasks[0].description shouldBe "Buy milk"
        domain.updatedAt shouldBe 1704067200000L
    }

    // -- Proto -> Domain: Response types --

    test("toDomain transforms CreateTaskListResponse to domain TaskList") {
        val protoTaskList = tasks.v1.TaskList(
            id = "tl-uuid-2",
            file_path = "/lists/new.json",
            title = "New List",
            tasks = emptyList(),
            updated_at = 1704153600000L
        )
        val response = tasks.v1.CreateTaskListResponse(task_list = protoTaskList)

        val domain = TaskListMapper.toDomain(response)

        domain.id shouldBe "tl-uuid-2"
        domain.filePath shouldBe "/lists/new.json"
        domain.name shouldBe "New List"
        domain.tasks.shouldBeEmpty()
        domain.updatedAt shouldBe 1704153600000L
    }

    test("toDomain transforms GetTaskListResponse to domain TaskList") {
        val protoTaskList = tasks.v1.TaskList(
            id = "tl-uuid-3",
            file_path = "/lists/existing.json",
            title = "Existing List",
            tasks = listOf(
                tasks.v1.MainTask(
                    description = "Task A",
                    done = true,
                    due_date = "",
                    recurrence = "daily",
                    sub_tasks = listOf(tasks.v1.SubTask(description = "Sub A", done = true))
                )
            ),
            updated_at = 1704240000000L
        )
        val response = tasks.v1.GetTaskListResponse(task_list = protoTaskList)

        val domain = TaskListMapper.toDomain(response)

        domain.id shouldBe "tl-uuid-3"
        domain.filePath shouldBe "/lists/existing.json"
        domain.name shouldBe "Existing List"
        domain.tasks shouldHaveSize 1
        domain.tasks[0].description shouldBe "Task A"
        domain.tasks[0].subTasks shouldHaveSize 1
        domain.updatedAt shouldBe 1704240000000L
    }

    test("toDomain transforms UpdateTaskListResponse to domain TaskList") {
        val protoTaskList = tasks.v1.TaskList(
            id = "tl-uuid-4",
            file_path = "/lists/updated.json",
            title = "Updated List",
            tasks = emptyList(),
            updated_at = 1704326400000L
        )
        val response = tasks.v1.UpdateTaskListResponse(task_list = protoTaskList)

        val domain = TaskListMapper.toDomain(response)

        domain.id shouldBe "tl-uuid-4"
        domain.filePath shouldBe "/lists/updated.json"
        domain.name shouldBe "Updated List"
        domain.updatedAt shouldBe 1704326400000L
    }

    test("toDomain transforms ListTaskListsResponse with multiple task lists") {
        val tl1 = tasks.v1.TaskList(
            id = "tl-uuid-5",
            file_path = "/lists/list1.json",
            title = "List 1",
            tasks = emptyList(),
            updated_at = 1704067200000L
        )
        val tl2 = tasks.v1.TaskList(
            id = "tl-uuid-6",
            file_path = "/lists/list2.json",
            title = "List 2",
            tasks = emptyList(),
            updated_at = 1704153600000L
        )
        val response = tasks.v1.ListTaskListsResponse(task_lists = listOf(tl1, tl2))

        val result = TaskListMapper.toDomain(response)

        result.taskLists shouldHaveSize 2
        result.taskLists[0].id shouldBe "tl-uuid-5"
        result.taskLists[0].filePath shouldBe "/lists/list1.json"
        result.taskLists[0].name shouldBe "List 1"
        result.taskLists[1].id shouldBe "tl-uuid-6"
        result.taskLists[1].filePath shouldBe "/lists/list2.json"
        result.taskLists[1].name shouldBe "List 2"
    }

    test("toDomain transforms ListTaskListsResponse with empty list") {
        val response = tasks.v1.ListTaskListsResponse(task_lists = emptyList())

        val result = TaskListMapper.toDomain(response)

        result.taskLists.shouldBeEmpty()
    }

    // -- Domain -> Proto transformations --

    test("toProto transforms CreateTaskListParams to CreateTaskListRequest") {
        val params = CreateTaskListParams(
            name = "My Tasks",
            path = "/home/user/lists",
            tasks = listOf(
                MainTask(
                    description = "First task",
                    done = false,
                    dueDate = "2026-03-20",
                    recurrence = "",
                    subTasks = listOf(SubTask(description = "Sub 1", done = false))
                )
            )
        )

        val proto = TaskListMapper.toProto(params)

        proto.title shouldBe "My Tasks"
        proto.parent_dir shouldBe "/home/user/lists"
        proto.tasks shouldHaveSize 1
        proto.tasks[0].description shouldBe "First task"
        proto.tasks[0].due_date shouldBe "2026-03-20"
        proto.tasks[0].sub_tasks shouldHaveSize 1
        proto.tasks[0].sub_tasks[0].description shouldBe "Sub 1"
    }

    test("toProto transforms UpdateTaskListParams to UpdateTaskListRequest with id field") {
        val params = UpdateTaskListParams(
            id = "tl-uuid-existing",
            tasks = listOf(
                MainTask(
                    description = "Updated task",
                    done = true,
                    dueDate = "",
                    recurrence = "monthly",
                    subTasks = emptyList()
                )
            )
        )

        val proto = TaskListMapper.toProto(params)

        proto.id shouldBe "tl-uuid-existing"
        proto.tasks shouldHaveSize 1
        proto.tasks[0].description shouldBe "Updated task"
        proto.tasks[0].done shouldBe true
        proto.tasks[0].recurrence shouldBe "monthly"
    }

    test("toProto transforms domain MainTask to proto MainTask with snake_case fields") {
        val domain = MainTask(
            description = "Test task",
            done = false,
            dueDate = "2026-06-15",
            recurrence = "weekly",
            subTasks = listOf(SubTask(description = "Child", done = true))
        )

        val proto = TaskListMapper.toProto(domain)

        proto.description shouldBe "Test task"
        proto.done shouldBe false
        proto.due_date shouldBe "2026-06-15"
        proto.recurrence shouldBe "weekly"
        proto.sub_tasks shouldHaveSize 1
        proto.sub_tasks[0].description shouldBe "Child"
        proto.sub_tasks[0].done shouldBe true
    }

    test("toProto transforms domain SubTask to proto SubTask") {
        val domain = SubTask(description = "Pick up package", done = false)

        val proto = TaskListMapper.toProto(domain)

        proto.description shouldBe "Pick up package"
        proto.done shouldBe false
    }

    // -- Bidirectional / round-trip transformations --

    test("round-trip: domain MainTask -> proto -> domain produces equivalent object") {
        val original = MainTask(
            description = "Round trip task",
            done = true,
            dueDate = "2026-12-25",
            recurrence = "yearly",
            subTasks = listOf(
                SubTask(description = "Wrap gifts", done = false),
                SubTask(description = "Send cards", done = true)
            )
        )

        val roundTripped = TaskListMapper.toDomain(TaskListMapper.toProto(original))

        roundTripped shouldBe original
    }

    test("round-trip: domain SubTask -> proto -> domain produces equivalent object") {
        val original = SubTask(description = "Round trip sub", done = true)

        val roundTripped = TaskListMapper.toDomain(TaskListMapper.toProto(original))

        roundTripped shouldBe original
    }

    // -- Edge cases --

    test("toDomain handles MainTask with empty strings for all fields") {
        val proto = tasks.v1.MainTask(
            description = "",
            done = false,
            due_date = "",
            recurrence = "",
            sub_tasks = emptyList()
        )

        val domain = TaskListMapper.toDomain(proto)

        domain.description shouldBe ""
        domain.dueDate shouldBe ""
        domain.recurrence shouldBe ""
        domain.subTasks.shouldBeEmpty()
    }

    test("toDomain handles SubTask with empty description") {
        val proto = tasks.v1.SubTask(description = "", done = true)

        val domain = TaskListMapper.toDomain(proto)

        domain.description shouldBe ""
        domain.done shouldBe true
    }

    test("toDomain handles MainTask with special characters") {
        val proto = tasks.v1.MainTask(
            description = "Special: Task! @#$%",
            done = false,
            due_date = "2026-01-01T00:00:00Z",
            recurrence = "every 2 weeks",
            sub_tasks = listOf(
                tasks.v1.SubTask(description = "Sub with\nnewline\tand\ttab", done = false)
            )
        )

        val domain = TaskListMapper.toDomain(proto)

        domain.description shouldBe "Special: Task! @#$%"
        domain.dueDate shouldBe "2026-01-01T00:00:00Z"
        domain.subTasks[0].description shouldBe "Sub with\nnewline\tand\ttab"
    }

    test("toDomain preserves timestamp precision") {
        val protoTaskList = tasks.v1.TaskList(
            id = "tl-uuid-ts",
            file_path = "/lists/ts.json",
            title = "Timestamp Test",
            tasks = emptyList(),
            updated_at = 1704067234567L
        )

        val domain = TaskListMapper.toDomain(protoTaskList)

        domain.updatedAt shouldBe 1704067234567L
    }

    test("toDomain transforms ListTaskListsResponse with single task list") {
        val tl = tasks.v1.TaskList(
            id = "tl-uuid-single",
            file_path = "/lists/single.json",
            title = "Single",
            tasks = emptyList(),
            updated_at = 100L
        )
        val response = tasks.v1.ListTaskListsResponse(task_lists = listOf(tl))

        val result = TaskListMapper.toDomain(response)

        result.taskLists shouldHaveSize 1
        result.taskLists[0].id shouldBe "tl-uuid-single"
        result.taskLists[0].filePath shouldBe "/lists/single.json"
    }
})
