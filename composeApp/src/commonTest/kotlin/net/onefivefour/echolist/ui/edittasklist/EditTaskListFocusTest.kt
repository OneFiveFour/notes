package net.onefivefour.echolist.ui.edittasklist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EditTaskListFocusTest : FunSpec({

    test("title action adds the first main task when the list is empty") {
        resolveTitleKeyboardAction(emptyList()) shouldBe KeyboardActionResolution(
            focusTarget = FocusTarget.LastMainTask,
            mutation = KeyboardMutation.AddMainTask
        )
    }

    test("title action clears focus when main tasks already exist") {
        resolveTitleKeyboardAction(
            listOf(mainTask(id = 1L))
        ) shouldBe KeyboardActionResolution(
            shouldClearFocus = true
        )
    }

    test("non-last main task advances focus to the next main task") {
        resolveMainTaskKeyboardAction(
            mainTasks = listOf(
                mainTask(id = 1L, description = "First"),
                mainTask(id = 2L, description = "Second")
            ),
            currentMainTaskId = 1L
        ) shouldBe KeyboardActionResolution(
            focusTarget = FocusTarget.MainTask(mainTaskId = 2L)
        )
    }

    test("last main task appends a new row when it has content") {
        resolveMainTaskKeyboardAction(
            mainTasks = listOf(mainTask(id = 1L, description = "First")),
            currentMainTaskId = 1L
        ) shouldBe KeyboardActionResolution(
            focusTarget = FocusTarget.LastMainTask,
            mutation = KeyboardMutation.AddMainTask
        )
    }

    test("empty last main task is removed and focus is cleared") {
        resolveMainTaskKeyboardAction(
            mainTasks = listOf(mainTask(id = 1L, description = "   ")),
            currentMainTaskId = 1L
        ) shouldBe KeyboardActionResolution(
            mutation = KeyboardMutation.RemoveMainTask(mainTaskId = 1L),
            shouldClearFocus = true
        )
    }

    test("next subtask advances focus to the following subtask") {
        val tasks = listOf(
            mainTask(
                id = 1L,
                subTaskIds = listOf(10L, 11L, 12L)
            )
        )

        resolveSubTaskKeyboardAction(
            mainTasks = tasks,
            mainTaskId = 1L,
            currentSubTaskId = 10L
        ) shouldBe KeyboardActionResolution(
            focusTarget = FocusTarget.SubTask(mainTaskId = 1L, id = 11L)
        )
    }

    test("last subtask requests a new subtask and focuses the appended row") {
        val task = mainTask(
            id = 1L,
            subTasks = listOf(
                UiSubTask(subTaskId = 10L, description = "First"),
                UiSubTask(subTaskId = 11L, description = "Second")
            )
        )

        resolveSubTaskKeyboardAction(
            mainTasks = listOf(task),
            mainTaskId = 1L,
            currentSubTaskId = 11L
        ) shouldBe KeyboardActionResolution(
            focusTarget = FocusTarget.LastSubTask(mainTaskId = 1L),
            mutation = KeyboardMutation.AddSubTask(mainTaskId = 1L)
        )

        task.subTasks.add(UiSubTask(subTaskId = 12L))

        resolveFocusTarget(
            tasks = listOf(task),
            focusTarget = FocusTarget.LastSubTask(mainTaskId = 1L)
        ) shouldBe FocusTarget.SubTask(mainTaskId = 1L, id = 12L)
    }

    test("last subtask target resolves to the first appended subtask when the task was empty") {
        val task = mainTask(id = 1L)

        task.subTasks.add(UiSubTask(subTaskId = 10L))

        resolveFocusTarget(
            tasks = listOf(task),
            focusTarget = FocusTarget.LastSubTask(mainTaskId = 1L)
        ) shouldBe FocusTarget.SubTask(mainTaskId = 1L, id = 10L)
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

    test("empty last subtask is removed and focus is cleared") {
        resolveSubTaskKeyboardAction(
            mainTasks = listOf(
                mainTask(
                    id = 1L,
                    subTasks = listOf(UiSubTask(subTaskId = 10L, description = "   "))
                )
            ),
            mainTaskId = 1L,
            currentSubTaskId = 10L
        ) shouldBe KeyboardActionResolution(
            mutation = KeyboardMutation.RemoveSubTask(mainTaskId = 1L, subTaskId = 10L),
            shouldClearFocus = true
        )
    }
})

private fun mainTask(
    id: Long,
    description: String = "",
    subTaskIds: List<Long> = emptyList(),
    subTasks: List<UiSubTask> = subTaskIds.map(::UiSubTask)
): UiMainTask = UiMainTask(
    id = id,
    description = description,
    subTasks = subTasks
)
