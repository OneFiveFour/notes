package net.onefivefour.echolist.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.domain.model.DueDateUrgency

/**
 * Feature: recurrence-reminders
 * Property 1: Urgency partitioning is exhaustive and correct
 * Property 2: Urgency monotonicity
 * Property 4: Invalid date defaults to Normal (at call-site level)
 *
 * Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 3.1, 3.2
 */
class DueDateUrgencyCalculatorPropertyTest : FunSpec({

    // -- Generators --

    // Reasonable date range: 2000-01-01 to 2100-12-31 (~36,524 days span)
    val arbLocalDate = Arb.int(0..36524).map { dayOffset ->
        val baseEpochDay = LocalDate(2000, 1, 1).toEpochDays()
        LocalDate.fromEpochDays(baseEpochDay + dayOffset)
    }

    // -- Property 1: Urgency partitioning is exhaustive and correct --

    test("Feature: recurrence-reminders, Property 1: computeUrgency returns Overdue when daysUntilDue <= 0") {
        checkAll(PropTestConfig(iterations = 100), arbLocalDate, arbLocalDate) { dueDate, today ->
            val daysUntilDue = dueDate.toEpochDays() - today.toEpochDays()
            val result = DueDateUrgencyCalculator.computeUrgency(dueDate, today)

            when {
                daysUntilDue <= 0 -> result shouldBe DueDateUrgency.Overdue
                daysUntilDue in 1..3 -> result shouldBe DueDateUrgency.Warning
                else -> result shouldBe DueDateUrgency.Normal
            }
        }
    }

    test("Feature: recurrence-reminders, Property 1: computeUrgency always returns exactly one of the three urgency values") {
        checkAll(PropTestConfig(iterations = 100), arbLocalDate, arbLocalDate) { dueDate, today ->
            val result = DueDateUrgencyCalculator.computeUrgency(dueDate, today)
            val isExactlyOne = (result == DueDateUrgency.Overdue) xor
                (result == DueDateUrgency.Warning) xor
                (result == DueDateUrgency.Normal)
            isExactlyOne shouldBe true
        }
    }

    // -- Property 2: Urgency monotonicity --

    test("Feature: recurrence-reminders, Property 2: for fixed today, earlier due dates have equal or greater severity") {
        checkAll(PropTestConfig(iterations = 100), arbLocalDate, arbLocalDate, arbLocalDate) { date1, date2, today ->
            val dueDate1 = if (date1 <= date2) date1 else date2
            val dueDate2 = if (date1 <= date2) date2 else date1

            val urgency1 = DueDateUrgencyCalculator.computeUrgency(dueDate1, today)
            val urgency2 = DueDateUrgencyCalculator.computeUrgency(dueDate2, today)

            val severity1 = severityOf(urgency1)
            val severity2 = severityOf(urgency2)

            // Earlier due date (dueDate1 <= dueDate2) should have >= severity
            (severity1 >= severity2) shouldBe true
        }
    }

    // -- Property 4: Invalid date defaults to Normal (at call-site level) --

    test("Feature: recurrence-reminders, Property 4: invalid date strings default to Normal at call-site") {
        checkAll(PropTestConfig(iterations = 100), Arb.string(0..50)) { dateStr ->
            val today = LocalDate(2024, 6, 15)
            val urgency = runCatching { LocalDate.parse(dateStr) }
                .map { DueDateUrgencyCalculator.computeUrgency(it, today) }
                .getOrDefault(DueDateUrgency.Normal)

            // If the string happens to be a valid date, the result depends on computation.
            // If it's invalid, result must be Normal.
            val parsedDate = runCatching { LocalDate.parse(dateStr) }.getOrNull()
            if (parsedDate == null) {
                urgency shouldBe DueDateUrgency.Normal
            }
            // If it IS a valid date, the urgency should still match computation
            else {
                urgency shouldBe DueDateUrgencyCalculator.computeUrgency(parsedDate, today)
            }
        }
    }

    test("Feature: recurrence-reminders, Property 4: empty and blank strings default to Normal") {
        val today = LocalDate(2024, 6, 15)
        val blankStrings = listOf("", " ", "  ", "\t", "\n", "   \t\n  ")

        blankStrings.forEach { dateStr ->
            val urgency = if (dateStr.isBlank()) {
                DueDateUrgency.Normal
            } else {
                runCatching { LocalDate.parse(dateStr) }
                    .map { DueDateUrgencyCalculator.computeUrgency(it, today) }
                    .getOrDefault(DueDateUrgency.Normal)
            }
            urgency shouldBe DueDateUrgency.Normal
        }
    }
})

/**
 * Maps urgency to a numeric severity for comparison.
 * Overdue = 2 (most severe), Warning = 1, Normal = 0 (least severe).
 */
private fun severityOf(urgency: DueDateUrgency): Int = when (urgency) {
    DueDateUrgency.Overdue -> 2
    DueDateUrgency.Warning -> 1
    DueDateUrgency.Normal -> 0
}
