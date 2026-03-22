package net.onefivefour.echolist.data.repository

import `file`.v1.ListFilesRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import net.onefivefour.echolist.data.mapper.FileMapper
import net.onefivefour.echolist.data.dto.CreateFolderParams
import net.onefivefour.echolist.data.dto.DeleteFolderParams
import net.onefivefour.echolist.data.models.FileEntry
import net.onefivefour.echolist.domain.model.Folder
import net.onefivefour.echolist.data.dto.UpdateFolderParams
import net.onefivefour.echolist.data.source.network.FileRemoteDataSource
import net.onefivefour.echolist.domain.repository.FileRepository

internal class FileRepositoryImpl(
    private val networkDataSource: FileRemoteDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : FileRepository {

    private val _directoryChanged = MutableSharedFlow<String>()
    override val directoryChanged: SharedFlow<String> = _directoryChanged.asSharedFlow()

    override suspend fun createFolder(params: CreateFolderParams): Result<Folder> =
        withContext(dispatcher) {
            try {
                val request = FileMapper.toProto(params)
                val response = networkDataSource.createFolder(request)
                val folder = FileMapper.toDomain(response)
                _directoryChanged.emit(params.parentDir)
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
                _directoryChanged.emit(parentDir)
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
                _directoryChanged.emit(parentDir)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
