package net.onefivefour.echolist.core.recurrence.domain

import kotlinx.datetime.DayOfWeek

sealed interface RecurrenceRule {

    data object Off : RecurrenceRule

    data class Daily(
        val selectedDays: Set<DayOfWeek> = emptySet()
    ) : RecurrenceRule

    data class Weekly(
        val everyNWeeks: Int = 1
    ) : RecurrenceRule

    data class Monthly(
        val everyNMonths: Int = 1,
        val dayOfMonth: Int = 1
    ) : RecurrenceRule

    data object Yearly : RecurrenceRule
}