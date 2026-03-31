package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.input.ImeAction
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_delete
import echolist.composeapp.generated.resources.ic_plus
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.SubTask
import net.onefivefour.echolist.ui.common.ElButton
import net.onefivefour.echolist.ui.common.ElTextField
import net.onefivefour.echolist.ui.common.GradientBackground
import net.onefivefour.echolist.ui.common.RoundIconButton
import net.onefivefour.echolist.ui.editnote.EditNoteTitle
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun EditTaskListScreen(
    uiState: EditTaskListUiState,
    onAddMainTask: () -> Unit,
    onRemoveMainTask: (Int) -> Unit,
    onAddSubTask: (Int) -> Unit,
    onRemoveSubTask: (Int, Int) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dimensions = EchoListTheme.dimensions
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
    val onSubTaskKeyboardAction: (Int, MainTaskDraft, Long) -> Unit = { mainTaskIndex, mainTask, subTaskId ->
        val advance = resolveSubTaskAdvance(
            mainTasks = uiState.mainTasks,
            mainTaskId = mainTask.id,
            currentSubTaskId = subTaskId
        )
        if (advance != null) {
            pendingFocusTarget = advance.focusTarget
            if (advance.shouldAddSubTask) {
                onAddSubTask(mainTaskIndex)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(
                horizontal = dimensions.xl,
                vertical = dimensions.l
            )
    ) {
        EditNoteTitle(
            textFieldState = uiState.titleState,
            requestFocus = uiState.isCreateMode,
            onNext = onAddMainTaskAndFocus
        )

        Spacer(modifier = Modifier.height(dimensions.m))

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
            Box(
                modifier = Modifier
                    .padding(dimensions.m)
                    .fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> EditTaskListLoading()
                    uiState.mainTasks.isEmpty() -> EditTaskListEmptyState(onAddMainTaskAndFocus)
                    else -> EditTaskListContent(
                        mainTasks = uiState.mainTasks,
                        onRemoveMainTask = onRemoveMainTask,
                        onAddMainTask = onAddMainTaskAndFocus,
                        onAddSubTask = onAddSubTask,
                        onRemoveSubTask = onRemoveSubTask,
                        focusTarget = resolvedFocusTarget,
                        onFocusHandled = onFocusHandled,
                        onSubTaskKeyboardAction = onSubTaskKeyboardAction
                    )
                }
            }
        }

        uiState.error?.let { errorMessage ->
            Spacer(modifier = Modifier.height(dimensions.s))
            Text(
                text = errorMessage,
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.error
            )
        }

        Spacer(modifier = Modifier.height(dimensions.m))

        EditTaskListToolbar(
            uiState = uiState,
            onAddMainTask = onAddMainTaskAndFocus,
            onSaveClick = onSaveClick,
            onDeleteClick = onDeleteClick
        )
    }
}

@Composable
private fun EditTaskListContent(
    mainTasks: List<MainTaskDraft>,
    onRemoveMainTask: (Int) -> Unit,
    onAddMainTask: () -> Unit,
    onAddSubTask: (Int) -> Unit,
    onRemoveSubTask: (Int, Int) -> Unit,
    focusTarget: FocusTarget?,
    onFocusHandled: () -> Unit,
    onSubTaskKeyboardAction: (Int, MainTaskDraft, Long) -> Unit
) {
    val dimensions = EchoListTheme.dimensions
    val mainTaskToFocus = focusTarget as? FocusTarget.MainTask
    val subTaskToFocus = focusTarget as? FocusTarget.SubTask

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(dimensions.m),
        contentPadding = PaddingValues(bottom = dimensions.s)
    ) {
        itemsIndexed(mainTasks, key = { _, mainTask -> mainTask.id }) { mainTaskIndex, mainTask ->
            MainTaskEditorCard(
                mainTask = mainTask,
                mainTaskIndex = mainTaskIndex,
                onRemoveMainTask = onRemoveMainTask,
                onAddMainTask = onAddMainTask,
                onAddSubTask = onAddSubTask,
                onRemoveSubTask = onRemoveSubTask,
                requestDescriptionFocus = mainTaskToFocus?.mainTaskId == mainTask.id,
                onDescriptionFocusHandled = onFocusHandled,
                focusedSubTaskId = subTaskToFocus?.id?.takeIf { subTaskToFocus.mainTaskId == mainTask.id },
                onSubTaskFocusHandled = onFocusHandled,
                onSubTaskKeyboardAction = { subTaskId ->
                    onSubTaskKeyboardAction(mainTaskIndex, mainTask, subTaskId)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MainTaskEditorCard(
    mainTask: MainTaskDraft,
    mainTaskIndex: Int,
    onRemoveMainTask: (Int) -> Unit,
    onAddMainTask: () -> Unit,
    onAddSubTask: (Int) -> Unit,
    onRemoveSubTask: (Int, Int) -> Unit,
    requestDescriptionFocus: Boolean,
    onDescriptionFocusHandled: () -> Unit,
    focusedSubTaskId: Long?,
    onSubTaskFocusHandled: () -> Unit,
    onSubTaskKeyboardAction: (Long) -> Unit
) {
    val dimensions = EchoListTheme.dimensions
    val descriptionFocusRequester = remember(mainTask.id) { FocusRequester() }

    LaunchedEffect(requestDescriptionFocus) {
        if (requestDescriptionFocus) {
            descriptionFocusRequester.requestFocus()
            onDescriptionFocusHandled()
        }
    }

    Surface(
        shape = EchoListTheme.shapes.medium,
        color = EchoListTheme.materialColors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = EchoListTheme.dimensions.borderWidth,
                color = EchoListTheme.materialColors.surfaceVariant,
                shape = EchoListTheme.shapes.medium
            )
    ) {
        Column(
            modifier = Modifier.padding(dimensions.s)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.s)
            ) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides EchoListTheme.dimensions.xxl) {
                    Checkbox(
                        checked = mainTask.isDone,
                        onCheckedChange = { isChecked -> mainTask.isDone = isChecked }
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {

                    ElTextField(
                        state = mainTask.descriptionState,
                        style = EchoListTheme.typography.bodyLarge,
                        singleLine = true,
                        imeAction = ImeAction.Next,
                        onKeyboardAction = onAddMainTask,
                        focusRequester = descriptionFocusRequester
                    )

                    if (mainTask.dueDateState.text.isNotEmpty()) {
                        ElTextField(
                            state = mainTask.dueDateState,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (mainTask.recurrenceState.text.isNotEmpty()) {
                        ElTextField(
                            state = mainTask.recurrenceState,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Icon(
                    painter = painterResource(Res.drawable.ic_delete),
                    contentDescription = "Delete main task",
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onRemoveMainTask(mainTaskIndex) }
                        .padding(
                            horizontal = dimensions.m,
                            vertical = dimensions.m
                        )
                )
            }

            if (mainTask.subTasks.isNotEmpty()) {
                Column {
                    mainTask.subTasks.forEachIndexed { subTaskIndex, subTask ->
                        SubTaskRow(
                            subTask = subTask,
                            shouldRequestFocus = focusedSubTaskId == subTask.subTaskId,
                            onFocusHandled = onSubTaskFocusHandled,
                            onKeyboardAction = { onSubTaskKeyboardAction(subTask.subTaskId) },
                            onRemoveSubTask = { onRemoveSubTask(mainTaskIndex, subTaskIndex) }
                        )
                    }
                }
            }

            TextButton(
                onClick = { onAddSubTask(mainTaskIndex) }
            ) {
                Text(
                    text = "Add subtask",
                    style = EchoListTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun SubTaskRow(
    subTask: SubTaskDraft,
    shouldRequestFocus: Boolean,
    onFocusHandled: () -> Unit,
    onKeyboardAction: () -> Unit,
    onRemoveSubTask: () -> Unit
) {
    val focusRequester = remember(subTask.subTaskId) { FocusRequester() }

    LaunchedEffect(shouldRequestFocus) {
        if (shouldRequestFocus) {
            focusRequester.requestFocus()
            onFocusHandled()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = EchoListTheme.dimensions.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides EchoListTheme.dimensions.xxl) {
            Checkbox(
                checked = subTask.isDone,
                onCheckedChange = { isChecked -> subTask.isDone = isChecked }
            )
        }

        ElTextField(
            state = subTask.descriptionState,
            modifier = Modifier.weight(1f),
            singleLine = true,
            imeAction = ImeAction.Next,
            onKeyboardAction = onKeyboardAction,
            focusRequester = focusRequester
        )

        Icon(
            painter = painterResource(Res.drawable.ic_delete),
            contentDescription = "Delete subtask",
            modifier = Modifier.clickable { onRemoveSubTask() }
        )
    }
}

@Composable
private fun EditTaskListToolbar(
    uiState: EditTaskListUiState,
    onAddMainTask: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dimensions = EchoListTheme.dimensions
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensions.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (uiState.isEditMode) {
            Icon(
                painter = painterResource(Res.drawable.ic_delete),
                contentDescription = "Delete task list",
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { deleteClick() }
                    .padding(
                        horizontal = dimensions.m,
                        vertical = dimensions.m
                    )
            )
        } else {
            Spacer(modifier = Modifier.width(dimensions.xxxl))
        }

        ElButton(
            onClick = onSaveClick,
            isEnabled = uiState.isSaveEnabled,
            modifier = Modifier.weight(1f)
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

@Composable
private fun EditTaskListLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Loading task list...",
            style = EchoListTheme.typography.bodyMedium,
            color = EchoListTheme.materialColors.onSurface
        )
    }
}

@Composable
private fun EditTaskListEmptyState(onAddMainTask: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
        ) {
            Text(
                text = "No main tasks yet.",
                style = EchoListTheme.typography.bodyMedium,
                color = EchoListTheme.materialColors.onSurface
            )
            TextButton(onClick = onAddMainTask) {
                Text(
                    text = "Add the first task",
                    style = EchoListTheme.typography.labelMedium
                )
            }
        }
    }
}

@Preview
@Composable
private fun SubTaskRowPreview() {
    val subTask = remember {
        SubTaskDraft(subTaskId = 1, description = "Review copy", isDone = false)
    }
    EchoListTheme {
        GradientBackground {
            SubTaskRow(
                subTask = subTask,
                shouldRequestFocus = false,
                onFocusHandled = {},
                onKeyboardAction = {},
                onRemoveSubTask = {}
            )
        }
    }
}

@Preview
@Composable
private fun SubTaskRowDonePreview() {
    val subTask = remember {
        SubTaskDraft(subTaskId = 2, description = "Draft checklist", isDone = true)
    }
    EchoListTheme {
        GradientBackground {
            SubTaskRow(
                subTask = subTask,
                shouldRequestFocus = false,
                onFocusHandled = {},
                onKeyboardAction = {},
                onRemoveSubTask = {}
            )
        }
    }
}

@Preview
@Composable
private fun MainTaskEditorCardPreview() {
    val task = remember {
        MainTaskDraft.fromDomain(
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
    }
    EchoListTheme {
        GradientBackground {
            MainTaskEditorCard(
                mainTask = task,
                mainTaskIndex = 0,
                onRemoveMainTask = {},
                onAddMainTask = {},
                onAddSubTask = {},
                onRemoveSubTask = { _, _ -> },
                requestDescriptionFocus = false,
                onDescriptionFocusHandled = {},
                focusedSubTaskId = null,
                onSubTaskFocusHandled = {},
                onSubTaskKeyboardAction = {}
            )
        }
    }
}

@Preview
@Composable
private fun MainTaskEditorCardEmptyPreview() {
    val task = remember {
        MainTaskDraft(id = 2)
    }
    EchoListTheme {
        GradientBackground {
            MainTaskEditorCard(
                mainTask = task,
                mainTaskIndex = 0,
                onRemoveMainTask = {},
                onAddMainTask = {},
                onAddSubTask = {},
                onRemoveSubTask = { _, _ -> },
                requestDescriptionFocus = false,
                onDescriptionFocusHandled = {},
                focusedSubTaskId = null,
                onSubTaskFocusHandled = {},
                onSubTaskKeyboardAction = {}
            )
        }
    }
}

@Preview
@Composable
private fun EditTaskListScreenPreview() {
    val tasks = remember {
        androidx.compose.runtime.mutableStateListOf(
            MainTaskDraft.fromDomain(
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
                    mode = EditTaskListMode.Create(parentPath = "")
                ),
                onAddMainTask = {},
                onRemoveMainTask = {},
                onAddSubTask = {},
                onRemoveSubTask = { _, _ -> },
                onSaveClick = {}
            )
        }
    }
}
