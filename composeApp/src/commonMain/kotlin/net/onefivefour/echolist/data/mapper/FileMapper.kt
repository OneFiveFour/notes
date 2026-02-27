package net.onefivefour.echolist.data.mapper

import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.Folder
import net.onefivefour.echolist.data.models.UpdateFolderParams
import `file`.v1.CreateFolderRequest
import `file`.v1.CreateFolderResponse
import `file`.v1.DeleteFolderRequest
import `file`.v1.ListFilesResponse
import `file`.v1.UpdateFolderRequest
import `file`.v1.UpdateFolderResponse

/**
 * Maps between Wire-generated file.v1 proto models and domain models.
 */
internal object FileMapper {

    // Proto -> Domain

    fun toDomain(proto: `file`.v1.Folder): Folder = Folder(
        path = proto.path,
        name = proto.name
    )

    fun toDomain(proto: CreateFolderResponse): Folder =
        toDomain(proto.folder!!)

    fun toDomain(proto: ListFilesResponse): List<String> =
        proto.entries

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
