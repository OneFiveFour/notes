package net.onefivefour.echolist.data.repository

import net.onefivefour.echolist.data.models.CreateTaskListParams
import net.onefivefour.echolist.data.models.ListTaskListsResult
import net.onefivefour.echolist.data.models.TaskList
import net.onefivefour.echolist.data.models.UpdateTaskListParams

interface TaskListRepository {
    suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList>
    suspend fun getTaskList(filePath: String): Result<TaskList>
    suspend fun listTaskLists(path: String): Result<ListTaskListsResult>
    suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList>
    suspend fun deleteTaskList(filePath: String): Result<Unit>
}
