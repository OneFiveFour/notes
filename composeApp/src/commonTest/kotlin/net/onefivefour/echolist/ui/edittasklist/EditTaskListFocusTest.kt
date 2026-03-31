package net.onefivefour.echolist.ui.edittasklist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EditTaskListFocusTest : FunSpec({

    test("next subtask advances focus to the following subtask") {
        val tasks = listOf(
            mainTask(
                id = 1L,
                subTaskIds = listOf(10L, 11L, 12L)
            )
        )

        resolveSubTaskAdvance(
            mainTasks = tasks,
            mainTaskId = 1L,
            currentSubTaskId = 10L
        ) shouldBe SubTaskAdvanceResult(
            focusTarget = FocusTarget.SubTask(mainTaskId = 1L, id = 11L),
            shouldAddSubTask = false
        )
    }

    test("last subtask requests a new subtask and focuses the appended row") {
        val task = mainTask(
            id = 1L,
            subTaskIds = listOf(10L, 11L)
        )

        resolveSubTaskAdvance(
            mainTasks = listOf(task),
            mainTaskId = 1L,
            currentSubTaskId = 11L
        ) shouldBe SubTaskAdvanceResult(
            focusTarget = FocusTarget.LastSubTask(mainTaskId = 1L),
            shouldAddSubTask = true
        )

        task.subTasks.add(UiSubTask(subTaskId = 12L))

        resolveFocusTarget(
            tasks = listOf(task),
            focusTarget = FocusTarget.LastSubTask(mainTaskId = 1L)
        ) shouldBe FocusTarget.SubTask(mainTaskId = 1L, id = 12L)
    }

    test("add main task target resolves to the appended main task") {
        val tasks = listOf(
            mainTask(id = 1L),
            mainTask(id = 2L)
        )

        resolveFocusTarget(
            tasks = tasks,
            focusTarget = FocusTarget.LastMainTask
        ) shouldBe FocusTarget.MainTask(mainTaskId = 2L)
    }

    test("main task keyboard flow always resolves to the newest appended main task") {
        val tasks = mutableListOf(
            mainTask(id = 1L)
        )

        tasks.add(mainTask(id = 2L))

        resolveFocusTarget(
            tasks = tasks,
            focusTarget = FocusTarget.LastMainTask
        ) shouldBe FocusTarget.MainTask(mainTaskId = 2L)
    }
})

private fun mainTask(
    id: Long,
    subTaskIds: List<Long> = emptyList()
): UiMainTask = UiMainTask(
    id = id,
    subTasks = subTaskIds.map(::UiSubTask)
)
