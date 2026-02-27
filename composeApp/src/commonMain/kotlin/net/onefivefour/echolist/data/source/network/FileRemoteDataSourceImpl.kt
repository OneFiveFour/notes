package net.onefivefour.echolist.data.source.network

import `file`.v1.CreateFolderRequest
import `file`.v1.CreateFolderResponse
import `file`.v1.DeleteFolderRequest
import `file`.v1.DeleteFolderResponse
import `file`.v1.ListFilesRequest
import `file`.v1.ListFilesResponse
import `file`.v1.UpdateFolderRequest
import `file`.v1.UpdateFolderResponse
import net.onefivefour.echolist.network.client.ConnectRpcClient

internal class FileRemoteDataSourceImpl(
    private val client: ConnectRpcClient
) : FileRemoteDataSource {

    override suspend fun createFolder(request: CreateFolderRequest): CreateFolderResponse {
        return client.call(
            path = "/file.v1.FileService/CreateFolder",
            request = request,
            requestSerializer = { CreateFolderRequest.ADAPTER.encode(it) },
            responseDeserializer = { CreateFolderResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun listFiles(request: ListFilesRequest): ListFilesResponse {
        return client.call(
            path = "/file.v1.FileService/ListFiles",
            request = request,
            requestSerializer = { ListFilesRequest.ADAPTER.encode(it) },
            responseDeserializer = { ListFilesResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun updateFolder(request: UpdateFolderRequest): UpdateFolderResponse {
        return client.call(
            path = "/file.v1.FileService/UpdateFolder",
            request = request,
            requestSerializer = { UpdateFolderRequest.ADAPTER.encode(it) },
            responseDeserializer = { UpdateFolderResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun deleteFolder(request: DeleteFolderRequest): DeleteFolderResponse {
        return client.call(
            path = "/file.v1.FileService/DeleteFolder",
            request = request,
            requestSerializer = { DeleteFolderRequest.ADAPTER.encode(it) },
            responseDeserializer = { DeleteFolderResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }
}
