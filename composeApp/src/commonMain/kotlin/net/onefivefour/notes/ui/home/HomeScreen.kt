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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.notes.ui.theme.LocalBeepMeDimensions

@Composable
fun HomeScreen(
    uiState: HomeScreenUiState,
    onNavigationClick: () -> Unit,
    onBreadcrumbClick: (path: String) -> Unit,
    onFolderClick: (folderId: String) -> Unit,
    onFileClick: (fileId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalBeepMeDimensions.current
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = dimensions.l)
            .verticalScroll(rememberScrollState())
    ) {
        Header(
            title = uiState.title,
            onNavigationClick = onNavigationClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(dimensions.s))

        BreadcrumbNav(
            breadcrumbs = uiState.breadcrumbs,
            onBreadcrumbClick = onBreadcrumbClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(dimensions.l))

        // FOLDERS section
        if (uiState.folders.isNotEmpty()) {
            Text(
                text = "FOLDERS",
                style = MaterialTheme.typography.labelSmall,
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(dimensions.s))
            val folderRows = uiState.folders.chunked(2)
            folderRows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.s)
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
                    Spacer(modifier = Modifier.height(dimensions.s))
                }
            }
            Spacer(modifier = Modifier.height(dimensions.l))
        }

        // FILES section
        if (uiState.files.isNotEmpty()) {
            Text(
                text = "FILES",
                style = MaterialTheme.typography.labelSmall,
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(dimensions.s))
            uiState.files.forEachIndexed { index, file ->
                FileItem(
                    file = file,
                    onClick = { onFileClick(file.id) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (index < uiState.files.lastIndex) {
                    Spacer(modifier = Modifier.height(dimensions.s))
                }
            }
        }
    }
}
