package net.onefivefour.echolist.ui.edittasklist

internal fun resolveFocusTarget(
    tasks: List<UiMainTask>,
    focusTarget: FocusTarget?
): FocusTarget? = when (focusTarget) {
    null -> null
    is FocusTarget.MainTask -> focusTarget.takeIf { task ->
        tasks.any { it.id == task.mainTaskId }
    }

    is FocusTarget.SubTask -> focusTarget.takeIf { subTask ->
        tasks.any { task ->
            task.id == subTask.mainTaskId && task.subTasks.any { it.subTaskId == subTask.id }
        }
    }

    FocusTarget.LastMainTask -> tasks.lastOrNull()?.let { task ->
        FocusTarget.MainTask(task.id)
    }

    is FocusTarget.LastSubTask -> tasks
        .firstOrNull { it.id == focusTarget.mainTaskId }
        ?.subTasks
        ?.lastOrNull()
        ?.let { subTask ->
            FocusTarget.SubTask(focusTarget.mainTaskId, subTask.subTaskId)
        }
}

internal fun resolveTitleKeyboardAction(mainTasks: List<UiMainTask>): KeyboardActionResolution =
    if (mainTasks.isEmpty()) {
        KeyboardActionResolution(
            focusTarget = FocusTarget.LastMainTask,
            mutation = KeyboardMutation.AddMainTask
        )
    } else {
        KeyboardActionResolution(shouldClearFocus = true)
    }

internal fun resolveMainTaskKeyboardAction(
    mainTasks: List<UiMainTask>,
    currentMainTaskId: Long
): KeyboardActionResolution? {
    val currentIndex = mainTasks.indexOfFirst { it.id == currentMainTaskId }
    if (currentIndex == -1) return null

    val nextMainTask = mainTasks.getOrNull(currentIndex + 1)
    if (nextMainTask != null) {
        return KeyboardActionResolution(
            focusTarget = FocusTarget.MainTask(nextMainTask.id)
        )
    }

    return if (mainTasks[currentIndex].descriptionState.isBlank()) {
        KeyboardActionResolution(
            mutation = KeyboardMutation.RemoveMainTask(currentMainTaskId),
            shouldClearFocus = true
        )
    } else {
        KeyboardActionResolution(
            focusTarget = FocusTarget.LastMainTask,
            mutation = KeyboardMutation.AddMainTask
        )
    }
}

internal fun resolveSubTaskKeyboardAction(
    mainTasks: List<UiMainTask>,
    mainTaskId: Long,
    currentSubTaskId: Long
): KeyboardActionResolution? {
    val mainTask = mainTasks.firstOrNull { it.id == mainTaskId } ?: return null
    val currentIndex = mainTask.subTasks.indexOfFirst { it.subTaskId == currentSubTaskId }
    if (currentIndex == -1) return null

    val nextSubTask = mainTask.subTasks.getOrNull(currentIndex + 1)
    if (nextSubTask != null) {
        return KeyboardActionResolution(
            focusTarget = FocusTarget.SubTask(mainTaskId, nextSubTask.subTaskId)
        )
    }

    return if (mainTask.subTasks[currentIndex].descriptionState.isBlank()) {
        KeyboardActionResolution(
            mutation = KeyboardMutation.RemoveSubTask(mainTaskId, currentSubTaskId),
            shouldClearFocus = true
        )
    } else {
        KeyboardActionResolution(
            focusTarget = FocusTarget.LastSubTask(mainTaskId),
            mutation = KeyboardMutation.AddSubTask(mainTaskId)
        )
    }
}

private fun androidx.compose.foundation.text.input.TextFieldState.isBlank(): Boolean =
    text.toString().trim().isBlank()
