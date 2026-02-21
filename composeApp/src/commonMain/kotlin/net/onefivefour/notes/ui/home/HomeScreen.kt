package net.onefivefour.notes.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import net.onefivefour.notes.ui.theme.EchoListTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeScreenUiState,
    onNavigationClick: (() -> Unit)?,
    onBreadcrumbClick: (path: String) -> Unit,
    onFolderClick: (folderId: String) -> Unit,
    onFileClick: (fileId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EchoListTheme.materialColors.background)
            .padding(horizontal = EchoListTheme.dimensions.l)
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
        val folderRows = uiState.folders.chunked(2)
        folderRows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
            ) {
                row.forEach { folder ->
                    FolderCard(
                        folder = folder,
                        onClick = { onFolderClick(folder.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty space if odd number of folders
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (index < folderRows.lastIndex) {
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
        uiState.files.forEachIndexed { index, file ->
            FileItem(
                file = file,
                onClick = { onFileClick(file.id) },
                modifier = Modifier.fillMaxWidth()
            )
            if (index < uiState.files.lastIndex) {
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
                preview = "Discussed project timeline and deliverables...",
                timestamp = "2 hours ago"
            ),
            FileUiModel(
                id = "2",
                title = "Shopping List",
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
        )
    }
}
