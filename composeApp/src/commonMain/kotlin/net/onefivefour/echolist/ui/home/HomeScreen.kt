package net.onefivefour.echolist.ui.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.ui.theme.EchoListTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeScreenUiState,
    onNavigationClick: (() -> Unit)?,
    onBreadcrumbClick: (path: String) -> Unit,
    onFolderClick: (folderId: String) -> Unit,
    onFileClick: (fileId: String) -> Unit,
    onAddFolderClick: () -> Unit,
    onAddNoteClick: () -> Unit,
    onAddTasklistClick: () -> Unit
) {

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
            onAddNoteClick = { },
            onAddTasklistClick = { },
        )
    }
}
