package net.onefivefour.echolist.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.onefivefour.echolist.data.mapper.TaskListMapper
import net.onefivefour.echolist.data.dto.CreateTaskListParams
import net.onefivefour.echolist.domain.model.TaskList
import net.onefivefour.echolist.domain.model.TaskListEntry
import net.onefivefour.echolist.data.models.UpdateTaskListParams
import net.onefivefour.echolist.data.source.network.TaskListRemoteDataSource
import net.onefivefour.echolist.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.domain.repository.TaskListRepository
import tasks.v1.DeleteTaskListRequest
import tasks.v1.GetTaskListRequest
import tasks.v1.ListTaskListsRequest

internal class TaskListRepositoryImpl(
    private val networkDataSource: TaskListRemoteDataSource,
    private val dispatcher: CoroutineDispatcher,
    private val directoryChangeNotifier: DirectoryChangeNotifier
) : TaskListRepository {

    override suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList> =
        withContext(dispatcher) {
            try {
                val request = TaskListMapper.toProto(params)
                val response = networkDataSource.createTaskList(request)
                val taskList = TaskListMapper.toDomain(response)
                directoryChangeNotifier.notifyChanged(normalizePath(params.path))
                Result.success(taskList)
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

    override suspend fun listTaskLists(parentDir: String): Result<List<TaskListEntry>> =
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
                val taskList = TaskListMapper.toDomain(response)
                directoryChangeNotifier.notifyChanged(
                    normalizePath(taskList.filePath.substringBeforeLast('/', ""))
                )
                Result.success(taskList)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteTaskList(taskListId: String): Result<Unit> =
        withContext(dispatcher) {
            try {
                val taskListResponse = networkDataSource.getTaskList(GetTaskListRequest(id = taskListId))
                val filePath = TaskListMapper.toDomain(taskListResponse).filePath
                val request = DeleteTaskListRequest(id = taskListId)
                networkDataSource.deleteTaskList(request)
                directoryChangeNotifier.notifyChanged(
                    normalizePath(filePath.substringBeforeLast('/', ""))
                )
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
