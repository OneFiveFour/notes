package net.onefivefour.echolist.feature.tasklist.ui

import net.onefivefour.echolist.core.designsystem.resources.Res as SharedRes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import net.onefivefour.echolist.feature.tasklist.resources.Res
import net.onefivefour.echolist.core.designsystem.resources.ic_delete
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun TaskListDeleteButton(
    uiState: EditTaskListUiState,
    onDeleteTaskList: () -> Unit
) {
    if (uiState.isPersisted) {
        Icon(
            painter = painterResource(SharedRes.drawable.ic_delete),
            contentDescription = "Delete task list",
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable(enabled = !uiState.isLoading && !uiState.isSaving) { onDeleteTaskList() }
                .padding(
                    horizontal = EchoListTheme.dimensions.m,
                    vertical = EchoListTheme.dimensions.m
                )
        )
    }
}
