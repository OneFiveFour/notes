package net.onefivefour.echolist.data.source.network

import folder.v1.CreateFolderRequest
import folder.v1.CreateFolderResponse
import folder.v1.DeleteFolderRequest
import folder.v1.DeleteFolderResponse
import folder.v1.RenameFolderRequest
import folder.v1.RenameFolderResponse

internal interface FolderRemoteDataSource {
    suspend fun createFolder(request: CreateFolderRequest): CreateFolderResponse
    suspend fun renameFolder(request: RenameFolderRequest): RenameFolderResponse
    suspend fun deleteFolder(request: DeleteFolderRequest): DeleteFolderResponse
}
