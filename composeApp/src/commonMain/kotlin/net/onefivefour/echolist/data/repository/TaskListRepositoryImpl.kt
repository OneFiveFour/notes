package net.onefivefour.echolist.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.onefivefour.echolist.data.mapper.TaskListMapper
import net.onefivefour.echolist.data.models.CreateTaskListParams
import net.onefivefour.echolist.data.models.ListTaskListsResult
import net.onefivefour.echolist.data.models.TaskList
import net.onefivefour.echolist.data.models.UpdateTaskListParams
import net.onefivefour.echolist.data.source.network.TaskListRemoteDataSource
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

    override suspend fun getTaskList(filePath: String): Result<TaskList> =
        withContext(dispatcher) {
            try {
                val request = GetTaskListRequest(file_path = filePath)
                val response = networkDataSource.getTaskList(request)
                Result.success(TaskListMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun listTaskLists(path: String): Result<ListTaskListsResult> =
        withContext(dispatcher) {
            try {
                val request = ListTaskListsRequest(path = path)
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

    override suspend fun deleteTaskList(filePath: String): Result<Unit> =
        withContext(dispatcher) {
            try {
                val request = DeleteTaskListRequest(file_path = filePath)
                networkDataSource.deleteTaskList(request)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
