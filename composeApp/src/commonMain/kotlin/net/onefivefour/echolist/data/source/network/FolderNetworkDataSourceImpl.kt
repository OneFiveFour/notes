package net.onefivefour.echolist.data.source.network

import folder.v1.CreateFolderRequest
import folder.v1.CreateFolderResponse
import folder.v1.DeleteFolderRequest
import folder.v1.DeleteFolderResponse
import folder.v1.RenameFolderRequest
import folder.v1.RenameFolderResponse
import net.onefivefour.echolist.network.client.ConnectRpcClient

internal class FolderNetworkDataSourceImpl(
    private val client: ConnectRpcClient
) : FolderNetworkDataSource {

    override suspend fun createFolder(request: CreateFolderRequest): CreateFolderResponse {
        return client.call(
            path = "/folder.v1.FolderService/CreateFolder",
            request = request,
            requestSerializer = { CreateFolderRequest.ADAPTER.encode(it) },
            responseDeserializer = { CreateFolderResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun renameFolder(request: RenameFolderRequest): RenameFolderResponse {
        return client.call(
            path = "/folder.v1.FolderService/RenameFolder",
            request = request,
            requestSerializer = { RenameFolderRequest.ADAPTER.encode(it) },
            responseDeserializer = { RenameFolderResponse.ADAPTER.decode(it) }
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
