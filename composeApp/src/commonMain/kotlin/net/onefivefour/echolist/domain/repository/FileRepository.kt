package net.onefivefour.echolist.domain.repository

import kotlinx.coroutines.flow.SharedFlow
import net.onefivefour.echolist.data.dto.CreateFolderParams
import net.onefivefour.echolist.data.dto.DeleteFolderParams
import net.onefivefour.echolist.data.models.FileEntry
import net.onefivefour.echolist.domain.model.Folder
import net.onefivefour.echolist.data.dto.UpdateFolderParams

interface FileRepository {
    val directoryChanged: SharedFlow<String>

    suspend fun createFolder(params: CreateFolderParams): Result<Folder>
    suspend fun listFiles(parentPath: String): Result<List<FileEntry>>
    suspend fun updateFolder(params: UpdateFolderParams): Result<Folder>
    suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit>
}
