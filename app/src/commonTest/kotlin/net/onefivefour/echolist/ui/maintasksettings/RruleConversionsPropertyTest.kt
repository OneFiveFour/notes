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
import kotlinx.datetime.DayOfWeek
import net.onefivefour.echolist.ui.recurrence.RecurrenceState

/**
 * Feature: main-task-settings-screen, Property 1: RRULE round-trip
 *
 * *For any* valid `RecurrenceState` instance, converting it to an RRULE string via `toRrule()`
 * and then parsing it back via `rruleToRecurrenceState()` shall produce a `RecurrenceState`
 * equal to the original.
 *
 * **Validates: Requirements 4.5**
 */
class RruleConversionsPropertyTest : FunSpec({

    val arbDayOfWeek = Arb.of(DayOfWeek.entries)
    val arbDaySet = Arb.set(arbDayOfWeek, 0..7)

    val arbRecurrenceState: Arb<RecurrenceState> = Arb.of(0, 1, 2, 3, 4).flatMap { variant ->
        when (variant) {
            0 -> Arb.int(0..0).map { RecurrenceState.Off as RecurrenceState }
            1 -> arbDaySet.map { days -> RecurrenceState.Daily(selectedDays = days) }
            2 -> Arb.int(1..52).map { n -> RecurrenceState.Weekly(everyNWeeks = n) }
            3 -> Arb.int(1..12).flatMap { n ->
                Arb.int(1..31).map { d -> RecurrenceState.Monthly(everyNMonths = n, dayOfMonth = d) }
            }
            else -> Arb.int(0..0).map { RecurrenceState.Yearly as RecurrenceState }
        }
    }

    test("Property 1: RRULE round-trip — toRrule then rruleToRecurrenceState returns original RecurrenceState") {
        checkAll(PropTestConfig(iterations = 100), arbRecurrenceState) { state ->
            val rrule = state.toRrule()
            rruleToRecurrenceState(rrule) shouldBe state
        }
    }
})
