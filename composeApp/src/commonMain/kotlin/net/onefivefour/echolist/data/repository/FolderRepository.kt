package net.onefivefour.echolist.data.repository

import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.FolderEntry
import net.onefivefour.echolist.data.models.RenameFolderParams

interface FolderRepository {
    suspend fun createFolder(params: CreateFolderParams): Result<List<FolderEntry>>
    suspend fun renameFolder(params: RenameFolderParams): Result<List<FolderEntry>>
    suspend fun deleteFolder(params: DeleteFolderParams): Result<List<FolderEntry>>
}
