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

internal fun resolveSubTaskAdvance(
    mainTasks: List<UiMainTask>,
    mainTaskId: Long,
    currentSubTaskId: Long
): SubTaskAdvanceResult? {
    val mainTask = mainTasks.firstOrNull { it.id == mainTaskId } ?: return null
    val currentIndex = mainTask.subTasks.indexOfFirst { it.subTaskId == currentSubTaskId }
    if (currentIndex == -1) return null

    val nextSubTask = mainTask.subTasks.getOrNull(currentIndex + 1)
    return if (nextSubTask != null) {
        SubTaskAdvanceResult(
            focusTarget = FocusTarget.SubTask(mainTaskId, nextSubTask.subTaskId),
            shouldAddSubTask = false
        )
    } else {
        SubTaskAdvanceResult(
            focusTarget = FocusTarget.LastSubTask(mainTaskId),
            shouldAddSubTask = true
        )
    }
}
