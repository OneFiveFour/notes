package net.onefivefour.echolist.data.mapper

import net.onefivefour.echolist.data.dto.CreateFolderParams
import net.onefivefour.echolist.data.dto.DeleteFolderParams
import net.onefivefour.echolist.data.models.FileEntry
import net.onefivefour.echolist.data.models.FileMetadata
import net.onefivefour.echolist.domain.model.Folder
import net.onefivefour.echolist.data.models.ItemType
import net.onefivefour.echolist.data.dto.UpdateFolderParams
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

    fun toDomain(proto: ListFilesResponse): List<FileEntry> =
        proto.entries.map { toDomain(it) }

    fun toDomain(proto: `file`.v1.FileEntry): FileEntry = FileEntry(
        path = proto.path,
        title = proto.title,
        itemType = toDomain(proto.item_type),
        metadata = when {
            proto.folder_metadata != null -> FileMetadata.Folder(
                childCount = proto.folder_metadata.child_count
            )
            proto.note_metadata != null -> FileMetadata.Note(
                id = proto.note_metadata.id,
                updatedAt = proto.note_metadata.updated_at,
                preview = proto.note_metadata.preview
            )
            proto.task_list_metadata != null -> FileMetadata.TaskList(
                id = proto.task_list_metadata.id,
                updatedAt = proto.task_list_metadata.updated_at,
                totalTaskCount = proto.task_list_metadata.total_task_count,
                doneTaskCount = proto.task_list_metadata.done_task_count
            )
            else -> null
        }
    )

    fun toDomain(proto: `file`.v1.ItemType): ItemType = when (proto) {
        `file`.v1.ItemType.ITEM_TYPE_FOLDER -> ItemType.FOLDER
        `file`.v1.ItemType.ITEM_TYPE_NOTE -> ItemType.NOTE
        `file`.v1.ItemType.ITEM_TYPE_TASK_LIST -> ItemType.TASK_LIST
        `file`.v1.ItemType.ITEM_TYPE_UNSPECIFIED -> ItemType.UNSPECIFIED
    }

    fun toDomain(proto: UpdateFolderResponse): Folder =
        toDomain(proto.folder!!)

    // Domain -> Proto

    fun toProto(params: CreateFolderParams): CreateFolderRequest = CreateFolderRequest(
        parent_dir = params.parentDir,
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
