package net.onefivefour.echolist.ui.edittasklist

/**
 * Returns true if [input] parses to an integer ≥ 1.
 * Used for week-interval and month-interval inputs.
 */
internal fun isValidPositiveInt(input: String): Boolean {
    val value = input.toIntOrNull() ?: return false
    return value >= 1
}

/**
 * Returns true if [input] parses to an integer in [1, 31].
 * Used for day-of-month input.
 */
internal fun isValidDayOfMonth(input: String): Boolean {
    val value = input.toIntOrNull() ?: return false
    return value in 1..31
}
