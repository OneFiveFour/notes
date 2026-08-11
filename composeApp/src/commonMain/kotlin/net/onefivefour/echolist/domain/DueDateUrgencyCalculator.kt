package net.onefivefour.echolist.domain

import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.domain.model.DueDateUrgency

/**
 * Pure function that determines the urgency level of a task
 * based on its due date relative to today.
 */
object DueDateUrgencyCalculator {

    /**
     * Computes the urgency for a given [dueDate] relative to [today].
     *
     * @return [DueDateUrgency.Overdue] when the difference is ≤ 0 days,
     *         [DueDateUrgency.Warning] when 1–3 days,
     *         [DueDateUrgency.Normal] when > 3 days.
     */
    fun computeUrgency(dueDate: LocalDate, today: LocalDate): DueDateUrgency {
        val daysUntilDue = dueDate.toEpochDays() - today.toEpochDays()
        return when {
            daysUntilDue <= 0 -> DueDateUrgency.Overdue
            daysUntilDue in 1..3 -> DueDateUrgency.Warning
            else -> DueDateUrgency.Normal
        }
    }
}
