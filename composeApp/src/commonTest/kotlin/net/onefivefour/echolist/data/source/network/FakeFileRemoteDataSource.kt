package net.onefivefour.echolist.data.source.network

import `file`.v1.CreateFolderRequest
import `file`.v1.CreateFolderResponse
import `file`.v1.DeleteFolderRequest
import `file`.v1.DeleteFolderResponse
import `file`.v1.ListFilesRequest
import `file`.v1.ListFilesResponse
import `file`.v1.UpdateFolderRequest
import `file`.v1.UpdateFolderResponse

internal class FakeFileRemoteDataSource : FileRemoteDataSource {

    var createFolderResult: Result<CreateFolderResponse> = Result.success(CreateFolderResponse())
    var listFilesResult: Result<ListFilesResponse> = Result.success(ListFilesResponse())
    var updateFolderResult: Result<UpdateFolderResponse> = Result.success(UpdateFolderResponse())
    var deleteFolderResult: Result<DeleteFolderResponse> = Result.success(DeleteFolderResponse())

    var lastCreateRequest: CreateFolderRequest? = null
    var lastListRequest: ListFilesRequest? = null
    var lastUpdateRequest: UpdateFolderRequest? = null
    var lastDeleteRequest: DeleteFolderRequest? = null

    override suspend fun createFolder(request: CreateFolderRequest): CreateFolderResponse {
        lastCreateRequest = request
        return createFolderResult.getOrThrow()
    }

    override suspend fun listFiles(request: ListFilesRequest): ListFilesResponse {
        lastListRequest = request
        return listFilesResult.getOrThrow()
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
