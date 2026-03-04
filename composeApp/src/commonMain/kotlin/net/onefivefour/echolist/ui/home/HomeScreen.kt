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
