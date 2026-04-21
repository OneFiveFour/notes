package net.onefivefour.echolist.ui.edittasklist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.collection
import kotlinx.datetime.DayOfWeek
import net.onefivefour.echolist.ui.recurrence.RecurrenceInterval
import net.onefivefour.echolist.ui.recurrence.RecurrenceState
import net.onefivefour.echolist.ui.recurrence.isValidDayOfMonth
import net.onefivefour.echolist.ui.recurrence.isValidPositiveInt
import net.onefivefour.echolist.ui.recurrence.monthlyFormatString
import net.onefivefour.echolist.ui.recurrence.weeklyFormatString

/**
 * Returns whether the given [state] treats the date as a base date (recurrence is active).
 * When the state is [RecurrenceState.Off], the date is a simple due date (returns false).
 * For any non-Off variant, the date is a base date for recurrence calculation (returns true).
 */
internal fun isBaseDate(state: RecurrenceState): Boolean {
    return state !is RecurrenceState.Off
}

/**
 * Returns the display label for [interval] given the currently [selected] interval.
 * When [interval] is the selected one, its [RecurrenceInterval.fullLabel] is shown;
 * otherwise its [RecurrenceInterval.shortLabel] is shown.
 */
internal fun displayLabel(interval: RecurrenceInterval, selected: RecurrenceInterval): String {
    return if (interval == selected) interval.fullLabel else interval.shortLabel
}

/**
 * Toggles the presence of [day] in [days].
 * If [day] is present, it is removed; if absent, it is added.
 * This mirrors the DailyDetailContent's onDayToggled callback behavior.
 */
internal fun toggleDay(days: Set<DayOfWeek>, day: DayOfWeek): Set<DayOfWeek> {
    return if (day in days) days - day else days + day
}

/**
 * Feature: recurrence-pattern-picker, Property 1: Label mapping correctness
 *
 * *For any* `RecurrenceInterval` in the entries list, its `shortLabel` and `fullLabel` must match
 * the specification: Off→("Off","Off"), Daily→("D","Daily"), Weekly→("W","Weekly"),
 * Monthly→("M","Monthly"), Yearly→("Y","Yearly").
 *
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**
 */
class RecurrencePatternPropertyTest : FunSpec({

    val expectedLabels = mapOf(
        RecurrenceInterval.Off to ("Off" to "Off"),
        RecurrenceInterval.Daily to ("D" to "Daily"),
        RecurrenceInterval.Weekly to ("W" to "Weekly"),
        RecurrenceInterval.Monthly to ("M" to "Monthly"),
        RecurrenceInterval.Yearly to ("Y" to "Yearly")
    )

    test("Property 1: Label mapping correctness — shortLabel and fullLabel match spec for every interval") {
        checkAll(Exhaustive.collection(RecurrenceInterval.entries)) { interval ->
            val (expectedShort, expectedFull) = expectedLabels.getValue(interval)
            interval.shortLabel shouldBe expectedShort
            interval.fullLabel shouldBe expectedFull
        }
    }

    /**
     * Feature: recurrence-pattern-picker, Property 2: Selected vs unselected label display
     *
     * *For any* `RecurrenceInterval` chosen as the selected interval, the [displayLabel] function
     * must return `fullLabel` for that interval and `shortLabel` for every other interval in the
     * entries list.
     *
     * **Validates: Requirements 1.2, 1.3**
     */
    test("Property 2: Selected vs unselected label display — fullLabel for selected, shortLabel for others") {
        checkAll(Exhaustive.collection(RecurrenceInterval.entries)) { selected ->
            RecurrenceInterval.entries.forEach { interval ->
                val expected = if (interval == selected) interval.fullLabel else interval.shortLabel
                displayLabel(interval, selected) shouldBe expected
            }
        }
    }

    /**
     * Feature: recurrence-pattern-picker, Property 7: Positive integer validation
     *
     * *For any* string input, the validator [isValidPositiveInt] accepts it if and only if
     * the string parses to an integer ≥ 1. This validator is shared by the week interval
     * and month interval inputs.
     *
     * **Validates: Requirements 6.3, 7.3**
     */
    test("Property 7: Positive integer validation — accepts iff string parses to integer >= 1") {
        // 1. Random positive integers (as strings) → should return true
        checkAll(PropTestConfig(iterations = 100), Arb.int(1..Int.MAX_VALUE)) { n ->
            isValidPositiveInt(n.toString()) shouldBe true
        }

        // 2. Random non-positive integers (0, negatives as strings) → should return false
        checkAll(PropTestConfig(iterations = 100), Arb.int(Int.MIN_VALUE..0)) { n ->
            isValidPositiveInt(n.toString()) shouldBe false
        }

        // 3. Random non-numeric strings → should return false
        checkAll(PropTestConfig(iterations = 100), Arb.string(1..20)) { s ->
            if (s.toIntOrNull() == null || s.toIntOrNull()!! < 1) {
                isValidPositiveInt(s) shouldBe false
            } else {
                isValidPositiveInt(s) shouldBe true
            }
        }

        // 4. Empty string → should return false
        isValidPositiveInt("") shouldBe false
    }

    /**
     * Feature: recurrence-pattern-picker, Property 8: Day-of-month range validation
     *
     * *For any* integer input, the day-of-month validator [isValidDayOfMonth] accepts it
     * if and only if the value is in the range [1, 31] inclusive.
     *
     * **Validates: Requirements 7.4**
     */
    test("Property 8: Day-of-month range validation — accepts iff value is in [1, 31]") {
        // 1. Integers in [1, 31] (as strings) → should return true
        checkAll(PropTestConfig(iterations = 100), Arb.int(1..31)) { n ->
            isValidDayOfMonth(n.toString()) shouldBe true
        }

        // 2. Integers outside [1, 31] (negatives, 0, 32+) → should return false
        checkAll(PropTestConfig(iterations = 100), Arb.int(Int.MIN_VALUE..0)) { n ->
            isValidDayOfMonth(n.toString()) shouldBe false
        }
        checkAll(PropTestConfig(iterations = 100), Arb.int(32..Int.MAX_VALUE)) { n ->
            isValidDayOfMonth(n.toString()) shouldBe false
        }

        // 3. Non-numeric strings → should return false
        checkAll(PropTestConfig(iterations = 100), Arb.string(1..20)) { s ->
            if (s.toIntOrNull() == null || s.toIntOrNull()!! !in 1..31) {
                isValidDayOfMonth(s) shouldBe false
            } else {
                isValidDayOfMonth(s) shouldBe true
            }
        }

        // 4. Empty string → should return false
        isValidDayOfMonth("") shouldBe false
    }

    /**
     * Feature: recurrence-pattern-picker, Property 4: Weekday toggle symmetric set operation
     *
     * *For any* `Set<DayOfWeek>` and any `DayOfWeek`, toggling that day should produce a new set
     * where: if the day was present it is now absent, and if it was absent it is now present.
     * The resulting set size differs from the original by exactly 1.
     *
     * **Validates: Requirements 5.3**
     */
    test("Property 4: Weekday toggle symmetric set operation — presence is flipped and size differs by exactly 1") {
        val arbDayOfWeek = Arb.of(DayOfWeek.entries)
        val arbDaySet = Arb.set(arbDayOfWeek, 0..7)

        checkAll(PropTestConfig(iterations = 100), arbDaySet, arbDayOfWeek) { days, day ->
            val toggled = toggleDay(days, day)

            if (day in days) {
                // Day was present → should now be absent
                (day in toggled) shouldBe false
            } else {
                // Day was absent → should now be present
                (day in toggled) shouldBe true
            }

            // Size must differ by exactly 1
            kotlin.math.abs(toggled.size - days.size) shouldBe 1
        }
    }

    /**
     * Feature: recurrence-pattern-picker, Property 5: Weekly format string correctness
     *
     * *For any* positive integer `n`, the display text returned by [weeklyFormatString] must
     * equal `"Every $n week(s)"`.
     *
     * **Validates: Requirements 6.2**
     */
    test("Property 5: Weekly format string correctness — output matches 'Every n week(s)' for any positive integer") {
        checkAll(PropTestConfig(iterations = 100), Arb.int(1..1000)) { n ->
            weeklyFormatString(n) shouldBe "Every $n week(s)"
        }
    }

    /**
     * Feature: recurrence-pattern-picker, Property 6: Monthly format string correctness
     *
     * *For any* positive integer `n` and any integer `m` in [1, 31], the display text returned
     * by [monthlyFormatString] must equal `"Every $n month(s) on the ${m}th day"`.
     *
     * **Validates: Requirements 7.2**
     */
    test("Property 6: Monthly format string correctness — output matches 'Every n month(s) on the mth day' for any positive integer and day") {
        checkAll(PropTestConfig(iterations = 100), Arb.int(1..1000), Arb.int(1..31)) { n, m ->
            monthlyFormatString(n, m) shouldBe "Every $n month(s) on the ${m}th day"
        }
    }

    /**
     * Feature: recurrence-pattern-picker, Property 3: Date semantics depend on recurrence state
     *
     * *For any* `RecurrenceState`, if the state is `Off` then the date represents a due date
     * (recurrence is empty / [isBaseDate] returns false), and if the state is any non-Off variant
     * then the date represents a base date (recurrence is populated / [isBaseDate] returns true).
     *
     * **Validates: Requirements 3.3, 3.4**
     */
    test("Property 3: Date semantics depend on recurrence state — Off means due date, non-Off means base date") {
        val arbDayOfWeek = Arb.of(DayOfWeek.entries)
        val arbDaySet = Arb.set(arbDayOfWeek, 0..7)

        // Arb that generates all 5 RecurrenceState variants with random config data
        val arbRecurrenceState: Arb<RecurrenceState> = Arb.of(0, 1, 2, 3, 4).flatMap { variant ->
            when (variant) {
                0 -> Arb.int(0..0).map { RecurrenceState.Off as RecurrenceState }
                1 -> arbDaySet.map { days -> RecurrenceState.Daily(selectedDays = days) }
                2 -> Arb.int(1..1000).map { n -> RecurrenceState.Weekly(everyNWeeks = n) }
                3 -> Arb.int(1..1000).flatMap { n ->
                    Arb.int(1..31).map { d -> RecurrenceState.Monthly(everyNMonths = n, dayOfMonth = d) }
                }
                else -> Arb.int(0..0).map { RecurrenceState.Yearly as RecurrenceState }
            }
        }

        checkAll(PropTestConfig(iterations = 100), arbRecurrenceState) { state ->
            when (state) {
                is RecurrenceState.Off -> {
                    // Off → date is a due date, not a base date
                    isBaseDate(state) shouldBe false
                }
                is RecurrenceState.Daily,
                is RecurrenceState.Weekly,
                is RecurrenceState.Monthly,
                is RecurrenceState.Yearly -> {
                    // Non-Off → date is a base date for recurrence
                    isBaseDate(state) shouldBe true
                }
            }
        }
    }
})
