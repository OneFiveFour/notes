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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.input.TextFieldState
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_delete
import echolist.composeapp.generated.resources.ic_plus
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.SubTask
import net.onefivefour.echolist.ui.common.ElButton
import net.onefivefour.echolist.ui.common.ElOutlinedTextField
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
            onNext = onAddMainTask
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
                    uiState.tasks.isEmpty() -> EditTaskListEmptyState(onAddMainTask)
                    else -> EditTaskListContent(
                        tasks = uiState.tasks,
                        onRemoveMainTask = onRemoveMainTask,
                        onAddSubTask = onAddSubTask,
                        onRemoveSubTask = onRemoveSubTask
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
            onAddMainTask = onAddMainTask,
            onSaveClick = onSaveClick,
            onDeleteClick = onDeleteClick
        )
    }
}

@Composable
private fun EditTaskListContent(
    tasks: List<MainTaskDraft>,
    onRemoveMainTask: (Int) -> Unit,
    onAddSubTask: (Int) -> Unit,
    onRemoveSubTask: (Int, Int) -> Unit
) {
    val dimensions = EchoListTheme.dimensions

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(dimensions.m),
        contentPadding = PaddingValues(bottom = dimensions.s)
    ) {
        itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
            MainTaskEditorCard(
                task = task,
                taskIndex = index,
                onRemoveMainTask = onRemoveMainTask,
                onAddSubTask = onAddSubTask,
                onRemoveSubTask = onRemoveSubTask
            )
        }
    }
}

@Composable
private fun MainTaskEditorCard(
    task: MainTaskDraft,
    taskIndex: Int,
    onRemoveMainTask: (Int) -> Unit,
    onAddSubTask: (Int) -> Unit,
    onRemoveSubTask: (Int, Int) -> Unit
) {
    val dimensions = EchoListTheme.dimensions

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
            modifier = Modifier.padding(dimensions.m),
            verticalArrangement = Arrangement.spacedBy(dimensions.s)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.s)
            ) {
                Checkbox(
                    checked = task.done,
                    onCheckedChange = { task.done = it }
                )

                ElOutlinedTextField(
                    text = task.description,
                    label = "Main task",
                    onValueChange = { task.description = it },
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    painter = painterResource(Res.drawable.ic_delete),
                    contentDescription = "Delete main task",
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onRemoveMainTask(taskIndex) }
                        .padding(
                            horizontal = dimensions.m,
                            vertical = dimensions.m
                        )
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(dimensions.s)
            ) {
                ElOutlinedTextField(
                    text = task.dueDate,
                    label = "Due date (YYYY-MM-DD)",
                    onValueChange = { value ->
                        task.dueDate = value
                        if (value.trim().isNotBlank()) {
                            task.recurrence = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                ElOutlinedTextField(
                    text = task.recurrence,
                    label = "RRULE recurrence",
                    onValueChange = { value ->
                        val sanitized = value.singleLine()
                        task.recurrence = sanitized
                        if (sanitized.isNotBlank()) {
                            task.dueDate = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (task.subTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(dimensions.xs))
                Text(
                    text = "Subtasks",
                    style = EchoListTheme.typography.labelMedium,
                    color = EchoListTheme.materialColors.onSurface.copy(alpha = 0.7f)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(dimensions.xs)
                ) {
                    task.subTasks.forEachIndexed { subTaskIndex, subTask ->
                        SubTaskRow(
                            subTask = subTask,
                            onRemoveSubTask = { onRemoveSubTask(taskIndex, subTaskIndex) }
                        )
                    }
                }
            }

            TextButton(
                onClick = { onAddSubTask(taskIndex) }
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
    onRemoveSubTask: () -> Unit
) {
    val dimensions = EchoListTheme.dimensions

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.s)
    ) {
        Checkbox(
            checked = subTask.done,
            onCheckedChange = { subTask.done = it }
        )

        ElOutlinedTextField(
            text = subTask.description,
            label = "Subtask",
            onValueChange = { subTask.description = it },
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(Res.drawable.ic_delete),
            contentDescription = "Delete subtask",
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onRemoveSubTask() }
                .padding(
                    horizontal = dimensions.m,
                    vertical = dimensions.m
                )
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
private fun EditTaskListScreenPreview() {
    val tasks = remember {
        androidx.compose.runtime.mutableStateListOf(
            MainTaskDraft.fromDomain(
                id = 1,
                domain = MainTask(
                    description = "Plan launch",
                    done = false,
                    dueDate = "2026-04-01",
                    recurrence = "",
                    subTasks = listOf(
                        SubTask(description = "Draft checklist", done = true),
                        SubTask(description = "Review copy", done = false)
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
                    tasks = tasks,
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
