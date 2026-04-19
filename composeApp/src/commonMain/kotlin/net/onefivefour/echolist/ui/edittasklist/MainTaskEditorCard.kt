package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_delete
import net.onefivefour.echolist.domain.model.MainTask
import net.onefivefour.echolist.domain.model.SubTask
import net.onefivefour.echolist.ui.common.ElTextField
import net.onefivefour.echolist.ui.common.GradientBackground
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MainTaskCard(
    mainTask: UiMainTask,
    isAutoDelete: Boolean,
    onRemoveMainTask: () -> Unit,
    onMainTaskCheckedChange: (Boolean) -> Unit,
    onMainTaskKeyboardAction: (Long) -> Unit,
    onSubTaskCheckedChange: (Int, Boolean) -> Unit,
    onFieldFocusLost: () -> Unit,
    onMainTaskDescriptionFocusChanged: (Boolean) -> Unit,
    onOpenTaskDateSheet: () -> Unit,
    shouldFocusMainTask: Boolean,
    onMainTaskFocusHandled: () -> Unit,
    showAddFirstSubTask: Boolean,
    onAddFirstSubTask: () -> Unit,
    focusedSubTaskId: Long?,
    onSubTaskFocusHandled: () -> Unit,
    onSubTaskKeyboardAction: (Long) -> Unit
) {

    val mainTaskFocusRequester = remember(mainTask.id) { FocusRequester() }

    LaunchedEffect(shouldFocusMainTask) {
        if (shouldFocusMainTask) {
            mainTaskFocusRequester.requestFocus()
            onMainTaskFocusHandled()
        }
    }

    Surface(
        shape = EchoListTheme.shapes.medium,
        color = EchoListTheme.materialColors.surface,
        modifier = Modifier.Companion
            .fillMaxWidth()
            .border(
                width = EchoListTheme.dimensions.borderWidth,
                color = EchoListTheme.materialColors.surfaceVariant,
                shape = EchoListTheme.shapes.medium
            )
    ) {
        Column(
            modifier = Modifier.Companion.padding(EchoListTheme.dimensions.s)
        ) {
            Row(
                verticalAlignment = Alignment.Companion.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
            ) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides EchoListTheme.dimensions.xxl) {
                    Checkbox(
                        checked = mainTask.isDone,
                        onCheckedChange = onMainTaskCheckedChange
                    )
                }

                Column(
                    modifier = Modifier.Companion.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {

                    ElTextField(
                        state = mainTask.descriptionState,
                        style = EchoListTheme.typography.bodyLarge,
                        singleLine = true,
                        imeAction = ImeAction.Next,
                        onKeyboardAction = { onMainTaskKeyboardAction(mainTask.id) },
                        onFocusChanged = onMainTaskDescriptionFocusChanged,
                        onFocusLost = onFieldFocusLost,
                        focusRequester = mainTaskFocusRequester
                    )

                    if (mainTask.dueDateState.text.isNotEmpty()) {
                        DueDateTag(
                            dueDate = mainTask.dueDateState.text.toString(),
                            onClick = onOpenTaskDateSheet
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Set due date",
                        modifier = Modifier.Companion
                            .clip(RoundedCornerShape(50))
                            .clickable { onOpenTaskDateSheet() }
                            .padding(
                                horizontal = EchoListTheme.dimensions.m,
                                vertical = EchoListTheme.dimensions.m
                            )
                    )

                    // Property 6: Delete icon visibility is inverse of isAutoDelete (tasklist-auto-delete)
                    if (!isAutoDelete) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_delete),
                            contentDescription = "Delete main task",
                            modifier = Modifier.Companion
                                .clip(RoundedCornerShape(50))
                                .clickable { onRemoveMainTask() }
                                .padding(
                                    horizontal = EchoListTheme.dimensions.m,
                                    vertical = EchoListTheme.dimensions.m
                                )
                        )
                    }
                }
            }

            if (mainTask.subTasks.isNotEmpty()) {
                Column {
                    mainTask.subTasks.forEachIndexed { subTaskIndex, subTask ->
                        SubTaskRow(
                            subTask = subTask,
                            shouldRequestFocus = focusedSubTaskId == subTask.subTaskId,
                            onFocusHandled = onSubTaskFocusHandled,
                            onKeyboardAction = onSubTaskKeyboardAction,
                            onFocusLost = onFieldFocusLost,
                            onCheckedChange = { isChecked ->
                                onSubTaskCheckedChange(subTaskIndex, isChecked)
                            }
                        )
                    }
                }
            } else if (showAddFirstSubTask) {
                TextButton(onClick = onAddFirstSubTask) {
                    Text(
                        text = "Add first subtask",
                        style = EchoListTheme.typography.labelMedium,
                        color = EchoListTheme.materialColors.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DueDateTag(
    dueDate: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = EchoListTheme.materialColors.surfaceVariant,
        onClick = onClick,
        modifier = Modifier.padding(top = EchoListTheme.dimensions.xs)
    ) {
        Text(
            text = dueDate,
            style = EchoListTheme.typography.labelMedium,
            color = EchoListTheme.materialColors.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = EchoListTheme.dimensions.m,
                vertical = EchoListTheme.dimensions.xs
            )
        )
    }
}


@Preview
@Composable
private fun MainTaskCardPreview() {
    val task = remember {
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
    }
    EchoListTheme {
        GradientBackground {
            MainTaskCard(
                mainTask = task,
                isAutoDelete = false,
                onRemoveMainTask = {},
                onMainTaskCheckedChange = {},
                onMainTaskKeyboardAction = {},
                onSubTaskCheckedChange = { _, _ -> },
                onFieldFocusLost = {},
                onMainTaskDescriptionFocusChanged = {},
                onOpenTaskDateSheet = {},
                shouldFocusMainTask = false,
                onMainTaskFocusHandled = {},
                showAddFirstSubTask = false,
                onAddFirstSubTask = {},
                focusedSubTaskId = null,
                onSubTaskFocusHandled = {},
                onSubTaskKeyboardAction = {}
            )
        }
    }
}

@Preview
@Composable
private fun MainTaskCardEmptyPreview() {
    val task = remember {
        UiMainTask(id = 2)
    }
    EchoListTheme {
        GradientBackground {
            MainTaskCard(
                mainTask = task,
                isAutoDelete = false,
                onRemoveMainTask = {},
                onMainTaskCheckedChange = {},
                onMainTaskKeyboardAction = {},
                onSubTaskCheckedChange = { _, _ -> },
                onFieldFocusLost = {},
                onMainTaskDescriptionFocusChanged = {},
                onOpenTaskDateSheet = {},
                shouldFocusMainTask = false,
                onMainTaskFocusHandled = {},
                showAddFirstSubTask = true,
                onAddFirstSubTask = {},
                focusedSubTaskId = null,
                onSubTaskFocusHandled = {},
                onSubTaskKeyboardAction = {}
            )
        }
    }
}

@Preview
@Composable
private fun MainTaskCardNoDueDatePreview() {
    val task = remember {
        UiMainTask(id = 3, description = "Schedule follow-up")
    }
    EchoListTheme {
        GradientBackground {
            MainTaskCard(
                mainTask = task,
                isAutoDelete = false,
                onRemoveMainTask = {},
                onMainTaskCheckedChange = {},
                onMainTaskKeyboardAction = {},
                onSubTaskCheckedChange = { _, _ -> },
                onFieldFocusLost = {},
                onMainTaskDescriptionFocusChanged = {},
                onOpenTaskDateSheet = {},
                shouldFocusMainTask = false,
                onMainTaskFocusHandled = {},
                showAddFirstSubTask = false,
                onAddFirstSubTask = {},
                focusedSubTaskId = null,
                onSubTaskFocusHandled = {},
                onSubTaskKeyboardAction = {}
            )
        }
    }
}
