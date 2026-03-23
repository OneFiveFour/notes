package net.onefivefour.echolist.data.repository

import `file`.v1.ListFilesRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.onefivefour.echolist.data.mapper.FileMapper
import net.onefivefour.echolist.data.dto.CreateFolderParams
import net.onefivefour.echolist.data.dto.DeleteFolderParams
import net.onefivefour.echolist.data.models.FileEntry
import net.onefivefour.echolist.domain.model.Folder
import net.onefivefour.echolist.data.dto.UpdateFolderParams
import net.onefivefour.echolist.data.source.network.FileRemoteDataSource
import net.onefivefour.echolist.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.domain.repository.FileRepository

internal class FileRepositoryImpl(
    private val networkDataSource: FileRemoteDataSource,
    private val directoryChangeNotifier: DirectoryChangeNotifier,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : FileRepository {

    override suspend fun createFolder(params: CreateFolderParams): Result<Folder> =
        withContext(dispatcher) {
            try {
                val request = FileMapper.toProto(params)
                val response = networkDataSource.createFolder(request)
                val folder = FileMapper.toDomain(response)
                directoryChangeNotifier.notifyChanged(params.parentDir)
                Result.success(folder)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun listFiles(parentPath: String): Result<List<FileEntry>> =
        withContext(dispatcher) {
            try {
                val request = ListFilesRequest(parent_dir = parentPath)
                val response = networkDataSource.listFiles(request)
                Result.success(FileMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> =
        withContext(dispatcher) {
            try {
                val request = FileMapper.toProto(params)
                val response = networkDataSource.updateFolder(request)
                val folder = FileMapper.toDomain(response)
                val parentDir = params.folderPath.substringBeforeLast('/', "")
                directoryChangeNotifier.notifyChanged(parentDir)
                Result.success(folder)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> =
        withContext(dispatcher) {
            try {
                val request = FileMapper.toProto(params)
                networkDataSource.deleteFolder(request)
                val parentDir = params.folderPath.substringBeforeLast('/', "")
                directoryChangeNotifier.notifyChanged(parentDir)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
