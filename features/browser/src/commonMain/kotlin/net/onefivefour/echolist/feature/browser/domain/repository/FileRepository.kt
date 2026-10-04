package net.onefivefour.echolist.feature.browser.domain.repository

import net.onefivefour.echolist.feature.browser.domain.model.CreateFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.DeleteFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.Folder
import net.onefivefour.echolist.feature.browser.domain.model.UpdateFolderParams

internal interface FileRepository {
    suspend fun createFolder(params: CreateFolderParams): Result<Folder>
    suspend fun listFiles(parentDir: String): Result<List<FileEntry>>
    suspend fun updateFolder(params: UpdateFolderParams): Result<Folder>
    suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit>
}
