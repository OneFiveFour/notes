package net.onefivefour.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.notes.ui.home.BreadcrumbItem
import net.onefivefour.notes.ui.home.FileUiModel
import net.onefivefour.notes.ui.home.FolderUiModel
import net.onefivefour.notes.ui.home.HomeScreen
import net.onefivefour.notes.ui.home.HomeScreenUiState
import net.onefivefour.notes.ui.theme.BeepMeClassicTheme
import net.onefivefour.notes.ui.theme.BeepMeTheme
import net.onefivefour.notes.ui.theme.ThemeManager

@Composable
@Preview
fun App() {
    val sampleUiState = remember {
        HomeScreenUiState(
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
                FileUiModel(id = "1", title = "Meeting Notes", preview = "Discussed project timeline and deliverables...", timestamp = "2 hours ago"),
                FileUiModel(id = "2", title = "Shopping List", preview = "Milk, eggs, bread, butter...", timestamp = "Yesterday")
            )
        )
    }

    BeepMeTheme {
        HomeScreen(
            uiState = sampleUiState,
            onNavigationClick = {},
            onBreadcrumbClick = {},
            onFolderClick = {},
            onFileClick = {}
        )
    }
}
