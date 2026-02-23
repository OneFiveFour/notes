package net.onefivefour.echolist.data.repository

import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.DirectoryEntry
import net.onefivefour.echolist.data.models.RenameFolderParams

interface FolderRepository {
    suspend fun createFolder(params: CreateFolderParams): Result<List<DirectoryEntry>>
    suspend fun renameFolder(params: RenameFolderParams): Result<List<DirectoryEntry>>
    suspend fun deleteFolder(params: DeleteFolderParams): Result<List<DirectoryEntry>>
}
