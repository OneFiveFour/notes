package net.onefivefour.echolist.ui.maintasksettings

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.set
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.ui.recurrence.RecurrenceInterval
import net.onefivefour.echolist.ui.recurrence.RecurrenceState

/**
 * Feature: main-task-settings-screen
 *
 * Property 3: Selecting a date clears recurrence
 * **Validates: Requirements 5.1**
 *
 * Property 4: Selecting a recurrence clears due date
 * **Validates: Requirements 5.2**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainTaskSettingsViewModelPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    fun createViewModel(
        mainTaskId: String,
        dueDate: String,
        recurrence: String
    ): MainTaskSettingsViewModel {
        return MainTaskSettingsViewModel(
            mainTaskId = mainTaskId,
            currentDueDate = dueDate,
            currentRecurrence = recurrence,
            resultBus = MainTaskSettingsResultBus()
        )
    }

    // Generator for non-Off RecurrenceState instances
    val arbDayOfWeek = Arb.of(DayOfWeek.entries)
    val arbDaySet = Arb.set(arbDayOfWeek, 0..7)

    val arbNonOffRecurrenceState: Arb<RecurrenceState> = Arb.of(1, 2, 3, 4).flatMap { variant ->
        when (variant) {
            1 -> arbDaySet.map { days -> RecurrenceState.Daily(selectedDays = days) }
            2 -> Arb.int(1..52).map { n -> RecurrenceState.Weekly(everyNWeeks = n) }
            3 -> Arb.int(1..12).flatMap { n ->
                Arb.int(1..31).map { d -> RecurrenceState.Monthly(everyNMonths = n, dayOfMonth = d) }
            }
            else -> Arb.int(0..0).map { RecurrenceState.Yearly as RecurrenceState }
        }
    }

    // Generator for random LocalDate instances (2000-01-01 to 2099-12-31), yielding UTC millis
    val arbDateMillisAndString: Arb<Pair<Long, String>> = Arb.int(0..36523).map { dayOffset ->
        val baseDate = LocalDate(2000, 1, 1)
        val epochDay = baseDate.toEpochDays() + dayOffset
        val date = LocalDate.fromEpochDays(epochDay)
        val millis = dueDateToUtcMillis(date.toString())!!
        millis to date.toString()
    }

    test("Property 3: Selecting a date preserves recurrence and sets due date to selected date") {
        runTest(testDispatcher) {
            checkAll(
                PropTestConfig(iterations = 100),
                arbNonOffRecurrenceState,
                arbDateMillisAndString
            ) { recurrenceState, (dateMillis, expectedDateString) ->
                val viewModel = createViewModel(
                    mainTaskId = "task-1",
                    dueDate = "",
                    recurrence = recurrenceState.toRrule()
                )

                testScheduler.advanceUntilIdle()

                val readyState = viewModel.uiState.value as MainTaskSettingsUiState.Ready
                readyState.recurrenceState shouldBe recurrenceState

                viewModel.onDateSelected(dateMillis)

                val updatedState = viewModel.uiState.value as MainTaskSettingsUiState.Ready
                updatedState.recurrenceState shouldBe recurrenceState
                updatedState.selectedDueDate shouldBe expectedDateString
            }
        }
    }

    // --- Property 4 generators ---

    val arbDateString: Arb<String> = Arb.int(0..36523).map { dayOffset ->
        val baseDate = LocalDate(2000, 1, 1)
        val epochDay = baseDate.toEpochDays() + dayOffset
        LocalDate.fromEpochDays(epochDay).toString()
    }

    val arbNonOffInterval: Arb<RecurrenceInterval> = Arb.of(
        RecurrenceInterval.Daily,
        RecurrenceInterval.Weekly,
        RecurrenceInterval.Monthly,
        RecurrenceInterval.Yearly
    )

    /**
     * Property 4: Selecting a recurrence preserves due date but sets interval
     * **Validates: Requirements 5.2**
     */
    test("Property 4: Selecting a recurrence preserves due date and sets interval") {
        runTest(testDispatcher) {
            checkAll(
                PropTestConfig(iterations = 100),
                arbDateString,
                arbNonOffInterval
            ) { dateString, interval ->
                val viewModel = createViewModel(
                    mainTaskId = "task-1",
                    dueDate = dateString,
                    recurrence = ""
                )

                testScheduler.advanceUntilIdle()

                val readyState = viewModel.uiState.value as MainTaskSettingsUiState.Ready
                readyState.selectedDueDate shouldBe dateString
                readyState.recurrenceState shouldBe RecurrenceState.Off

                viewModel.onRecurrenceIntervalSelected(interval)

                val updatedState = viewModel.uiState.value as MainTaskSettingsUiState.Ready
                updatedState.selectedDueDate shouldBe dateString
                updatedState.recurrenceState.interval shouldBe interval
            }
        }
    }

    test("Confirming invalid recurrence details keeps the screen in an error state") {
        runTest(testDispatcher) {
            val viewModel = createViewModel(
                mainTaskId = "task-1",
                dueDate = "2026-04-01",
                recurrence = ""
            )

            testScheduler.advanceUntilIdle()

            viewModel.onRecurrenceIntervalSelected(RecurrenceInterval.Weekly)
            viewModel.onRecurrenceDetailChanged(RecurrenceState.Weekly(everyNWeeks = null))

            val readyState = viewModel.uiState.value as MainTaskSettingsUiState.Ready
            readyState.showRecurrenceValidationErrors shouldBe true
        }
    }
})
