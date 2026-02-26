package net.onefivefour.echolist.data.repository

import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.Folder
import net.onefivefour.echolist.data.models.UpdateFolderParams

interface FolderRepository {
    suspend fun createFolder(params: CreateFolderParams): Result<Folder>
    suspend fun getFolder(folderPath: String): Result<Folder>
    suspend fun listFolders(parentPath: String): Result<List<Folder>>
    suspend fun updateFolder(params: UpdateFolderParams): Result<Folder>
    suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit>
}
