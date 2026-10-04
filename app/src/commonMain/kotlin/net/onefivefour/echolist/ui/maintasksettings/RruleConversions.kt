package net.onefivefour.echolist.ui.maintasksettings

import kotlinx.datetime.DayOfWeek
import net.onefivefour.echolist.ui.recurrence.RecurrenceState

private val dayOfWeekToRrule = mapOf(
    DayOfWeek.MONDAY to "MO",
    DayOfWeek.TUESDAY to "TU",
    DayOfWeek.WEDNESDAY to "WE",
    DayOfWeek.THURSDAY to "TH",
    DayOfWeek.FRIDAY to "FR",
    DayOfWeek.SATURDAY to "SA",
    DayOfWeek.SUNDAY to "SU"
)

private val rruleToDayOfWeek = dayOfWeekToRrule.entries.associate { (k, v) -> v to k }

internal fun RecurrenceState.toRrule(): String = when (this) {
    is RecurrenceState.Off -> ""

    is RecurrenceState.Daily -> {
        if (selectedDays.isEmpty()) {
            "FREQ=DAILY"
        } else {
            val byDay = selectedDays
                .sortedBy { it.ordinal }
                .joinToString(",") { dayOfWeekToRrule.getValue(it) }
            "FREQ=DAILY;BYDAY=$byDay"
        }
    }

    is RecurrenceState.Weekly -> {
        require(everyNWeeks != null && everyNWeeks >= 1)
        "FREQ=WEEKLY;INTERVAL=$everyNWeeks"
    }

    is RecurrenceState.Monthly -> {
        require(everyNMonths != null && everyNMonths >= 1)
        require(dayOfMonth != null && dayOfMonth in 1..31)
        "FREQ=MONTHLY;INTERVAL=$everyNMonths;BYMONTHDAY=$dayOfMonth"
    }

    is RecurrenceState.Yearly -> "FREQ=YEARLY"
}

internal fun rruleToRecurrenceState(rrule: String): RecurrenceState {
    if (rrule.isBlank()) return RecurrenceState.Off

    return runCatching {
        val parts = rrule.split(";").associate { part ->
            val (key, value) = part.split("=", limit = 2)
            key to value
        }

        when (parts["FREQ"]) {
            "DAILY" -> {
                val byDay = parts["BYDAY"]
                if (byDay.isNullOrBlank()) {
                    RecurrenceState.Daily()
                } else {
                    val days = byDay.split(",")
                        .mapNotNull { rruleToDayOfWeek[it.trim()] }
                        .toSet()
                    RecurrenceState.Daily(selectedDays = days)
                }
            }

            "WEEKLY" -> {
                val interval = parts["INTERVAL"]?.toIntOrNull() ?: 1
                RecurrenceState.Weekly(everyNWeeks = interval)
            }

            "MONTHLY" -> {
                val interval = parts["INTERVAL"]?.toIntOrNull() ?: 1
                val day = parts["BYMONTHDAY"]?.toIntOrNull() ?: 1
                RecurrenceState.Monthly(everyNMonths = interval, dayOfMonth = day)
            }

            "YEARLY" -> RecurrenceState.Yearly

            else -> RecurrenceState.Off
        }
    }.getOrElse { RecurrenceState.Off }
}
