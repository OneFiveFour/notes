package net.onefivefour.echolist.data.repository

import net.onefivefour.echolist.data.dto.CreateFolderParams
import net.onefivefour.echolist.data.dto.DeleteFolderParams
import net.onefivefour.echolist.data.models.FileEntry
import net.onefivefour.echolist.domain.model.Folder
import net.onefivefour.echolist.data.dto.UpdateFolderParams
import net.onefivefour.echolist.domain.repository.FileRepository

/**
 * Fake [FileRepository] for testing. Supports pre-configured results
 * and tracks all method calls for verification.
 */
open class FakeFileRepository : FileRepository {

    var createFolderResult: Result<Folder> = Result.success(Folder(path = "", name = ""))
    var listFilesResult: Result<List<FileEntry>> = Result.success(emptyList())
    var updateFolderResult: Result<Folder> = Result.success(Folder(path = "", name = ""))
    var deleteFolderResult: Result<Unit> = Result.success(Unit)

    /** All method invocations recorded for verification. */
    val callLog = mutableListOf<String>()

    /** The last [CreateFolderParams] passed to [createFolder]. */
    var lastCreateParams: CreateFolderParams? = null
        private set

    override suspend fun createFolder(params: CreateFolderParams): Result<Folder> {
        callLog.add("createFolder(${params.parentDir}, ${params.name})")
        lastCreateParams = params
        return createFolderResult
    }

    override suspend fun listFiles(parentPath: String): Result<List<FileEntry>> {
        callLog.add("listFiles($parentPath)")
        return listFilesResult
    }

    override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> {
        callLog.add("updateFolder(${params.folderPath}, ${params.newName})")
        return updateFolderResult
    }

    override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> {
        callLog.add("deleteFolder(${params.folderPath})")
        return deleteFolderResult
    }
}
