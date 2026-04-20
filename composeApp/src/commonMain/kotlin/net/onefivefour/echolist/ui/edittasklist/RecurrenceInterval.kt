package net.onefivefour.echolist.ui.edittasklist

sealed interface RecurrenceInterval {
    val shortLabel: String
    val fullLabel: String

    data object Off : RecurrenceInterval {
        override val shortLabel = "Off"
        override val fullLabel = "Off"
    }

    data object Daily : RecurrenceInterval {
        override val shortLabel = "D"
        override val fullLabel = "Daily"
    }

    data object Weekly : RecurrenceInterval {
        override val shortLabel = "W"
        override val fullLabel = "Weekly"
    }

    data object Monthly : RecurrenceInterval {
        override val shortLabel = "M"
        override val fullLabel = "Monthly"
    }

    data object Yearly : RecurrenceInterval {
        override val shortLabel = "Y"
        override val fullLabel = "Yearly"
    }

    companion object {
        val entries: List<RecurrenceInterval> = listOf(Off, Daily, Weekly, Monthly, Yearly)
    }
}
