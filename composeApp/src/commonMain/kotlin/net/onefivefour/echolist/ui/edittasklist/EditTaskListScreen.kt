package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.mutableStateListOf
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.SubTask
import net.onefivefour.echolist.ui.common.GradientBackground
import net.onefivefour.echolist.ui.editnote.EditNoteTitle
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun EditTaskListScreen(
    uiState: EditTaskListUiState,
    onAddMainTask: () -> Unit,
    onRemoveMainTask: (Int) -> Unit,
    onAddSubTask: (Int) -> Unit,
    onMainTaskCheckedChange: (Int, Boolean) -> Unit,
    onSubTaskCheckedChange: (Int, Int, Boolean) -> Unit,
    onToggleAutoDelete: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    var pendingFocusTarget by remember { mutableStateOf<FocusTarget?>(null) }

    val resolvedFocusTarget = resolveFocusTarget(
        tasks = uiState.mainTasks,
        focusTarget = pendingFocusTarget
    )

    val onFocusHandled = {
        pendingFocusTarget = null
    }

    val onAddMainTaskAndFocus = {
        pendingFocusTarget = FocusTarget.LastMainTask
        onAddMainTask()
    }

    val onSubTaskKeyboardAction: (Int, UiMainTask, Long) -> Unit = { mainTaskIndex, mainTask, subTaskId ->
        resolveSubTaskAdvance(
            mainTasks = uiState.mainTasks,
            mainTaskId = mainTask.id,
            currentSubTaskId = subTaskId
        )?.let { advanceResult ->
            pendingFocusTarget = advanceResult.focusTarget
            if (advanceResult.shouldAddSubTask) {
                onAddSubTask(mainTaskIndex)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().imePadding()) {

        EditNoteTitle(
            textFieldState = uiState.titleState,
            requestFocus = uiState.isCreateMode,
            onNext = onAddMainTaskAndFocus
        )

        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.m))

        Surface(
            shape = EchoListTheme.shapes.medium,
            color = EchoListTheme.materialColors.surface,
            modifier = Modifier
                .weight(1f)
                .border(
                    width = EchoListTheme.dimensions.borderWidth,
                    color = EchoListTheme.materialColors.surfaceVariant,
                    shape = EchoListTheme.shapes.medium
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(EchoListTheme.dimensions.m)
                    .fillMaxSize()
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Auto Delete",
                        style = EchoListTheme.typography.titleSmall,
                        color = EchoListTheme.materialColors.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = uiState.isAutoDelete,
                        onCheckedChange = onToggleAutoDelete,
                        enabled = !uiState.isLoading && !uiState.isSaving
                    )
                }

                Spacer(modifier = Modifier.height(EchoListTheme.dimensions.m))

                when {
                    uiState.isLoading -> TaskListLoading()
                    uiState.mainTasks.isEmpty() -> EmptyTaskList(onAddMainTaskAndFocus)
                    else -> TaskList(
                        mainTasks = uiState.mainTasks,
                        isAutoDelete = uiState.isAutoDelete,
                        onRemoveMainTask = onRemoveMainTask,
                        onMainTaskCheckedChange = onMainTaskCheckedChange,
                        onAddMainTask = onAddMainTaskAndFocus,
                        onSubTaskCheckedChange = onSubTaskCheckedChange,
                        focusTarget = resolvedFocusTarget,
                        onFocusHandled = onFocusHandled,
                        onSubTaskKeyboardAction = onSubTaskKeyboardAction
                    )
                }
            }
        }

        uiState.error?.let { errorMessage ->
            Spacer(modifier = Modifier.height(EchoListTheme.dimensions.s))
            Text(
                text = errorMessage,
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.error
            )
        }

        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.m))

        TaskListToolbar(
            uiState = uiState,
            onAddMainTask = onAddMainTaskAndFocus,
            onSaveClick = onSaveClick,
            onDeleteClick = onDeleteClick
        )
    }
}

@Preview
@Composable
private fun EditTaskListScreenPreview() {
    val tasks = remember {
        mutableStateListOf(
            UiMainTask.fromDomain(
                id = 1,
                domain = MainTask(
                    description = "Plan launch",
                    isDone = false,
                    dueDate = "2026-04-01",
                    recurrence = "",
                    subTasks = listOf(
                        SubTask(description = "Draft checklist", isDone = true),
                        SubTask(description = "Review copy", isDone = false)
                    )
                )
            )
        )
    }

    EchoListTheme {
        GradientBackground {
            EditTaskListScreen(
                uiState = EditTaskListUiState(
                    titleState = TextFieldState(initialText = "Launch plan"),
                    mainTasks = tasks,
                    isAutoDelete = false,
                    mode = EditTaskListMode.Create(parentPath = "")
                ),
                onAddMainTask = {},
                onRemoveMainTask = {},
                onAddSubTask = {},
                onMainTaskCheckedChange = { _, _ -> },
                onSubTaskCheckedChange = { _, _, _ -> },
                onToggleAutoDelete = {},
                onSaveClick = {},
                onDeleteClick = {}
            )
        }
    }
}
