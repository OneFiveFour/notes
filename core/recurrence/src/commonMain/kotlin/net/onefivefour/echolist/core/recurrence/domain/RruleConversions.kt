package net.onefivefour.echolist.core.recurrence.domain

import kotlinx.datetime.DayOfWeek

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

fun RecurrenceRule.toRrule(): String = when (this) {
    is RecurrenceRule.Off -> ""

    is RecurrenceRule.Daily -> {
        if (selectedDays.isEmpty()) {
            "FREQ=DAILY"
        } else {
            val byDay = selectedDays
                .sortedBy { it.ordinal }
                .joinToString(",") { dayOfWeekToRrule.getValue(it) }
            "FREQ=DAILY;BYDAY=$byDay"
        }
    }

    is RecurrenceRule.Weekly -> {
        require(everyNWeeks >= 1)
        "FREQ=WEEKLY;INTERVAL=$everyNWeeks"
    }

    is RecurrenceRule.Monthly -> {
        require(everyNMonths >= 1)
        require(dayOfMonth in 1..31)
        "FREQ=MONTHLY;INTERVAL=$everyNMonths;BYMONTHDAY=$dayOfMonth"
    }

    is RecurrenceRule.Yearly -> "FREQ=YEARLY"
}

fun rruleToRecurrenceRule(rrule: String): RecurrenceRule {
    if (rrule.isBlank()) return RecurrenceRule.Off

    return runCatching {
        val parts = rrule.split(";").associate { part ->
            val (key, value) = part.split("=", limit = 2)
            key to value
        }

        when (parts["FREQ"]) {
            "DAILY" -> {
                val byDay = parts["BYDAY"]
                if (byDay.isNullOrBlank()) {
                    RecurrenceRule.Daily()
                } else {
                    val days = byDay.split(",")
                        .mapNotNull { rruleToDayOfWeek[it.trim()] }
                        .toSet()
                    RecurrenceRule.Daily(selectedDays = days)
                }
            }

            "WEEKLY" -> {
                val interval = parts["INTERVAL"]?.toIntOrNull() ?: 1
                RecurrenceRule.Weekly(everyNWeeks = interval)
            }

            "MONTHLY" -> {
                val interval = parts["INTERVAL"]?.toIntOrNull() ?: 1
                val day = parts["BYMONTHDAY"]?.toIntOrNull() ?: 1
                RecurrenceRule.Monthly(everyNMonths = interval, dayOfMonth = day)
            }

            "YEARLY" -> RecurrenceRule.Yearly

            else -> RecurrenceRule.Off
        }
    }.getOrElse { RecurrenceRule.Off }
}