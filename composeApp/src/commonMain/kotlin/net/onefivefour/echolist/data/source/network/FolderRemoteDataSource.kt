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

internal interface FolderRemoteDataSource {
    suspend fun createFolder(request: CreateFolderRequest): CreateFolderResponse
    suspend fun getFolder(request: GetFolderRequest): GetFolderResponse
    suspend fun listFolders(request: ListFoldersRequest): ListFoldersResponse
    suspend fun updateFolder(request: UpdateFolderRequest): UpdateFolderResponse
    suspend fun deleteFolder(request: DeleteFolderRequest): DeleteFolderResponse
}
