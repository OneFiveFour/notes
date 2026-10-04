package net.onefivefour.echolist.domain.model

/**
 * Represents the urgency level of a task based on proximity to its due date.
 */
enum class DueDateUrgency {
    /** Due date is more than 3 days in the future, or no due date set. */
    Normal,
    /** Due date is within the next 3 days (exclusive of today). */
    Warning,
    /** Due date is today or in the past. */
    Overdue
}
