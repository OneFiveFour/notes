package net.onefivefour.echolist.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.onefivefour.echolist.data.mapper.FolderMapper
import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.Folder
import net.onefivefour.echolist.data.models.RenameFolderParams
import net.onefivefour.echolist.data.source.network.FolderRemoteDataSource

internal class FolderRepositoryImpl(
    private val networkDataSource: FolderRemoteDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : FolderRepository {

    override suspend fun createFolder(params: CreateFolderParams): Result<List<Folder>> =
        withContext(dispatcher) {
            try {
                val request = FolderMapper.toProto(params)
                val response = networkDataSource.createFolder(request)
                Result.success(FolderMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun renameFolder(params: RenameFolderParams): Result<List<Folder>> =
        withContext(dispatcher) {
            try {
                val request = FolderMapper.toProto(params)
                val response = networkDataSource.renameFolder(request)
                Result.success(FolderMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteFolder(params: DeleteFolderParams): Result<List<Folder>> =
        withContext(dispatcher) {
            try {
                val request = FolderMapper.toProto(params)
                val response = networkDataSource.deleteFolder(request)
                Result.success(FolderMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
