package net.onefivefour.echolist.domain.repository

import net.onefivefour.echolist.data.dto.CreateTaskListParams
import net.onefivefour.echolist.domain.model.TaskList
import net.onefivefour.echolist.domain.model.TaskListEntry
import net.onefivefour.echolist.data.models.UpdateTaskListParams

interface TaskListRepository {
    suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList>
    suspend fun getTaskList(taskListId: String): Result<TaskList>
    suspend fun listTaskLists(parentDir: String): Result<List<TaskListEntry>>
    suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList>
    suspend fun deleteTaskList(taskListId: String): Result<Unit>
}
