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

internal class FakeFolderRemoteDataSource : FolderRemoteDataSource {

    var createFolderResult: Result<CreateFolderResponse> = Result.success(CreateFolderResponse())
    var getFolderResult: Result<GetFolderResponse> = Result.success(GetFolderResponse())
    var listFoldersResult: Result<ListFoldersResponse> = Result.success(ListFoldersResponse())
    var updateFolderResult: Result<UpdateFolderResponse> = Result.success(UpdateFolderResponse())
    var deleteFolderResult: Result<DeleteFolderResponse> = Result.success(DeleteFolderResponse())

    var lastCreateRequest: CreateFolderRequest? = null
    var lastGetRequest: GetFolderRequest? = null
    var lastListRequest: ListFoldersRequest? = null
    var lastUpdateRequest: UpdateFolderRequest? = null
    var lastDeleteRequest: DeleteFolderRequest? = null

    override suspend fun createFolder(request: CreateFolderRequest): CreateFolderResponse {
        lastCreateRequest = request
        return createFolderResult.getOrThrow()
    }

    override suspend fun getFolder(request: GetFolderRequest): GetFolderResponse {
        lastGetRequest = request
        return getFolderResult.getOrThrow()
    }

    override suspend fun listFolders(request: ListFoldersRequest): ListFoldersResponse {
        lastListRequest = request
        return listFoldersResult.getOrThrow()
    }

    override suspend fun updateFolder(request: UpdateFolderRequest): UpdateFolderResponse {
        lastUpdateRequest = request
        return updateFolderResult.getOrThrow()
    }

    override suspend fun deleteFolder(request: DeleteFolderRequest): DeleteFolderResponse {
        lastDeleteRequest = request
        return deleteFolderResult.getOrThrow()
    }
}
