package net.onefivefour.echolist.ui.recurrence

/**
 * Returns true if [input] contains only ASCII digits, or is empty.
 * Used while editing numeric recurrence inputs.
 */
internal fun isEditableNumberInput(input: String): Boolean {
    return input.all { it in '0'..'9' }
}

/**
 * Returns true if [input] is an integer >= 1.
 * Used for week-interval and month-interval inputs.
 */
internal fun isValidPositiveInt(input: Int?): Boolean {
    return input != null && input >= 1
}

/**
 * Returns true if [input] is an integer in [1, 31].
 * Used for day-of-month input.
 */
internal fun isValidDayOfMonth(input: Int?): Boolean {
    return input != null && input in 1..31
}

internal fun RecurrenceState.hasValidDetails(): Boolean {
    return when (this) {
        is RecurrenceState.Off,
        is RecurrenceState.Daily,
        is RecurrenceState.Yearly -> true

        is RecurrenceState.Weekly -> isValidPositiveInt(everyNWeeks)

        is RecurrenceState.Monthly -> isValidPositiveInt(everyNMonths) &&
            isValidDayOfMonth(dayOfMonth)
    }
}
