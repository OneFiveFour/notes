package net.onefivefour.echolist.data.mapper

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.onefivefour.echolist.data.dto.CreateTaskListParams
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
            id = "sub-1",
            description = "Buy groceries",
            is_done = false
        )

        val domain = TaskListMapper.toDomain(proto)

        domain.id shouldBe "sub-1"
        domain.description shouldBe "Buy groceries"
        domain.isDone shouldBe false
    }

    test("toDomain transforms completed proto SubTask") {
        val proto = tasks.v1.SubTask(
            id = "sub-2",
            description = "Send email",
            is_done = true
        )

        val domain = TaskListMapper.toDomain(proto)

        domain.id shouldBe "sub-2"
        domain.description shouldBe "Send email"
        domain.isDone shouldBe true
    }

    // -- Proto -> Domain: MainTask --

    test("toDomain transforms proto MainTask with field name conversions (due_date, sub_tasks)") {
        val subTask = tasks.v1.SubTask(id = "sub-1", description = "Sub item", is_done = false)
        val proto = tasks.v1.MainTask(
            id = "main-1",
            description = "Main task",
            is_done = false,
            due_date = "2026-03-15",
            recurrence = "weekly",
            sub_tasks = listOf(subTask)
        )

        val domain = TaskListMapper.toDomain(proto)

        domain.id shouldBe "main-1"
        domain.description shouldBe "Main task"
        domain.isDone shouldBe false
        domain.dueDate shouldBe "2026-03-15"
        domain.recurrence shouldBe "weekly"
        domain.subTasks shouldHaveSize 1
        domain.subTasks[0].id shouldBe "sub-1"
        domain.subTasks[0].description shouldBe "Sub item"
        domain.subTasks[0].isDone shouldBe false
    }

    test("toDomain transforms proto MainTask with multiple sub_tasks") {
        val proto = tasks.v1.MainTask(
            id = "main-2",
            description = "Project planning",
            is_done = false,
            due_date = "2026-04-01",
            recurrence = "",
            sub_tasks = listOf(
                tasks.v1.SubTask(id = "s1", description = "Define scope", is_done = true),
                tasks.v1.SubTask(id = "s2", description = "Create timeline", is_done = false),
                tasks.v1.SubTask(id = "s3", description = "Assign resources", is_done = false)
            )
        )

        val domain = TaskListMapper.toDomain(proto)

        domain.subTasks shouldHaveSize 3
        domain.subTasks[0].description shouldBe "Define scope"
        domain.subTasks[0].isDone shouldBe true
        domain.subTasks[1].description shouldBe "Create timeline"
        domain.subTasks[2].description shouldBe "Assign resources"
    }

    test("toDomain transforms proto MainTask with empty sub_tasks list") {
        val proto = tasks.v1.MainTask(
            id = "main-3",
            description = "Simple task",
            is_done = true,
            due_date = "",
            recurrence = "",
            sub_tasks = emptyList()
        )

        val domain = TaskListMapper.toDomain(proto)

        domain.description shouldBe "Simple task"
        domain.isDone shouldBe true
        domain.subTasks.shouldBeEmpty()
    }

    // -- Proto -> Domain: TaskList --

    test("toDomain transforms proto TaskList to domain TaskList with field name conversions") {
        val protoTaskList = tasks.v1.TaskList(
            id = "tl-uuid-1",
            parent_dir = "home/user/lists",
            title = "Shopping List",
            tasks = listOf(
                tasks.v1.MainTask(
                    id = "main-1",
                    description = "Buy milk",
                    is_done = false,
                    due_date = "2026-03-10",
                    recurrence = "",
                    sub_tasks = emptyList()
                )
            ),
            updated_at = 1704067200000L,
            is_auto_delete = true
        )

        val domain = TaskListMapper.toDomain(protoTaskList)

        domain.id shouldBe "tl-uuid-1"
        domain.parentDir shouldBe "home/user/lists"
        domain.name shouldBe "Shopping List"
        domain.tasks shouldHaveSize 1
        domain.tasks[0].description shouldBe "Buy milk"
        domain.updatedAt shouldBe 1704067200000L
        domain.isAutoDelete shouldBe true
    }

    // -- Proto -> Domain: Response types --

    test("toDomain transforms CreateTaskListResponse to domain TaskList") {
        val protoTaskList = tasks.v1.TaskList(
            id = "tl-uuid-2",
            parent_dir = "lists",
            title = "New List",
            tasks = emptyList(),
            updated_at = 1704153600000L,
            is_auto_delete = false
        )
        val response = tasks.v1.CreateTaskListResponse(task_list = protoTaskList)

        val domain = TaskListMapper.toDomain(response)

        domain.id shouldBe "tl-uuid-2"
        domain.parentDir shouldBe "lists"
        domain.name shouldBe "New List"
        domain.tasks.shouldBeEmpty()
        domain.updatedAt shouldBe 1704153600000L
        domain.isAutoDelete shouldBe false
    }

    test("toDomain transforms GetTaskListResponse to domain TaskList") {
        val protoTaskList = tasks.v1.TaskList(
            id = "tl-uuid-3",
            parent_dir = "lists",
            title = "Existing List",
            tasks = listOf(
                tasks.v1.MainTask(
                    id = "main-1",
                    description = "Task A",
                    is_done = true,
                    due_date = "",
                    recurrence = "daily",
                    sub_tasks = listOf(tasks.v1.SubTask(id = "s1", description = "Sub A", is_done = true))
                )
            ),
            updated_at = 1704240000000L,
            is_auto_delete = true
        )
        val response = tasks.v1.GetTaskListResponse(task_list = protoTaskList)

        val domain = TaskListMapper.toDomain(response)

        domain.id shouldBe "tl-uuid-3"
        domain.parentDir shouldBe "lists"
        domain.name shouldBe "Existing List"
        domain.tasks shouldHaveSize 1
        domain.tasks[0].description shouldBe "Task A"
        domain.tasks[0].subTasks shouldHaveSize 1
        domain.updatedAt shouldBe 1704240000000L
        domain.isAutoDelete shouldBe true
    }

    test("toDomain transforms UpdateTaskListResponse to domain TaskList") {
        val protoTaskList = tasks.v1.TaskList(
            id = "tl-uuid-4",
            parent_dir = "lists",
            title = "Updated List",
            tasks = emptyList(),
            updated_at = 1704326400000L,
            is_auto_delete = true
        )
        val response = tasks.v1.UpdateTaskListResponse(task_list = protoTaskList)

        val domain = TaskListMapper.toDomain(response)

        domain.id shouldBe "tl-uuid-4"
        domain.parentDir shouldBe "lists"
        domain.name shouldBe "Updated List"
        domain.updatedAt shouldBe 1704326400000L
        domain.isAutoDelete shouldBe true
    }

    test("toDomain transforms ListTaskListsResponse with multiple task lists") {
        val tl1 = tasks.v1.TaskList(
            id = "tl-uuid-5",
            parent_dir = "lists",
            title = "List 1",
            tasks = emptyList(),
            updated_at = 1704067200000L
        )
        val tl2 = tasks.v1.TaskList(
            id = "tl-uuid-6",
            parent_dir = "lists",
            title = "List 2",
            tasks = emptyList(),
            updated_at = 1704153600000L
        )
        val response = tasks.v1.ListTaskListsResponse(task_lists = listOf(tl1, tl2))

        val result = TaskListMapper.toDomain(response)

        result shouldHaveSize 2
        result[0].id shouldBe "tl-uuid-5"
        result[0].parentDir shouldBe "lists"
        result[0].name shouldBe "List 1"
        result[1].id shouldBe "tl-uuid-6"
        result[1].parentDir shouldBe "lists"
        result[1].name shouldBe "List 2"
    }

    test("toDomain transforms ListTaskListsResponse with empty list") {
        val response = tasks.v1.ListTaskListsResponse(task_lists = emptyList())

        val result = TaskListMapper.toDomain(response)

        result.shouldBeEmpty()
    }

    // -- Domain -> Proto transformations --

    test("toProto transforms CreateTaskListParams to CreateTaskListRequest") {
        val params = CreateTaskListParams(
            name = "My Tasks",
            path = "/home/user/lists",
            isAutoDelete = true,
            tasks = listOf(
                MainTask(
                    id = "main-1",
                    description = "First task",
                    isDone = false,
                    dueDate = "2026-03-20",
                    recurrence = "",
                    subTasks = listOf(SubTask(id = "sub-1", description = "Sub 1", isDone = false))
                )
            )
        )

        val proto = TaskListMapper.toProto(params)

        proto.title shouldBe "My Tasks"
        proto.parent_dir shouldBe "/home/user/lists"
        proto.is_auto_delete shouldBe true
        proto.tasks shouldHaveSize 1
        proto.tasks[0].id shouldBe "main-1"
        proto.tasks[0].description shouldBe "First task"
        proto.tasks[0].due_date shouldBe "2026-03-20"
        proto.tasks[0].sub_tasks shouldHaveSize 1
        proto.tasks[0].sub_tasks[0].id shouldBe "sub-1"
        proto.tasks[0].sub_tasks[0].description shouldBe "Sub 1"
    }

    test("toProto transforms UpdateTaskListParams to UpdateTaskListRequest with id and title fields") {
        val params = UpdateTaskListParams(
            id = "tl-uuid-existing",
            title = "Updated List Name",
            isAutoDelete = true,
            tasks = listOf(
                MainTask(
                    id = "main-1",
                    description = "Updated task",
                    isDone = true,
                    dueDate = "",
                    recurrence = "monthly",
                    subTasks = emptyList()
                )
            )
        )

        val proto = TaskListMapper.toProto(params)

        proto.id shouldBe "tl-uuid-existing"
        proto.title shouldBe "Updated List Name"
        proto.is_auto_delete shouldBe true
        proto.tasks shouldHaveSize 1
        proto.tasks[0].id shouldBe "main-1"
        proto.tasks[0].description shouldBe "Updated task"
        proto.tasks[0].is_done shouldBe true
        proto.tasks[0].recurrence shouldBe "monthly"
    }

    test("toProto transforms domain MainTask to proto MainTask with snake_case fields") {
        val domain = MainTask(
            id = "main-1",
            description = "Test task",
            isDone = false,
            dueDate = "2026-06-15",
            recurrence = "weekly",
            subTasks = listOf(SubTask(id = "sub-1", description = "Child", isDone = true))
        )

        val proto = TaskListMapper.toProto(domain)

        proto.id shouldBe "main-1"
        proto.description shouldBe "Test task"
        proto.is_done shouldBe false
        proto.due_date shouldBe "2026-06-15"
        proto.recurrence shouldBe "weekly"
        proto.sub_tasks shouldHaveSize 1
        proto.sub_tasks[0].id shouldBe "sub-1"
        proto.sub_tasks[0].description shouldBe "Child"
        proto.sub_tasks[0].is_done shouldBe true
    }

    test("toProto transforms domain SubTask to proto SubTask") {
        val domain = SubTask(id = "sub-1", description = "Pick up package", isDone = false)

        val proto = TaskListMapper.toProto(domain)

        proto.id shouldBe "sub-1"
        proto.description shouldBe "Pick up package"
        proto.is_done shouldBe false
    }

    // -- Bidirectional / round-trip transformations --

    test("round-trip: domain MainTask -> proto -> domain produces equivalent object") {
        val original = MainTask(
            id = "main-1",
            description = "Round trip task",
            isDone = true,
            dueDate = "2026-12-25",
            recurrence = "yearly",
            subTasks = listOf(
                SubTask(id = "sub-1", description = "Wrap gifts", isDone = false),
                SubTask(id = "sub-2", description = "Send cards", isDone = true)
            )
        )

        val roundTripped = TaskListMapper.toDomain(TaskListMapper.toProto(original))

        roundTripped shouldBe original
    }

    test("round-trip: domain SubTask -> proto -> domain produces equivalent object") {
        val original = SubTask(id = "sub-1", description = "Round trip sub", isDone = true)

        val roundTripped = TaskListMapper.toDomain(TaskListMapper.toProto(original))

        roundTripped shouldBe original
    }

    // -- Edge cases --

    test("toDomain handles MainTask with empty strings for all fields") {
        val proto = tasks.v1.MainTask(
            id = "",
            description = "",
            is_done = false,
            due_date = "",
            recurrence = "",
            sub_tasks = emptyList()
        )

        val domain = TaskListMapper.toDomain(proto)

        domain.id shouldBe ""
        domain.description shouldBe ""
        domain.dueDate shouldBe ""
        domain.recurrence shouldBe ""
        domain.subTasks.shouldBeEmpty()
    }

    test("toDomain handles SubTask with empty description") {
        val proto = tasks.v1.SubTask(id = "sub-1", description = "", is_done = true)

        val domain = TaskListMapper.toDomain(proto)

        domain.id shouldBe "sub-1"
        domain.description shouldBe ""
        domain.isDone shouldBe true
    }

    test("toDomain handles MainTask with special characters") {
        val proto = tasks.v1.MainTask(
            id = "main-special",
            description = "Special: Task! @#$%",
            is_done = false,
            due_date = "2026-01-01T00:00:00Z",
            recurrence = "every 2 weeks",
            sub_tasks = listOf(
                tasks.v1.SubTask(id = "sub-special", description = "Sub with\nnewline\tand\ttab", is_done = false)
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
            parent_dir = "lists",
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
            parent_dir = "lists",
            title = "Single",
            tasks = emptyList(),
            updated_at = 100L
        )
        val response = tasks.v1.ListTaskListsResponse(task_lists = listOf(tl))

        val result = TaskListMapper.toDomain(response)

        result shouldHaveSize 1
        result[0].id shouldBe "tl-uuid-single"
        result[0].parentDir shouldBe "lists"
    }
})
