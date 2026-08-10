package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_delete
import net.onefivefour.echolist.ui.common.GradientBackground
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeScreenUiState,
    onBreadcrumbClick: (path: String) -> Unit,
    onRefresh: () -> Unit = {},
    onFolderClick: (path: String) -> Unit = {},
    onNoteClick: (noteId: String) -> Unit = {},
    onTaskClick: (taskListId: String) -> Unit = {},
    onDeleteCurrentFolderClick: () -> Unit = {},
    createItemCallbacks: CreateItemCallbacks = CreateItemCallbacks(),
    createFolderUiState: CreateFolderUiState = CreateFolderUiState(),
    onFolderNameChange: (String) -> Unit = {},
    onConfirmCreateFolder: () -> Unit = {},
    onDismissCreateFolder: () -> Unit = {}
) {

    Column {
        BreadcrumbBar(
            breadcrumbs = uiState.breadcrumbs,
            onBreadcrumbClick = onBreadcrumbClick
        )

        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.xxl))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = uiState.breadcrumbs.lastOrNull()?.label ?: "",
                color = EchoListTheme.materialColors.primary,
                style = EchoListTheme.typography.titleLarge
            )

            if (uiState.canDeleteCurrentFolder) {
                Icon(
                    painter = painterResource(Res.drawable.ic_delete),
                    contentDescription = "Delete folder",
                    tint = EchoListTheme.materialColors.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = !uiState.isDeletingFolder) { onDeleteCurrentFolderClick() }
                        .padding(
                            horizontal = EchoListTheme.dimensions.m,
                            vertical = EchoListTheme.dimensions.m
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.m))

        val pullToRefreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            modifier = Modifier.weight(1f),
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = uiState.isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = EchoListTheme.materialColors.primary,
                    color = EchoListTheme.materialColors.onPrimary
                )
            }
        ) {
            FileOverview(
                fileEntries = uiState.fileEntries,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onFolderClick = onFolderClick,
                onNoteClick = onNoteClick,
                onTaskClick = onTaskClick
            )
        }

        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.xxl))

        BottomNavigation(
            createItemCallbacks = createItemCallbacks
        )

        CreateFolderDialog(
            uiState = createFolderUiState,
            onNameChange = onFolderNameChange,
            onConfirm = onConfirmCreateFolder,
            onDismiss = onDismissCreateFolder
        )
    }
}

@Composable
@Preview
private fun HomeScreenPreview() {
    EchoListTheme {
        GradientBackground {
            HomeScreen(
                uiState = HomeScreenUiState(
                    breadcrumbs = listOf(
                        BreadcrumbItem(
                            label = "Home",
                            parentDir = ""
                        ),
                        BreadcrumbItem(
                            label = "Folder 1",
                            parentDir = "folder1"
                        )
                    )
                ),
                onBreadcrumbClick = {}
            )
        }
    }
}
