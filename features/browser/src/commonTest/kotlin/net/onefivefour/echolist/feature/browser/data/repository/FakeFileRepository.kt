package net.onefivefour.echolist.feature.browser.data.repository

import net.onefivefour.echolist.feature.browser.domain.model.CreateFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.DeleteFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.Folder
import net.onefivefour.echolist.feature.browser.domain.model.UpdateFolderParams
import net.onefivefour.echolist.feature.browser.domain.repository.FileRepository

/**
 * Fake [FileRepository] for testing. Supports pre-configured results
 * and tracks all method calls for verification.
 */
internal open class FakeFileRepository : FileRepository {

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

    override suspend fun listFiles(parentDir: String): Result<List<FileEntry>> {
        callLog.add("listFiles($parentDir)")
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
