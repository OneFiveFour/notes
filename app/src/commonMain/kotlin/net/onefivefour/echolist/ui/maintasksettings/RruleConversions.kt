package net.onefivefour.echolist.ui.maintasksettings

import net.onefivefour.echolist.core.recurrence.domain.RecurrenceRule
import net.onefivefour.echolist.core.recurrence.domain.rruleToRecurrenceRule
import net.onefivefour.echolist.core.recurrence.domain.toRrule
import net.onefivefour.echolist.ui.recurrence.RecurrenceState

internal fun RecurrenceState.toRrule(): String = when (this) {
    RecurrenceState.Off -> RecurrenceRule.Off
    is RecurrenceState.Daily -> RecurrenceRule.Daily(selectedDays)
    is RecurrenceState.Weekly -> RecurrenceRule.Weekly(requireNotNull(everyNWeeks))
    is RecurrenceState.Monthly -> RecurrenceRule.Monthly(requireNotNull(everyNMonths), requireNotNull(dayOfMonth))
    RecurrenceState.Yearly -> RecurrenceRule.Yearly
}.toRrule()

internal fun rruleToRecurrenceState(rrule: String): RecurrenceState = when (val rule = rruleToRecurrenceRule(rrule)) {
    RecurrenceRule.Off -> RecurrenceState.Off
    is RecurrenceRule.Daily -> RecurrenceState.Daily(rule.selectedDays)
    is RecurrenceRule.Weekly -> RecurrenceState.Weekly(rule.everyNWeeks)
    is RecurrenceRule.Monthly -> RecurrenceState.Monthly(rule.everyNMonths, rule.dayOfMonth)
    RecurrenceRule.Yearly -> RecurrenceState.Yearly
}
