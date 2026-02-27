package net.onefivefour.echolist.data.source.network

import `file`.v1.CreateFolderRequest
import `file`.v1.CreateFolderResponse
import `file`.v1.DeleteFolderRequest
import `file`.v1.DeleteFolderResponse
import `file`.v1.ListFilesRequest
import `file`.v1.ListFilesResponse
import `file`.v1.UpdateFolderRequest
import `file`.v1.UpdateFolderResponse

internal interface FileRemoteDataSource {
    suspend fun createFolder(request: CreateFolderRequest): CreateFolderResponse
    suspend fun listFiles(request: ListFilesRequest): ListFilesResponse
    suspend fun updateFolder(request: UpdateFolderRequest): UpdateFolderResponse
    suspend fun deleteFolder(request: DeleteFolderRequest): DeleteFolderResponse
}
