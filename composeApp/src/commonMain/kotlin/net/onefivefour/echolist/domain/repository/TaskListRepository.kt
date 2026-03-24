package net.onefivefour.echolist.domain.repository

import net.onefivefour.echolist.data.models.CreateTaskListParams
import net.onefivefour.echolist.data.dto.ListTaskListsResult
import net.onefivefour.echolist.domain.model.TaskList
import net.onefivefour.echolist.data.models.UpdateTaskListParams

interface TaskListRepository {
    suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList>
    suspend fun getTaskList(taskListId: String): Result<TaskList>
    suspend fun listTaskLists(parentDir: String): Result<ListTaskListsResult>
    suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList>
    suspend fun deleteTaskList(taskListId: String): Result<Unit>
}
