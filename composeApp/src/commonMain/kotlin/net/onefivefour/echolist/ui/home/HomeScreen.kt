package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.ui.theme.EchoListTheme

/**
 * Sealed type representing the different cell types in the folder grid.
 */
internal sealed interface FolderGridCell {
    data class Folder(val folder: FolderUiModel) : FolderGridCell
    data object AddButton : FolderGridCell
    data class InlineEditor(val state: InlineCreationState) : FolderGridCell
}

/**
 * Builds the list of grid cells for the folder section.
 * All folders come first, followed by either the AddButton or InlineEditor.
 */
internal fun buildFolderGridItems(
    folders: List<FolderUiModel>,
    inlineCreationState: InlineCreationState
): List<FolderGridCell> = buildList {
    folders.forEach { folder ->
        add(FolderGridCell.Folder(folder))
    }
    when (inlineCreationState) {
        is InlineCreationState.Hidden -> add(FolderGridCell.AddButton)
        is InlineCreationState.Editing -> add(FolderGridCell.InlineEditor(inlineCreationState))
        is InlineCreationState.Saving -> add(FolderGridCell.InlineEditor(inlineCreationState))
        is InlineCreationState.Error -> add(FolderGridCell.InlineEditor(inlineCreationState))
    }
}

/**
 * Sealed type representing the different cell types in the file grid.
 */
internal sealed interface FileGridCell {
    data class File(val file: FileUiModel) : FileGridCell
    data object AddButton : FileGridCell
}

/**
 * Builds the list of grid cells for the file section.
 * All files come first, followed by the AddButton.
 */
internal fun buildFileGridItems(files: List<FileUiModel>): List<FileGridCell> = buildList {
    files.forEach { file -> add(FileGridCell.File(file)) }
    add(FileGridCell.AddButton)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeScreenUiState,
    onNavigationClick: (() -> Unit)?,
    onBreadcrumbClick: (path: String) -> Unit,
    onFolderClick: (folderId: String) -> Unit,
    onFileClick: (fileId: String) -> Unit,
    onAddFolderClick: () -> Unit,
    onInlineNameChanged: (String) -> Unit,
    onInlineConfirm: () -> Unit,
    onInlineCancel: () -> Unit,
    onAddNoteClick: () -> Unit,
    onAddTasklistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EchoListTheme.materialColors.background)
            .padding(horizontal = EchoListTheme.dimensions.l)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {

        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = EchoListTheme.materialColors.background
            ),
            title = {
                Text(
                    text = uiState.title,
                    style = EchoListTheme.typography.titleLarge,
                    color = EchoListTheme.materialColors.primary
                )
            },
            navigationIcon = {
                if (onNavigationClick != null) {
                    IconButton(onClick = onNavigationClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = EchoListTheme.materialColors.primary
                        )
                    }
                }
            }
        )

        BreadcrumbNav(
            breadcrumbs = uiState.breadcrumbs,
            onBreadcrumbClick = onBreadcrumbClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.l))

        // FOLDERS section
        Text(
            text = "FOLDERS",
            style = MaterialTheme.typography.labelSmall,
            color = EchoListTheme.materialColors.primary
        )
        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.s))

        // Build grid items: all folders + trailing AddButton or InlineEditor
        val gridItems = buildFolderGridItems(uiState.folders, uiState.inlineCreationState)

        val gridRows = gridItems.chunked(2)
        gridRows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
            ) {
                row.forEach { cell ->
                    when (cell) {
                        is FolderGridCell.Folder -> {
                            FolderCard(
                                folder = cell.folder,
                                onClick = { onFolderClick(cell.folder.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        is FolderGridCell.AddButton -> {
                            AddItemButton(
                                onClick = onAddFolderClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        is FolderGridCell.InlineEditor -> {
                            val state = cell.state
                            InlineItemEditor(
                                value = when (state) {
                                    is InlineCreationState.Editing -> state.name
                                    is InlineCreationState.Saving -> state.name
                                    is InlineCreationState.Error -> state.name
                                    else -> ""
                                },
                                onValueChange = onInlineNameChanged,
                                onConfirm = onInlineConfirm,
                                onCancel = onInlineCancel,
                                isLoading = state is InlineCreationState.Saving,
                                errorMessage = (state as? InlineCreationState.Error)?.message,
                                icon = Icons.Default.Folder,
                                placeholder = "Folder name",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                // Pad odd-count rows with a Spacer
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (index < gridRows.lastIndex) {
                Spacer(modifier = Modifier.height(EchoListTheme.dimensions.s))
            }
        }
        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.l))

        // FILES section
        Text(
            text = "FILES",
            style = MaterialTheme.typography.labelSmall,
            color = EchoListTheme.materialColors.primary
        )
        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.s))

        val fileGridItems = buildFileGridItems(uiState.files)
        val fileGridRows = fileGridItems.chunked(2)
        fileGridRows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
            ) {
                row.forEach { cell ->
                    when (cell) {
                        is FileGridCell.File -> {
                            FileItem(
                                file = cell.file,
                                onClick = { onFileClick(cell.file.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        is FileGridCell.AddButton -> {
                            AddFileButton(
                                onAddNoteClick = onAddNoteClick,
                                onAddTasklistClick = onAddTasklistClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                // Pad odd-count rows with a Spacer
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (index < fileGridRows.lastIndex) {
                Spacer(modifier = Modifier.height(EchoListTheme.dimensions.s))
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    val uiState = HomeScreenUiState(
        title = "My Notes",
        breadcrumbs = listOf(
            BreadcrumbItem(label = "Home", path = "/"),
            BreadcrumbItem(label = "My Notes", path = "/my-notes")
        ),
        folders = listOf(
            FolderUiModel(id = "1", name = "Work", itemCount = 5),
            FolderUiModel(id = "2", name = "Personal", itemCount = 3)
        ),
        files = listOf(
            FileUiModel(
                id = "1",
                title = "Meeting Notes",
                fileType = FileType.NOTE,
                preview = "Discussed project timeline and deliverables...",
                timestamp = "2 hours ago"
            ),
            FileUiModel(
                id = "2",
                title = "Shopping List",
                fileType = FileType.TASK_LIST,
                preview = "Milk, eggs, bread, butter...",
                timestamp = "Yesterday"
            )
        )
    )
    EchoListTheme {
        HomeScreen(
            uiState = uiState,
            onNavigationClick = { },
            onBreadcrumbClick = { },
            onFolderClick = { },
            onFileClick = { },
            onAddFolderClick = { },
            onInlineNameChanged = { },
            onInlineConfirm = { },
            onInlineCancel = { },
            onAddNoteClick = { },
            onAddTasklistClick = { },
        )
    }
}
