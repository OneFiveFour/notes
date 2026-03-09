package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.data.models.FileEntry
import net.onefivefour.echolist.data.models.FileMetadata
import net.onefivefour.echolist.data.models.ItemType
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun FileOverview(
    title: String,
    fileEntries: List<FileEntry>,
    isLoading: Boolean,
    error: String?,
    onFolderClick: (path: String) -> Unit,
    onNoteClick: (path: String) -> Unit,
    onTaskClick: (path: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            color = EchoListTheme.materialColors.primary,
            style = EchoListTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.m))

        when {
            isLoading -> LoadingIndicator()
            error != null -> ErrorContainer(message = error)
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
            ) {
                items(fileEntries, key = { it.path }) { entry ->
                    when (entry.itemType) {
                        ItemType.FOLDER -> FolderItem(
                            title = entry.title,
                            metadata = entry.metadata as? FileMetadata.Folder,
                            onClick = { onFolderClick(entry.path) }
                        )
                        ItemType.NOTE -> NoteItem(
                            title = entry.title,
                            metadata = entry.metadata as? FileMetadata.Note,
                            onClick = { onNoteClick(entry.path) }
                        )
                        ItemType.TASK_LIST -> TaskItem(
                            title = entry.title,
                            metadata = entry.metadata as? FileMetadata.TaskList,
                            onClick = { onTaskClick(entry.path) }
                        )
                        ItemType.UNSPECIFIED -> { /* skip */ }
                    }
                }
            }
        }
    }
}