package net.onefivefour.echolist.data.mapper

import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.Folder
import net.onefivefour.echolist.data.models.UpdateFolderParams
import folder.v1.CreateFolderRequest
import folder.v1.CreateFolderResponse
import folder.v1.DeleteFolderRequest
import folder.v1.GetFolderResponse
import folder.v1.ListFoldersResponse
import folder.v1.UpdateFolderRequest
import folder.v1.UpdateFolderResponse

/**
 * Maps between Wire-generated folder proto models and domain models.
 */
internal object FolderMapper {

    // Proto -> Domain

    fun toDomain(proto: folder.v1.Folder): Folder = Folder(
        path = proto.path,
        name = proto.name
    )

    fun toDomain(proto: CreateFolderResponse): Folder =
        toDomain(proto.folder!!)

    fun toDomain(proto: GetFolderResponse): Folder =
        toDomain(proto.folder!!)

    fun toDomain(proto: ListFoldersResponse): List<Folder> =
        proto.folders.map { toDomain(it) }

    fun toDomain(proto: UpdateFolderResponse): Folder =
        toDomain(proto.folder!!)

    // Domain -> Proto

    fun toProto(params: CreateFolderParams): CreateFolderRequest = CreateFolderRequest(
        parent_path = params.parentPath,
        name = params.name
    )

    fun toProto(params: UpdateFolderParams): UpdateFolderRequest = UpdateFolderRequest(
        folder_path = params.folderPath,
        new_name = params.newName
    )

    fun toProto(params: DeleteFolderParams): DeleteFolderRequest = DeleteFolderRequest(
        folder_path = params.folderPath
    )
}
