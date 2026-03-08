package net.onefivefour.echolist.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_plus
import echolist.composeapp.generated.resources.ic_search
import echolist.composeapp.generated.resources.ic_settings
import echolist.composeapp.generated.resources.recent
import net.onefivefour.echolist.ui.common.GradientBackground
import net.onefivefour.echolist.ui.common.RoundIconButton
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeScreenUiState,
    onBreadcrumbClick: (path: String) -> Unit,
    onNoteCreate: () -> Unit = {},
    onTaskCreate: () -> Unit = {},
    onFolderCreate: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .systemBarsPadding()
            .padding(
                horizontal = EchoListTheme.dimensions.xl,
                vertical = EchoListTheme.dimensions.l
            )
    ) {
        BreadcrumbBar(
            breadcrumbs = uiState.breadcrumbs,
            onBreadcrumbClick = onBreadcrumbClick
        )

        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.xxl))

        Text(
            text = uiState.breadcrumbs.lastOrNull()?.label ?: "",
            color = EchoListTheme.materialColors.primary,
            style = EchoListTheme.typography.titleLarge
        )

        Surface(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = EchoListTheme.materialColors.surfaceVariant,
                    shape = RoundedCornerShape(50.dp)
                )
                .fillMaxSize()
                .weight(1f)

        ) { }

        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.xxl))

        Text(
            text = stringResource(Res.string.recent),
            color = EchoListTheme.materialColors.primary,
            style = EchoListTheme.typography.titleLarge
        )

        var isFabExpanded by remember { mutableStateOf(false) }

        Crossfade(targetState = isFabExpanded) { showItemPills ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (showItemPills) {
                    true -> {
                        CreateItemPill(
                            color = EchoListTheme.echoListColorScheme.noteColor,
                            text = "Note",
                            onClick = {
                                onNoteCreate()
                                isFabExpanded = false
                            }
                        )
                        Spacer(modifier = Modifier.width(EchoListTheme.dimensions.m))
                        CreateItemPill(
                            color = EchoListTheme.echoListColorScheme.taskColor,
                            text = "Task",
                            onClick = {
                                onTaskCreate()
                                isFabExpanded = false
                            }
                        )
                        Spacer(modifier = Modifier.width(EchoListTheme.dimensions.m))
                        CreateItemPill(
                            color = EchoListTheme.echoListColorScheme.folderColor,
                            text = "Folder",
                            onClick = {
                                onFolderCreate()
                                isFabExpanded = false
                            }
                        )
                    }

                    else -> {
                        RoundIconButton(
                            iconRes = Res.drawable.ic_search,
                            onClick = {}
                        )
                        Spacer(modifier = Modifier.width(EchoListTheme.dimensions.m))
                        RoundIconButton(
                            iconRes = Res.drawable.ic_settings,
                            onClick = {}
                        )
                    }
                }

                Spacer(modifier = Modifier.width(EchoListTheme.dimensions.m))

                RoundIconButton(
                    modifier = Modifier.rotate(
                        when {
                            isFabExpanded -> 45f
                            else -> 0f
                        }
                    ),
                    iconRes = Res.drawable.ic_plus,
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = EchoListTheme.materialColors.primary,
                    contentColor = EchoListTheme.materialColors.onPrimary
                )
            }

        }
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
                            path = "/"
                        ),
                        BreadcrumbItem(
                            label = "Folder 1",
                            path = "/folder1"
                        )
                    )
                ),
                onBreadcrumbClick = {}
            )
        }
    }
}