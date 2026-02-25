package net.onefivefour.echolist.data.repository

import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.Folder
import net.onefivefour.echolist.data.models.RenameFolderParams

/**
 * Fake [FolderRepository] for testing. Supports pre-configured results
 * and tracks all method calls for verification.
 */
open class FakeFolderRepository : FolderRepository {

    var createFolderResult: Result<List<Folder>> = Result.success(emptyList())
    var renameFolderResult: Result<List<Folder>> = Result.success(emptyList())
    var deleteFolderResult: Result<List<Folder>> = Result.success(emptyList())

    /** All method invocations recorded for verification. */
    val callLog = mutableListOf<String>()

    /** The last [CreateFolderParams] passed to [createFolder]. */
    var lastCreateParams: CreateFolderParams? = null
        private set

    override suspend fun createFolder(params: CreateFolderParams): Result<List<Folder>> {
        callLog.add("createFolder(${params.domain}, ${params.parentPath}, ${params.name})")
        lastCreateParams = params
        return createFolderResult
    }

    override suspend fun renameFolder(params: RenameFolderParams): Result<List<Folder>> {
        callLog.add("renameFolder(${params.domain}, ${params.folderPath}, ${params.newName})")
        return renameFolderResult
    }

    override suspend fun deleteFolder(params: DeleteFolderParams): Result<List<Folder>> {
        callLog.add("deleteFolder(${params.domain}, ${params.folderPath})")
        return deleteFolderResult
    }
}
