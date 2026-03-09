package net.onefivefour.echolist.data.models

data class FileEntry(
    val path: String,
    val title: String,
    val itemType: ItemType,
    val metadata: FileMetadata?
)

enum class ItemType {
    UNSPECIFIED,
    FOLDER,
    NOTE,
    TASK_LIST
}

sealed interface FileMetadata {
    data class Folder(val childCount: Int) : FileMetadata
    data class Note(val updatedAt: Long, val preview: String) : FileMetadata
    data class TaskList(
        val updatedAt: Long,
        val totalTaskCount: Int,
        val doneTaskCount: Int
    ) : FileMetadata
}
