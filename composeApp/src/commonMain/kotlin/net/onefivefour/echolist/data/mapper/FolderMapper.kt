package net.onefivefour.echolist.data.mapper

import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.FolderEntry
import net.onefivefour.echolist.data.models.RenameFolderParams
import folder.v1.CreateFolderRequest
import folder.v1.CreateFolderResponse
import folder.v1.DeleteFolderRequest
import folder.v1.DeleteFolderResponse
import folder.v1.RenameFolderRequest
import folder.v1.RenameFolderResponse

/**
 * Maps between Wire-generated folder proto models and domain models.
 */
internal object FolderMapper {

    // Proto -> Domain

    fun toDomain(proto: folder.v1.FolderEntry): FolderEntry = FolderEntry(
        path = proto.path
    )

    fun toDomain(proto: CreateFolderResponse): List<FolderEntry> =
        proto.entries.map { toDomain(it) }

    fun toDomain(proto: RenameFolderResponse): List<FolderEntry> =
        proto.entries.map { toDomain(it) }

    fun toDomain(proto: DeleteFolderResponse): List<FolderEntry> =
        proto.entries.map { toDomain(it) }

    // Domain -> Proto

    fun toProto(params: CreateFolderParams): CreateFolderRequest = CreateFolderRequest(
        domain = params.domain,
        parent_path = params.parentPath,
        name = params.name
    )

    fun toProto(params: RenameFolderParams): RenameFolderRequest = RenameFolderRequest(
        domain = params.domain,
        folder_path = params.folderPath,
        new_name = params.newName
    )

    fun toProto(params: DeleteFolderParams): DeleteFolderRequest = DeleteFolderRequest(
        domain = params.domain,
        folder_path = params.folderPath
    )
}
