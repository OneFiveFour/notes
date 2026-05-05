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
            listOf(mainTask(id = "1"))
        ) shouldBe KeyboardActionResolution(
            shouldClearFocus = true
        )
    }

    test("non-last main task advances focus to the next main task") {
        resolveMainTaskKeyboardAction(
            mainTasks = listOf(
                mainTask(id = "1", description = "First"),
                mainTask(id = "2", description = "Second")
            ),
            currentMainTaskId = "1"
        ) shouldBe KeyboardActionResolution(
            focusTarget = FocusTarget.MainTask(mainTaskId = "2")
        )
    }

    test("last main task appends a new row when it has content") {
        resolveMainTaskKeyboardAction(
            mainTasks = listOf(mainTask(id = "1", description = "First")),
            currentMainTaskId = "1"
        ) shouldBe KeyboardActionResolution(
            focusTarget = FocusTarget.LastMainTask,
            mutation = KeyboardMutation.AddMainTask
        )
    }

    test("empty last main task is removed and focus is cleared") {
        resolveMainTaskKeyboardAction(
            mainTasks = listOf(mainTask(id = "1", description = "   ")),
            currentMainTaskId = "1"
        ) shouldBe KeyboardActionResolution(
            mutation = KeyboardMutation.RemoveMainTask(mainTaskId = "1"),
            shouldClearFocus = true
        )
    }

    test("next subtask advances focus to the following subtask") {
        val tasks = listOf(
            mainTask(
                id = "1",
                subTaskIds = listOf("10", "11", "12")
            )
        )

        resolveSubTaskKeyboardAction(
            mainTasks = tasks,
            mainTaskId = "1",
            currentSubTaskId = "10"
        ) shouldBe KeyboardActionResolution(
            focusTarget = FocusTarget.SubTask(mainTaskId = "1", id = "11")
        )
    }

    test("last subtask requests a new subtask and focuses the appended row") {
        val task = mainTask(
            id = "1",
            subTasks = listOf(
                UiSubTask(id = "10", description = "First"),
                UiSubTask(id = "11", description = "Second")
            )
        )

        resolveSubTaskKeyboardAction(
            mainTasks = listOf(task),
            mainTaskId = "1",
            currentSubTaskId = "11"
        ) shouldBe KeyboardActionResolution(
            focusTarget = FocusTarget.LastSubTask(mainTaskId = "1"),
            mutation = KeyboardMutation.AddSubTask(mainTaskId = "1")
        )

        task.subTasks.add(UiSubTask(id = "12"))

        resolveFocusTarget(
            tasks = listOf(task),
            focusTarget = FocusTarget.LastSubTask(mainTaskId = "1")
        ) shouldBe FocusTarget.SubTask(mainTaskId = "1", id = "12")
    }

    test("last subtask target resolves to the first appended subtask when the task was empty") {
        val task = mainTask(id = "1")

        task.subTasks.add(UiSubTask(id = "10"))

        resolveFocusTarget(
            tasks = listOf(task),
            focusTarget = FocusTarget.LastSubTask(mainTaskId = "1")
        ) shouldBe FocusTarget.SubTask(mainTaskId = "1", id = "10")
    }

    test("add main task target resolves to the appended main task") {
        val tasks = listOf(
            mainTask(id = "1"),
            mainTask(id = "2")
        )

        resolveFocusTarget(
            tasks = tasks,
            focusTarget = FocusTarget.LastMainTask
        ) shouldBe FocusTarget.MainTask(mainTaskId = "2")
    }

    test("main task keyboard flow always resolves to the newest appended main task") {
        val tasks = mutableListOf(
            mainTask(id = "1")
        )

        tasks.add(mainTask(id = "2"))

        resolveFocusTarget(
            tasks = tasks,
            focusTarget = FocusTarget.LastMainTask
        ) shouldBe FocusTarget.MainTask(mainTaskId = "2")
    }

    test("empty last subtask is removed and focus is cleared") {
        resolveSubTaskKeyboardAction(
            mainTasks = listOf(
                mainTask(
                    id = "1",
                    subTasks = listOf(UiSubTask(id = "10", description = "   "))
                )
            ),
            mainTaskId = "1",
            currentSubTaskId = "10"
        ) shouldBe KeyboardActionResolution(
            mutation = KeyboardMutation.RemoveSubTask(mainTaskId = "1", subTaskId = "10"),
            shouldClearFocus = true
        )
    }
})

private fun mainTask(
    id: String,
    description: String = "",
    subTaskIds: List<String> = emptyList(),
    subTasks: List<UiSubTask> = subTaskIds.map { UiSubTask(id = it) }
): UiMainTask = UiMainTask(
    id = id,
    description = description,
    subTasks = subTasks
)
