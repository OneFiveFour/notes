package net.onefivefour.echolist.data.source.network

import folder.v1.CreateFolderRequest
import folder.v1.CreateFolderResponse
import folder.v1.DeleteFolderRequest
import folder.v1.DeleteFolderResponse
import folder.v1.GetFolderRequest
import folder.v1.GetFolderResponse
import folder.v1.ListFoldersRequest
import folder.v1.ListFoldersResponse
import folder.v1.UpdateFolderRequest
import folder.v1.UpdateFolderResponse
import net.onefivefour.echolist.network.client.ConnectRpcClient

internal class FolderRemoteDataSourceImpl(
    private val client: ConnectRpcClient
) : FolderRemoteDataSource {

    override suspend fun createFolder(request: CreateFolderRequest): CreateFolderResponse {
        return client.call(
            path = "/folder.v1.FolderService/CreateFolder",
            request = request,
            requestSerializer = { CreateFolderRequest.ADAPTER.encode(it) },
            responseDeserializer = { CreateFolderResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun getFolder(request: GetFolderRequest): GetFolderResponse {
        return client.call(
            path = "/folder.v1.FolderService/GetFolder",
            request = request,
            requestSerializer = { GetFolderRequest.ADAPTER.encode(it) },
            responseDeserializer = { GetFolderResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun listFolders(request: ListFoldersRequest): ListFoldersResponse {
        return client.call(
            path = "/folder.v1.FolderService/ListFolders",
            request = request,
            requestSerializer = { ListFoldersRequest.ADAPTER.encode(it) },
            responseDeserializer = { ListFoldersResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun updateFolder(request: UpdateFolderRequest): UpdateFolderResponse {
        return client.call(
            path = "/folder.v1.FolderService/UpdateFolder",
            request = request,
            requestSerializer = { UpdateFolderRequest.ADAPTER.encode(it) },
            responseDeserializer = { UpdateFolderResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun deleteFolder(request: DeleteFolderRequest): DeleteFolderResponse {
        return client.call(
            path = "/folder.v1.FolderService/DeleteFolder",
            request = request,
            requestSerializer = { DeleteFolderRequest.ADAPTER.encode(it) },
            responseDeserializer = { DeleteFolderResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }
}
