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
import androidx.compose.ui.platform.LocalFocusManager
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
    onRemoveSubTask: (Int, Int) -> Unit,
    onMainTaskCheckedChange: (Int, Boolean) -> Unit,
    onSubTaskCheckedChange: (Int, Int, Boolean) -> Unit,
    onToggleAutoDelete: (Boolean) -> Unit,
    onFieldFocusLost: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var pendingFocusTarget by remember { mutableStateOf<FocusTarget?>(null) }

    val resolvedFocusTarget = resolveFocusTarget(
        tasks = uiState.mainTasks,
        focusTarget = pendingFocusTarget
    )

    val onFocusHandled = {
        pendingFocusTarget = null
    }

    val applyKeyboardAction = { action: KeyboardActionResolution? ->
        if (action == null) {
            Unit
        } else {
            pendingFocusTarget = action.focusTarget

            when (val mutation = action.mutation) {
                null -> Unit
                KeyboardMutation.AddMainTask -> onAddMainTask()
                is KeyboardMutation.RemoveMainTask -> {
                    val mainTaskIndex = uiState.mainTasks.indexOfFirst { it.id == mutation.mainTaskId }
                    if (mainTaskIndex != -1) {
                        onRemoveMainTask(mainTaskIndex)
                    }
                }

                is KeyboardMutation.AddSubTask -> {
                    val mainTaskIndex = uiState.mainTasks.indexOfFirst { it.id == mutation.mainTaskId }
                    if (mainTaskIndex != -1) {
                        onAddSubTask(mainTaskIndex)
                    }
                }

                is KeyboardMutation.RemoveSubTask -> {
                    val mainTaskIndex = uiState.mainTasks.indexOfFirst { it.id == mutation.mainTaskId }
                    val subTaskIndex = uiState.mainTasks
                        .getOrNull(mainTaskIndex)
                        ?.subTasks
                        ?.indexOfFirst { it.subTaskId == mutation.subTaskId }
                        ?: -1

                    if (mainTaskIndex != -1 && subTaskIndex != -1) {
                        onRemoveSubTask(mainTaskIndex, subTaskIndex)
                    }
                }
            }

            if (action.shouldClearFocus) {
                focusManager.clearFocus(force = true)
            }
        }
    }

    val onAddMainTaskAndFocus = {
        applyKeyboardAction(
            KeyboardActionResolution(
                focusTarget = FocusTarget.LastMainTask,
                mutation = KeyboardMutation.AddMainTask
            )
        )
    }

    val onTitleKeyboardAction = {
        applyKeyboardAction(resolveTitleKeyboardAction(uiState.mainTasks))
    }

    val onMainTaskKeyboardAction: (Long) -> Unit = { mainTaskId ->
        applyKeyboardAction(
            resolveMainTaskKeyboardAction(
                mainTasks = uiState.mainTasks,
                currentMainTaskId = mainTaskId
            )
        )
    }

    val onSubTaskKeyboardAction: (Long, Long) -> Unit = { mainTaskId, subTaskId ->
        applyKeyboardAction(
            resolveSubTaskKeyboardAction(
                mainTasks = uiState.mainTasks,
                mainTaskId = mainTaskId,
                currentSubTaskId = subTaskId
            )
        )
    }

    Column(modifier = modifier.fillMaxSize().imePadding()) {

        EditNoteTitle(
            textFieldState = uiState.titleState,
            requestFocus = uiState.isCreateMode,
            onNext = onTitleKeyboardAction,
            onFocusLost = onFieldFocusLost
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
                        onFieldFocusLost = onFieldFocusLost,
                        focusTarget = resolvedFocusTarget,
                        onFocusHandled = onFocusHandled,
                        onMainTaskKeyboardAction = onMainTaskKeyboardAction,
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

        if (uiState.isPersisted) {
            Spacer(modifier = Modifier.height(EchoListTheme.dimensions.m))

            TaskListToolbar(
                isEnabled = !uiState.isLoading && !uiState.isSaving,
                onDeleteClick = onDeleteClick
            )
        }
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
                onRemoveSubTask = { _, _ -> },
                onMainTaskCheckedChange = { _, _ -> },
                onSubTaskCheckedChange = { _, _, _ -> },
                onToggleAutoDelete = {},
                onFieldFocusLost = {},
                onDeleteClick = {}
            )
        }
    }
}
