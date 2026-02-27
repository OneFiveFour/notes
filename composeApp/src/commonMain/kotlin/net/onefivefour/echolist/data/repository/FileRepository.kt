package net.onefivefour.echolist.data.repository

import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.Folder
import net.onefivefour.echolist.data.models.UpdateFolderParams

interface FileRepository {
    suspend fun createFolder(params: CreateFolderParams): Result<Folder>
    suspend fun listFiles(parentPath: String): Result<List<String>>
    suspend fun updateFolder(params: UpdateFolderParams): Result<Folder>
    suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit>
}
