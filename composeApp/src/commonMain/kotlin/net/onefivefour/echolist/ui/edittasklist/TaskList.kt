package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun TaskList(
    mainTasks: List<UiMainTask>,
    isAutoDelete: Boolean,
    onRemoveMainTask: (Int) -> Unit,
    onMainTaskCheckedChange: (Int, Boolean) -> Unit,
    onAddMainTask: () -> Unit,
    onSubTaskCheckedChange: (Int, Int, Boolean) -> Unit,
    onFieldFocusLost: () -> Unit,
    focusTarget: FocusTarget?,
    onFocusHandled: () -> Unit,
    onSubTaskKeyboardAction: (Int, UiMainTask, Long) -> Unit
) {
    val mainTaskToFocus = focusTarget as? FocusTarget.MainTask
    val subTaskToFocus = focusTarget as? FocusTarget.SubTask

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m),
        contentPadding = PaddingValues(bottom = EchoListTheme.dimensions.s)
    ) {
        itemsIndexed(mainTasks, key = { _, mainTask -> mainTask.id }) { mainTaskIndex, mainTask ->
            MainTaskCard(
                mainTask = mainTask,
                isAutoDelete = isAutoDelete,
                onRemoveMainTask = { onRemoveMainTask(mainTaskIndex) },
                onMainTaskCheckedChange = { isChecked ->
                    onMainTaskCheckedChange(mainTaskIndex, isChecked)
                },
                onAddMainTask = onAddMainTask,
                onSubTaskCheckedChange = { subTaskIndex, isChecked ->
                    onSubTaskCheckedChange(mainTaskIndex, subTaskIndex, isChecked)
                },
                onFieldFocusLost = onFieldFocusLost,
                shouldFocusMainTask = mainTaskToFocus?.mainTaskId == mainTask.id,
                onMainTaskFocusHandled = onFocusHandled,
                focusedSubTaskId = subTaskToFocus?.id?.takeIf { subTaskToFocus.mainTaskId == mainTask.id },
                onSubTaskFocusHandled = onFocusHandled,
                onSubTaskKeyboardAction = { subTaskId ->
                    onSubTaskKeyboardAction(mainTaskIndex, mainTask, subTaskId)
                }
            )
        }

        item {
            TextButton(onClick = onAddMainTask) {
                Text(
                    text = "Add another main task",
                    style = EchoListTheme.typography.labelMedium,
                    color = EchoListTheme.materialColors.primary
                )
            }
        }
    }
}
