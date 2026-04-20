package net.onefivefour.echolist.ui.edittasklist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.DayOfWeek

class RecurrencePatternTest : FunSpec({

    test("RecurrenceInterval.entries has exactly 5 elements in order: Off, Daily, Weekly, Monthly, Yearly") {
        RecurrenceInterval.entries shouldBe listOf(
            RecurrenceInterval.Off,
            RecurrenceInterval.Daily,
            RecurrenceInterval.Weekly,
            RecurrenceInterval.Monthly,
            RecurrenceInterval.Yearly
        )
    }

    test("Default RecurrenceState is Off") {
        val defaultState: RecurrenceState = RecurrenceState.Off
        defaultState.interval shouldBe RecurrenceInterval.Off
    }

    test("RecurrenceState.Weekly defaults everyNWeeks to 1") {
        val weekly = RecurrenceState.Weekly()
        weekly.everyNWeeks shouldBe 1
    }

    test("RecurrenceState.Monthly defaults everyNMonths to 1") {
        val monthly = RecurrenceState.Monthly()
        monthly.everyNMonths shouldBe 1
    }

    // --- Task 7.3: TaskDateBottomSheet integration data-level tests ---

    test("Off state is a data object with no configurable fields") {
        val off = RecurrenceState.Off
        off.shouldBeInstanceOf<RecurrenceState.Off>()
        off.interval shouldBe RecurrenceInterval.Off
        // Off is a data object — no constructor parameters, no mutable config
        off shouldBe RecurrenceState.Off
    }

    test("Yearly state is a data object with no configurable fields") {
        val yearly = RecurrenceState.Yearly
        yearly.shouldBeInstanceOf<RecurrenceState.Yearly>()
        yearly.interval shouldBe RecurrenceInterval.Yearly
        // Yearly is a data object — no constructor parameters, no mutable config
        yearly shouldBe RecurrenceState.Yearly
    }

    test("Daily state selectedDays can hold all 7 DayOfWeek values") {
        val allDays = DayOfWeek.entries.toSet()
        allDays.size shouldBe 7

        val daily = RecurrenceState.Daily(selectedDays = allDays)
        daily.selectedDays.size shouldBe 7
        DayOfWeek.entries.forEach { day ->
            (day in daily.selectedDays) shouldBe true
        }
    }

    test("Weekly state has exactly one configurable field: everyNWeeks") {
        val weekly = RecurrenceState.Weekly(everyNWeeks = 3)
        weekly.everyNWeeks shouldBe 3
        weekly.interval shouldBe RecurrenceInterval.Weekly
        // Verify copy works with the single field
        val updated = weekly.copy(everyNWeeks = 5)
        updated.everyNWeeks shouldBe 5
    }

    test("Monthly state has exactly two configurable fields: everyNMonths and dayOfMonth") {
        val monthly = RecurrenceState.Monthly(everyNMonths = 2, dayOfMonth = 15)
        monthly.everyNMonths shouldBe 2
        monthly.dayOfMonth shouldBe 15
        monthly.interval shouldBe RecurrenceInterval.Monthly
        // Verify both fields can be updated independently via copy
        val updatedMonths = monthly.copy(everyNMonths = 6)
        updatedMonths.everyNMonths shouldBe 6
        updatedMonths.dayOfMonth shouldBe 15
        val updatedDay = monthly.copy(dayOfMonth = 28)
        updatedDay.everyNMonths shouldBe 2
        updatedDay.dayOfMonth shouldBe 28
    }

    test("Monthly default dayOfMonth can be set to match a given base date day") {
        // Simulate setting dayOfMonth from a base date's day component (e.g., the 23rd)
        val baseDateDay = 23
        val monthly = RecurrenceState.Monthly(dayOfMonth = baseDateDay)
        monthly.dayOfMonth shouldBe baseDateDay
        monthly.everyNMonths shouldBe 1 // default month interval
    }
})
