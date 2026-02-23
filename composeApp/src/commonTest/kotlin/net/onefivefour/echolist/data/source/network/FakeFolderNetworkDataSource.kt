package net.onefivefour.echolist.data.source.network

import folder.v1.CreateFolderRequest
import folder.v1.CreateFolderResponse
import folder.v1.DeleteFolderRequest
import folder.v1.DeleteFolderResponse
import folder.v1.RenameFolderRequest
import folder.v1.RenameFolderResponse

internal class FakeFolderNetworkDataSource : FolderNetworkDataSource {

    var createFolderResult: Result<CreateFolderResponse> = Result.success(CreateFolderResponse())
    var renameFolderResult: Result<RenameFolderResponse> = Result.success(RenameFolderResponse())
    var deleteFolderResult: Result<DeleteFolderResponse> = Result.success(DeleteFolderResponse())

    var lastCreateRequest: CreateFolderRequest? = null
    var lastRenameRequest: RenameFolderRequest? = null
    var lastDeleteRequest: DeleteFolderRequest? = null

    override suspend fun createFolder(request: CreateFolderRequest): CreateFolderResponse {
        lastCreateRequest = request
        return createFolderResult.getOrThrow()
    }

    override suspend fun renameFolder(request: RenameFolderRequest): RenameFolderResponse {
        lastRenameRequest = request
        return renameFolderResult.getOrThrow()
    }

    override suspend fun deleteFolder(request: DeleteFolderRequest): DeleteFolderResponse {
        lastDeleteRequest = request
        return deleteFolderResult.getOrThrow()
    }
}
