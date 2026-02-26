package net.onefivefour.echolist.data.source.network

import net.onefivefour.echolist.network.client.ConnectRpcClient
import tasks.v1.CreateTaskListRequest
import tasks.v1.CreateTaskListResponse
import tasks.v1.DeleteTaskListRequest
import tasks.v1.DeleteTaskListResponse
import tasks.v1.GetTaskListRequest
import tasks.v1.GetTaskListResponse
import tasks.v1.ListTaskListsRequest
import tasks.v1.ListTaskListsResponse
import tasks.v1.UpdateTaskListRequest
import tasks.v1.UpdateTaskListResponse

internal class TaskListRemoteDataSourceImpl(
    private val client: ConnectRpcClient
) : TaskListRemoteDataSource {

    override suspend fun createTaskList(request: CreateTaskListRequest): CreateTaskListResponse {
        return client.call(
            path = "/tasks.v1.TaskListService/CreateTaskList",
            request = request,
            requestSerializer = { CreateTaskListRequest.ADAPTER.encode(it) },
            responseDeserializer = { CreateTaskListResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun getTaskList(request: GetTaskListRequest): GetTaskListResponse {
        return client.call(
            path = "/tasks.v1.TaskListService/GetTaskList",
            request = request,
            requestSerializer = { GetTaskListRequest.ADAPTER.encode(it) },
            responseDeserializer = { GetTaskListResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun listTaskLists(request: ListTaskListsRequest): ListTaskListsResponse {
        return client.call(
            path = "/tasks.v1.TaskListService/ListTaskLists",
            request = request,
            requestSerializer = { ListTaskListsRequest.ADAPTER.encode(it) },
            responseDeserializer = { ListTaskListsResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun updateTaskList(request: UpdateTaskListRequest): UpdateTaskListResponse {
        return client.call(
            path = "/tasks.v1.TaskListService/UpdateTaskList",
            request = request,
            requestSerializer = { UpdateTaskListRequest.ADAPTER.encode(it) },
            responseDeserializer = { UpdateTaskListResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun deleteTaskList(request: DeleteTaskListRequest): DeleteTaskListResponse {
        return client.call(
            path = "/tasks.v1.TaskListService/DeleteTaskList",
            request = request,
            requestSerializer = { DeleteTaskListRequest.ADAPTER.encode(it) },
            responseDeserializer = { DeleteTaskListResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }
}
