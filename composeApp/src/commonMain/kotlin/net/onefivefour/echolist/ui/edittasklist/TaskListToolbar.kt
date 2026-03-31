package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_delete
import echolist.composeapp.generated.resources.ic_plus
import net.onefivefour.echolist.ui.common.ElButton
import net.onefivefour.echolist.ui.common.RoundIconButton
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun TaskListToolbar(
    uiState: EditTaskListUiState,
    onAddMainTask: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val addMainTaskClick = if (uiState.isLoading || uiState.isSaving) {
        {}
    } else {
        onAddMainTask
    }

    val deleteClick = if (uiState.isEditMode && !uiState.isLoading && !uiState.isSaving) {
        onDeleteClick
    } else {
        {}
    }

    Row(
        modifier = Modifier.Companion.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m),
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        if (uiState.isEditMode) {
            Icon(
                painter = painterResource(Res.drawable.ic_delete),
                contentDescription = "Delete task list",
                modifier = Modifier.Companion
                    .clip(RoundedCornerShape(50))
                    .clickable { deleteClick() }
                    .padding(
                        horizontal = EchoListTheme.dimensions.m,
                        vertical = EchoListTheme.dimensions.m
                    )
            )
        }

        ElButton(
            onClick = onSaveClick,
            isEnabled = uiState.isSaveEnabled,
            modifier = Modifier.Companion.weight(1f)
        ) {
            Text(
                text = if (uiState.isSaving) "Saving..." else "Save",
                style = EchoListTheme.typography.labelMedium
            )
        }

        RoundIconButton(
            iconRes = Res.drawable.ic_plus,
            onClick = addMainTaskClick,
            containerColor = EchoListTheme.materialColors.primary,
            contentColor = EchoListTheme.materialColors.onPrimary,
            contentDescription = "Add main task"
        )
    }
}