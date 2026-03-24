package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.data.models.FileEntry
import net.onefivefour.echolist.data.models.FileMetadata
import net.onefivefour.echolist.data.models.ItemType
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun FileOverview(
    fileEntries: List<FileEntry>,
    isLoading: Boolean,
    error: String?,
    onFolderClick: (path: String) -> Unit,
    onNoteClick: (noteId: String) -> Unit,
    onTaskClick: (taskListId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
    ) {
        when {
            isLoading -> item(key = "loading") { LoadingIndicator() }
            error != null -> item(key = "error") { ErrorContainer(message = error) }
            else -> items(fileEntries, key = { it.path }) { entry ->
                when (entry.itemType) {
                    ItemType.FOLDER -> FolderItem(
                        title = entry.title,
                        metadata = entry.metadata as? FileMetadata.Folder,
                        onClick = { onFolderClick(entry.path) }
                    )

                    ItemType.NOTE -> {
                        (entry.metadata as? FileMetadata.Note)?.let { noteMetadata ->
                            NoteItem(
                                id = noteMetadata.id,
                                title = entry.title,
                                preview = noteMetadata.preview,
                                onClick = onNoteClick
                            )
                        }
                    }

                    ItemType.TASK_LIST -> {
                        (entry.metadata as? FileMetadata.TaskList)?.let { taskListMetadata ->
                            TaskItem(
                                id = taskListMetadata.id,
                                title = entry.title,
                                doneTaskCount = taskListMetadata.doneTaskCount,
                                totalTaskCount = taskListMetadata.totalTaskCount,
                                onClick = onTaskClick
                            )
                        }
                    }

                    ItemType.UNSPECIFIED -> { /* skip */
                    }
                }
            }
        }
    }
}