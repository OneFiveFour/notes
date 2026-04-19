package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.SubTask
import net.onefivefour.echolist.ui.common.EditTitle
import net.onefivefour.echolist.ui.common.GradientBackground
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
    onDueDateSelected: (Long, String) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val controller = rememberEditTaskListScreenController(
        mainTasks = uiState.mainTasks,
        onAddMainTask = onAddMainTask,
        onRemoveMainTask = onRemoveMainTask,
        onAddSubTask = onAddSubTask,
        onRemoveSubTask = onRemoveSubTask
    )
    var activeDateSheet by remember { mutableStateOf<TaskDateSheetState?>(null) }
    val sheetTask = activeDateSheet?.let { sheet ->
        uiState.mainTasks.firstOrNull { it.id == sheet.mainTaskId }
    }

    LaunchedEffect(activeDateSheet?.mainTaskId, sheetTask) {
        if (activeDateSheet != null && sheetTask == null) {
            activeDateSheet = null
        }
    }

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        EditTitle(
            textFieldState = uiState.titleState,
            requestFocus = uiState.isCreateMode,
            onNext = controller.onTitleKeyboardAction,
            onFocusLost = onFieldFocusLost
        )

        TaskListContentCard(
            modifier = Modifier.weight(1f),
            uiState = uiState,
            controller = controller,
            onToggleAutoDelete = onToggleAutoDelete,
            onFieldFocusLost = onFieldFocusLost,
            onMainTaskCheckedChange = onMainTaskCheckedChange,
            onRemoveMainTask = onRemoveMainTask,
            onSubTaskCheckedChange = onSubTaskCheckedChange,
            onOpenTaskDateSheet = { mainTaskId ->
                activeDateSheet = TaskDateSheetState(mainTaskId = mainTaskId)
            },
            onDeleteClick = onDeleteClick
        )

        uiState.error?.let { errorMessage ->
            Spacer(modifier = Modifier.height(EchoListTheme.dimensions.s))
            Text(
                text = errorMessage,
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.error
            )
        }

        Box(
            modifier = Modifier
                .size(1.dp)
                .focusRequester(controller.blurFocusRequester)
                .focusable()
        )
    }

    val currentSheetState = activeDateSheet
    if (currentSheetState != null && sheetTask != null) {
        TaskDateBottomSheet(
            sheetState = currentSheetState,
            mainTask = sheetTask,
            onDismissRequest = { activeDateSheet = null },
            onTabSelected = { selectedTab ->
                activeDateSheet = currentSheetState.copy(selectedTab = selectedTab)
            },
            onDueDateSelected = { dueDate ->
                activeDateSheet = null
                onDueDateSelected(currentSheetState.mainTaskId, dueDate)
            }
        )
    }
}

@Composable
private fun TaskListContentCard(
    modifier: Modifier = Modifier,
    uiState: EditTaskListUiState,
    controller: EditTaskListScreenController,
    onToggleAutoDelete: (Boolean) -> Unit,
    onFieldFocusLost: () -> Unit,
    onMainTaskCheckedChange: (Int, Boolean) -> Unit,
    onRemoveMainTask: (Int) -> Unit,
    onSubTaskCheckedChange: (Int, Int, Boolean) -> Unit,
    onOpenTaskDateSheet: (Long) -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        shape = EchoListTheme.shapes.medium,
        color = EchoListTheme.materialColors.surface,
        modifier = modifier
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
            DeleteRow(
                uiState = uiState,
                onToggleAutoDelete = onToggleAutoDelete,
                onDeleteTaskList = onDeleteClick
            )

            Spacer(modifier = Modifier.height(EchoListTheme.dimensions.m))

            when {
                uiState.isLoading -> TaskListLoading()
                uiState.mainTasks.isEmpty() -> EmptyTaskList(controller.onAddMainTaskAndFocus)
                else -> TaskList(
                    focusedMainTaskId = controller.focusedMainTaskId,
                    focusTarget = controller.resolvedFocusTarget,
                    isAutoDelete = uiState.isAutoDelete,
                    isEditMode = uiState.isEditMode,
                    mainTasks = uiState.mainTasks,
                    onAddMainTask = controller.onAddMainTaskAndFocus,
                    onAddSubTask = controller.onAddFirstSubTaskAndFocus,
                    onFieldFocusLost = onFieldFocusLost,
                    onFocusHandled = controller.onFocusHandled,
                    onMainTaskCheckedChange = onMainTaskCheckedChange,
                    onMainTaskDescriptionFocusChanged = controller.onMainTaskDescriptionFocusChanged,
                    onOpenTaskDateSheet = onOpenTaskDateSheet,
                    onMainTaskKeyboardAction = controller.onMainTaskKeyboardAction,
                    onRemoveMainTask = onRemoveMainTask,
                    onSubTaskCheckedChange = onSubTaskCheckedChange,
                    onSubTaskKeyboardAction = controller.onSubTaskKeyboardAction
                )
            }
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
                onDueDateSelected = { _, _ -> },
                onDeleteClick = {}
            )
        }
    }
}
