package net.onefivefour.echolist.data.repository

import `file`.v1.ListFilesRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.onefivefour.echolist.data.mapper.FileMapper
import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.Folder
import net.onefivefour.echolist.data.models.UpdateFolderParams
import net.onefivefour.echolist.data.source.network.FileRemoteDataSource

internal class FileRepositoryImpl(
    private val networkDataSource: FileRemoteDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : FileRepository {

    override suspend fun createFolder(params: CreateFolderParams): Result<Folder> =
        withContext(dispatcher) {
            try {
                val request = FileMapper.toProto(params)
                val response = networkDataSource.createFolder(request)
                Result.success(FileMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun listFiles(parentPath: String): Result<List<String>> =
        withContext(dispatcher) {
            try {
                val request = ListFilesRequest(parent_path = parentPath)
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
                Result.success(FileMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> =
        withContext(dispatcher) {
            try {
                val request = FileMapper.toProto(params)
                networkDataSource.deleteFolder(request)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
