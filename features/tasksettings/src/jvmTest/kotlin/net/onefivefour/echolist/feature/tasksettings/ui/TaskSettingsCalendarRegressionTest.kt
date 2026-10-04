package net.onefivefour.echolist.feature.tasksettings.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.core.tasks.domain.TaskSettingsResultSink
import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges
import net.onefivefour.echolist.feature.tasksettings.ui.recurrence.RecurrenceState
import net.onefivefour.echolist.testutil.NoOpNotificationPermissionChecker
import net.onefivefour.echolist.testutil.NoOpNotificationPermissionRequester

@OptIn(ExperimentalCoroutinesApi::class)
class TaskSettingsCalendarRegressionTest : FunSpec({
    val dispatcher = StandardTestDispatcher()
    beforeSpec { Dispatchers.setMain(dispatcher) }
    afterSpec { Dispatchers.resetMain() }

    test("selecting a date updates the current calendar and its emitted result restores a new screen") {
        runTest(dispatcher) {
            var result: TaskSettingsChanges? = null
            val sink = TaskSettingsResultSink { result = it }
            val settings = MainTaskSettingsViewModel("draft", "", "", true,
                NoOpNotificationPermissionChecker(), NoOpNotificationPermissionRequester(), sink)
            val selected = dueDateToUtcMillis("2026-08-01")!!
            settings.onDateSelected(selected)
            settings.onRecurrenceDetailChanged(RecurrenceState.Weekly(everyNWeeks = 2))
            testScheduler.advanceUntilIdle()
            (settings.uiState.value as MainTaskSettingsUiState.Ready).initialDateMillis shouldBe selected
            val saved = requireNotNull(result)
            saved.dueDate shouldBe "2026-08-01"
            saved.recurrence shouldBe "FREQ=WEEKLY;INTERVAL=2"
            val reopened = MainTaskSettingsViewModel(saved.mainTaskId, saved.dueDate, saved.recurrence, saved.isNotificationEnabled,
                NoOpNotificationPermissionChecker(), NoOpNotificationPermissionRequester(), sink)
            val state = reopened.uiState.value as MainTaskSettingsUiState.Ready
            state.initialDateMillis shouldBe selected
            state.selectedDueDate shouldBe saved.dueDate
            state.recurrenceState shouldBe RecurrenceState.Weekly(everyNWeeks = 2)
        }
    }
})
