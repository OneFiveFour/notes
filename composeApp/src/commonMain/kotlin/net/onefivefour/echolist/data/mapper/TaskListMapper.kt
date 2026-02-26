package net.onefivefour.echolist.data.mapper

import net.onefivefour.echolist.data.models.CreateTaskListParams
import net.onefivefour.echolist.data.models.ListTaskListsResult
import net.onefivefour.echolist.data.models.MainTask
import net.onefivefour.echolist.data.models.SubTask
import net.onefivefour.echolist.data.models.TaskList
import net.onefivefour.echolist.data.models.TaskListEntry
import net.onefivefour.echolist.data.models.UpdateTaskListParams
import tasks.v1.CreateTaskListRequest
import tasks.v1.CreateTaskListResponse
import tasks.v1.GetTaskListResponse
import tasks.v1.ListTaskListsResponse
import tasks.v1.UpdateTaskListRequest
import tasks.v1.UpdateTaskListResponse

/**
 * Maps between Wire-generated task list proto models and domain models.
 */
internal object TaskListMapper {

    // Proto -> Domain

    fun toDomain(proto: tasks.v1.SubTask): SubTask = SubTask(
        description = proto.description,
        done = proto.done
    )

    fun toDomain(proto: tasks.v1.MainTask): MainTask = MainTask(
        description = proto.description,
        done = proto.done,
        dueDate = proto.due_date,
        recurrence = proto.recurrence,
        subTasks = proto.sub_tasks.map { toDomain(it) }
    )

    fun toDomain(proto: CreateTaskListResponse): TaskList = TaskList(
        filePath = proto.file_path,
        name = proto.name,
        tasks = proto.tasks.map { toDomain(it) },
        updatedAt = proto.updated_at
    )

    fun toDomain(proto: GetTaskListResponse): TaskList = TaskList(
        filePath = proto.file_path,
        name = proto.name,
        tasks = proto.tasks.map { toDomain(it) },
        updatedAt = proto.updated_at
    )

    fun toDomain(proto: ListTaskListsResponse): ListTaskListsResult = ListTaskListsResult(
        taskLists = proto.task_lists.map { toDomain(it) },
        entries = proto.entries
    )

    fun toDomain(proto: tasks.v1.TaskListEntry): TaskListEntry = TaskListEntry(
        filePath = proto.file_path,
        name = proto.name,
        updatedAt = proto.updated_at
    )

    fun toDomain(proto: UpdateTaskListResponse): TaskList = TaskList(
        filePath = proto.file_path,
        name = proto.name,
        tasks = proto.tasks.map { toDomain(it) },
        updatedAt = proto.updated_at
    )

    // Domain -> Proto

    fun toProto(params: CreateTaskListParams): CreateTaskListRequest = CreateTaskListRequest(
        name = params.name,
        path = params.path,
        tasks = params.tasks.map { toProto(it) }
    )

    fun toProto(params: UpdateTaskListParams): UpdateTaskListRequest = UpdateTaskListRequest(
        file_path = params.filePath,
        tasks = params.tasks.map { toProto(it) }
    )

    fun toProto(domain: MainTask): tasks.v1.MainTask = tasks.v1.MainTask(
        description = domain.description,
        done = domain.done,
        due_date = domain.dueDate,
        recurrence = domain.recurrence,
        sub_tasks = domain.subTasks.map { toProto(it) }
    )

    fun toProto(domain: SubTask): tasks.v1.SubTask = tasks.v1.SubTask(
        description = domain.description,
        done = domain.done
    )
}
