package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

internal data class EditTaskListScreenController(
    val blurFocusRequester: FocusRequester,
    val focusedMainTaskId: Long?,
    val resolvedFocusTarget: FocusTarget?,
    val onFocusHandled: () -> Unit,
    val onMainTaskDescriptionFocusChanged: (Long, Boolean) -> Unit,
    val onAddMainTaskAndFocus: () -> Unit,
    val onTitleKeyboardAction: () -> Unit,
    val onMainTaskKeyboardAction: (Long) -> Unit,
    val onSubTaskKeyboardAction: (Long, Long) -> Unit,
    val onAddFirstSubTaskAndFocus: (Long) -> Unit
)

@Composable
internal fun rememberEditTaskListScreenController(
    mainTasks: List<UiMainTask>,
    onAddMainTask: () -> Unit,
    onRemoveMainTask: (Int) -> Unit,
    onAddSubTask: (Int) -> Unit,
    onRemoveSubTask: (Int, Int) -> Unit
): EditTaskListScreenController {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val blurFocusRequester = remember { FocusRequester() }
    var clearFocusRequest by remember { mutableIntStateOf(0) }
    var pendingFocusTarget by remember { mutableStateOf<FocusTarget?>(null) }
    var focusedMainTaskId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(clearFocusRequest) {
        if (clearFocusRequest == 0) return@LaunchedEffect

        // Wait for the composition that removes the focused row, then focus a non-text sink so no
        // other text field picks up focus as a fallback.
        androidx.compose.runtime.withFrameNanos { }
        blurFocusRequester.requestFocus()
        keyboardController?.hide()
    }

    val resolvedFocusTarget = resolveFocusTarget(
        tasks = mainTasks,
        focusTarget = pendingFocusTarget
    )

    val applyKeyboardAction: (KeyboardActionResolution?) -> Unit = { action ->
        if (action != null) {
            if (action.shouldClearFocus) {
                // Clear immediately, then move focus to a non-text sink after the row removal.
                focusManager.clearFocus(force = true)
                blurFocusRequester.requestFocus()
                keyboardController?.hide()
                clearFocusRequest += 1
            }

            pendingFocusTarget = action.focusTarget
            action.mutation.execute(
                mainTasks = mainTasks,
                onAddMainTask = onAddMainTask,
                onRemoveMainTask = onRemoveMainTask,
                onAddSubTask = onAddSubTask,
                onRemoveSubTask = onRemoveSubTask
            )
        }
    }

    return EditTaskListScreenController(
        blurFocusRequester = blurFocusRequester,
        focusedMainTaskId = focusedMainTaskId,
        resolvedFocusTarget = resolvedFocusTarget,
        onFocusHandled = { pendingFocusTarget = null },
        onMainTaskDescriptionFocusChanged = { mainTaskId, isFocused ->
            focusedMainTaskId = if (isFocused) {
                mainTaskId
            } else if (focusedMainTaskId == mainTaskId) {
                null
            } else {
                focusedMainTaskId
            }
        },
        onAddMainTaskAndFocus = {
            applyKeyboardAction(
                KeyboardActionResolution(
                    focusTarget = FocusTarget.LastMainTask,
                    mutation = KeyboardMutation.AddMainTask
                )
            )
        },
        onTitleKeyboardAction = {
            applyKeyboardAction(resolveTitleKeyboardAction(mainTasks))
        },
        onMainTaskKeyboardAction = { mainTaskId ->
            applyKeyboardAction(
                resolveMainTaskKeyboardAction(
                    mainTasks = mainTasks,
                    currentMainTaskId = mainTaskId
                )
            )
        },
        onSubTaskKeyboardAction = { mainTaskId, subTaskId ->
            applyKeyboardAction(
                resolveSubTaskKeyboardAction(
                    mainTasks = mainTasks,
                    mainTaskId = mainTaskId,
                    currentSubTaskId = subTaskId
                )
            )
        },
        onAddFirstSubTaskAndFocus = { mainTaskId ->
            applyKeyboardAction(
                KeyboardActionResolution(
                    focusTarget = FocusTarget.LastSubTask(mainTaskId),
                    mutation = KeyboardMutation.AddSubTask(mainTaskId)
                )
            )
        }
    )
}

private fun KeyboardMutation?.execute(
    mainTasks: List<UiMainTask>,
    onAddMainTask: () -> Unit,
    onRemoveMainTask: (Int) -> Unit,
    onAddSubTask: (Int) -> Unit,
    onRemoveSubTask: (Int, Int) -> Unit
) {
    when (this) {
        null -> Unit
        KeyboardMutation.AddMainTask -> onAddMainTask()
        is KeyboardMutation.RemoveMainTask -> {
            mainTasks.indexOfMainTask(id = mainTaskId)
                ?.let(onRemoveMainTask)
        }

        is KeyboardMutation.AddSubTask -> {
            mainTasks.indexOfMainTask(id = mainTaskId)
                ?.let(onAddSubTask)
        }

        is KeyboardMutation.RemoveSubTask -> {
            mainTasks.findTaskCoordinates(
                mainTaskId = mainTaskId,
                subTaskId = subTaskId
            )?.let { coordinates ->
                onRemoveSubTask(coordinates.mainTaskIndex, coordinates.subTaskIndex)
            }
        }
    }
}

private fun List<UiMainTask>.indexOfMainTask(id: Long): Int? =
    indexOfFirst { it.id == id }
        .takeIf { it != -1 }

private fun List<UiMainTask>.findTaskCoordinates(
    mainTaskId: Long,
    subTaskId: Long
): TaskCoordinates? {
    val mainTaskIndex = indexOfMainTask(id = mainTaskId) ?: return null
    val subTaskIndex = get(mainTaskIndex)
        .subTasks
        .indexOfFirst { it.subTaskId == subTaskId }
        .takeIf { it != -1 }
        ?: return null

    return TaskCoordinates(mainTaskIndex = mainTaskIndex, subTaskIndex = subTaskIndex)
}

private data class TaskCoordinates(
    val mainTaskIndex: Int,
    val subTaskIndex: Int
)
