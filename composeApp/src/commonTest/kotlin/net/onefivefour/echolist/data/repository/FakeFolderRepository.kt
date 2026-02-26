package net.onefivefour.echolist.data.repository

import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.Folder
import net.onefivefour.echolist.data.models.UpdateFolderParams

/**
 * Fake [FolderRepository] for testing. Supports pre-configured results
 * and tracks all method calls for verification.
 */
open class FakeFolderRepository : FolderRepository {

    var createFolderResult: Result<Folder> = Result.success(Folder(path = "", name = ""))
    var getFolderResult: Result<Folder> = Result.success(Folder(path = "", name = ""))
    var listFoldersResult: Result<List<Folder>> = Result.success(emptyList())
    var updateFolderResult: Result<Folder> = Result.success(Folder(path = "", name = ""))
    var deleteFolderResult: Result<Unit> = Result.success(Unit)

    /** All method invocations recorded for verification. */
    val callLog = mutableListOf<String>()

    /** The last [CreateFolderParams] passed to [createFolder]. */
    var lastCreateParams: CreateFolderParams? = null
        private set

    override suspend fun createFolder(params: CreateFolderParams): Result<Folder> {
        callLog.add("createFolder(${params.parentPath}, ${params.name})")
        lastCreateParams = params
        return createFolderResult
    }

    override suspend fun getFolder(folderPath: String): Result<Folder> {
        callLog.add("getFolder($folderPath)")
        return getFolderResult
    }

    override suspend fun listFolders(parentPath: String): Result<List<Folder>> {
        callLog.add("listFolders($parentPath)")
        return listFoldersResult
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
