package net.onefivefour.echolist.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.onefivefour.echolist.data.mapper.TaskListMapper
import net.onefivefour.echolist.data.models.CreateTaskListParams
import net.onefivefour.echolist.data.dto.ListTaskListsResult
import net.onefivefour.echolist.domain.model.TaskList
import net.onefivefour.echolist.data.models.UpdateTaskListParams
import net.onefivefour.echolist.data.source.network.TaskListRemoteDataSource
import net.onefivefour.echolist.domain.repository.TaskListRepository
import tasks.v1.DeleteTaskListRequest
import tasks.v1.GetTaskListRequest
import tasks.v1.ListTaskListsRequest

internal class TaskListRepositoryImpl(
    private val networkDataSource: TaskListRemoteDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : TaskListRepository {

    override suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList> =
        withContext(dispatcher) {
            try {
                val request = TaskListMapper.toProto(params)
                val response = networkDataSource.createTaskList(request)
                Result.success(TaskListMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getTaskList(taskListId: String): Result<TaskList> =
        withContext(dispatcher) {
            try {
                val request = GetTaskListRequest(id = taskListId)
                val response = networkDataSource.getTaskList(request)
                Result.success(TaskListMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun listTaskLists(parentDir: String): Result<ListTaskListsResult> =
        withContext(dispatcher) {
            try {
                val request = ListTaskListsRequest(parent_dir = parentDir)
                val response = networkDataSource.listTaskLists(request)
                Result.success(TaskListMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList> =
        withContext(dispatcher) {
            try {
                val request = TaskListMapper.toProto(params)
                val response = networkDataSource.updateTaskList(request)
                Result.success(TaskListMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteTaskList(taskListId: String): Result<Unit> =
        withContext(dispatcher) {
            try {
                val request = DeleteTaskListRequest(id = taskListId)
                networkDataSource.deleteTaskList(request)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}