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
import kotlinx.coroutines.flow.MutableSharedFlow
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
class MainTaskSettingsViewModelPropertyTest : FunSpec({

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

    test("Property 3: Selecting a date clears recurrence — onDateSelected sets recurrence to Off and due date to selected date") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbNonOffRecurrenceState,
            arbDateMillisAndString
        ) { recurrenceState, (dateMillis, expectedDateString) ->
            // Create a ViewModel with a non-Off recurrence and no initial due date
            val viewModel = MainTaskSettingsViewModel(
                mainTaskId = 1L,
                initialDueDate = "",
                initialRecurrence = recurrenceState.toRrule(),
                resultFlow = MutableSharedFlow()
            )

            // Verify the initial recurrence state is not Off
            viewModel.uiState.value.recurrenceState shouldBe recurrenceState

            // Select a date
            viewModel.onDateSelected(dateMillis)

            // Assert recurrence is cleared to Off
            viewModel.uiState.value.recurrenceState shouldBe RecurrenceState.Off

            // Assert due date is set to the selected date
            viewModel.uiState.value.selectedDueDate shouldBe expectedDateString
        }
    }

    // --- Property 4 generators ---

    // Generator for random date strings (2000-01-01 to 2099-12-31) formatted as YYYY-MM-DD
    val arbDateString: Arb<String> = Arb.int(0..36523).map { dayOffset ->
        val baseDate = LocalDate(2000, 1, 1)
        val epochDay = baseDate.toEpochDays() + dayOffset
        LocalDate.fromEpochDays(epochDay).toString()
    }

    // Generator for non-Off RecurrenceInterval
    val arbNonOffInterval: Arb<RecurrenceInterval> = Arb.of(
        RecurrenceInterval.Daily,
        RecurrenceInterval.Weekly,
        RecurrenceInterval.Monthly,
        RecurrenceInterval.Yearly
    )

    /**
     * Property 4: Selecting a recurrence clears due date
     *
     * *For any* `MainTaskSettingsViewModel` whose due date is non-empty, and *for any*
     * recurrence interval other than `Off`, calling `onRecurrenceIntervalSelected(interval)`
     * shall result in the due date state being empty (`""`) and the recurrence state
     * reflecting the selected interval.
     *
     * **Validates: Requirements 5.2**
     */
    test("Property 4: Selecting a recurrence clears due date — onRecurrenceIntervalSelected clears due date and sets interval") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbDateString,
            arbNonOffInterval
        ) { dateString, interval ->
            // Create a ViewModel with a non-empty initial due date and Off recurrence
            val viewModel = MainTaskSettingsViewModel(
                mainTaskId = 1L,
                initialDueDate = dateString,
                initialRecurrence = "",
                resultFlow = MutableSharedFlow()
            )

            // Verify the initial state has the due date set and recurrence Off
            viewModel.uiState.value.selectedDueDate shouldBe dateString
            viewModel.uiState.value.recurrenceState shouldBe RecurrenceState.Off

            // Select a non-Off recurrence interval
            viewModel.onRecurrenceIntervalSelected(interval)

            // Assert due date is cleared
            viewModel.uiState.value.selectedDueDate shouldBe ""

            // Assert recurrence state reflects the selected interval
            viewModel.uiState.value.recurrenceState.interval shouldBe interval
        }
    }
})
