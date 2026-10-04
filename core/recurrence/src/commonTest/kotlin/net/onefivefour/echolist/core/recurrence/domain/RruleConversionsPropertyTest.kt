package net.onefivefour.echolist.core.recurrence.domain

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

/**
 * Feature: main-task-settings-screen, Property 1: RRULE round-trip
 *
 * *For any* valid `RecurrenceRule` instance, converting it to an RRULE string via `toRrule()`
 * and then parsing it back via `rruleToRecurrenceRule()` shall produce a `RecurrenceRule`
 * equal to the original.
 *
 * **Validates: Requirements 4.5**
 */
class RruleConversionsPropertyTest : FunSpec({

    val arbDayOfWeek = Arb.of(DayOfWeek.entries)
    val arbDaySet = Arb.set(arbDayOfWeek, 0..7)

    val arbRecurrenceRule: Arb<RecurrenceRule> = Arb.of(0, 1, 2, 3, 4).flatMap { variant ->
        when (variant) {
            0 -> Arb.int(0..0).map { RecurrenceRule.Off as RecurrenceRule }
            1 -> arbDaySet.map { days -> RecurrenceRule.Daily(selectedDays = days) }
            2 -> Arb.int(1..52).map { n -> RecurrenceRule.Weekly(everyNWeeks = n) }
            3 -> Arb.int(1..12).flatMap { n ->
                Arb.int(1..31).map { d -> RecurrenceRule.Monthly(everyNMonths = n, dayOfMonth = d) }
            }
            else -> Arb.int(0..0).map { RecurrenceRule.Yearly as RecurrenceRule }
        }
    }

    test("Property 1: RRULE round-trip — toRrule then rruleToRecurrenceRule returns original RecurrenceRule") {
        checkAll(PropTestConfig(iterations = 100), arbRecurrenceRule) { state ->
            val rrule = state.toRrule()
            rruleToRecurrenceRule(rrule) shouldBe state
        }
    }
})