package net.onefivefour.echolist.data.repository

import folder.v1.GetFolderRequest
import folder.v1.ListFoldersRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.onefivefour.echolist.data.mapper.FolderMapper
import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.Folder
import net.onefivefour.echolist.data.models.UpdateFolderParams
import net.onefivefour.echolist.data.source.network.FolderRemoteDataSource

internal class FolderRepositoryImpl(
    private val networkDataSource: FolderRemoteDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : FolderRepository {

    override suspend fun createFolder(params: CreateFolderParams): Result<Folder> =
        withContext(dispatcher) {
            try {
                val request = FolderMapper.toProto(params)
                val response = networkDataSource.createFolder(request)
                Result.success(FolderMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getFolder(folderPath: String): Result<Folder> =
        withContext(dispatcher) {
            try {
                val request = GetFolderRequest(folder_path = folderPath)
                val response = networkDataSource.getFolder(request)
                Result.success(FolderMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun listFolders(parentPath: String): Result<List<Folder>> =
        withContext(dispatcher) {
            try {
                val request = ListFoldersRequest(parent_path = parentPath)
                val response = networkDataSource.listFolders(request)
                Result.success(FolderMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> =
        withContext(dispatcher) {
            try {
                val request = FolderMapper.toProto(params)
                val response = networkDataSource.updateFolder(request)
                Result.success(FolderMapper.toDomain(response))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> =
        withContext(dispatcher) {
            try {
                val request = FolderMapper.toProto(params)
                networkDataSource.deleteFolder(request)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
