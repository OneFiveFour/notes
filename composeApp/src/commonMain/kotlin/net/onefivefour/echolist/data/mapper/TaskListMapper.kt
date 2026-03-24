package net.onefivefour.echolist.data.mapper

import net.onefivefour.echolist.data.models.CreateTaskListParams
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.SubTask
import net.onefivefour.echolist.domain.model.TaskList
import net.onefivefour.echolist.domain.model.TaskListEntry
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
@Suppress("TooManyFunctions")
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

    fun toDomain(proto: tasks.v1.TaskList): TaskList = TaskList(
        id = proto.id,
        filePath = proto.file_path,
        name = proto.title,
        tasks = proto.tasks.map { toDomain(it) },
        updatedAt = proto.updated_at
    )

    fun toDomain(proto: CreateTaskListResponse): TaskList {
        val taskList = proto.task_list!!
        return toDomain(taskList)
    }

    fun toDomain(proto: GetTaskListResponse): TaskList {
        val taskList = proto.task_list!!
        return toDomain(taskList)
    }

    fun toDomain(proto: ListTaskListsResponse): List<TaskListEntry> =
        proto.task_lists.map { toEntry(it) }

    fun toEntry(proto: tasks.v1.TaskList): TaskListEntry = TaskListEntry(
        id = proto.id,
        filePath = proto.file_path,
        name = proto.title,
        updatedAt = proto.updated_at
    )

    fun toDomain(proto: UpdateTaskListResponse): TaskList {
        val taskList = proto.task_list!!
        return toDomain(taskList)
    }

    // Domain -> Proto

    fun toProto(params: CreateTaskListParams): CreateTaskListRequest = CreateTaskListRequest(
        title = params.name,
        parent_dir = params.path,
        tasks = params.tasks.map { toProto(it) }
    )

    fun toProto(params: UpdateTaskListParams): UpdateTaskListRequest = UpdateTaskListRequest(
        id = params.id,
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