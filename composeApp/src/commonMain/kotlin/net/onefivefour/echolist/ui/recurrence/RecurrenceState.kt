package net.onefivefour.echolist.ui.recurrence

import kotlinx.datetime.DayOfWeek

sealed interface RecurrenceState {
    val interval: RecurrenceInterval

    data object Off : RecurrenceState {
        override val interval = RecurrenceInterval.Off
    }

    data class Daily(
        val selectedDays: Set<DayOfWeek> = emptySet()
    ) : RecurrenceState {
        override val interval = RecurrenceInterval.Daily
    }

    data class Weekly(
        val everyNWeeks: Int = 1
    ) : RecurrenceState {
        override val interval = RecurrenceInterval.Weekly
    }

    data class Monthly(
        val everyNMonths: Int = 1,
        val dayOfMonth: Int = 1
    ) : RecurrenceState {
        override val interval = RecurrenceInterval.Monthly
    }

    data object Yearly : RecurrenceState {
        override val interval = RecurrenceInterval.Yearly
    }
}
