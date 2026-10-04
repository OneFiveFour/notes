package net.onefivefour.echolist.ui.edittasklist

import platform.Foundation.NSDateComponents
import platform.Foundation.NSLocale
import platform.Foundation.NSRelativeDateTimeFormatter
import platform.Foundation.NSRelativeDateTimeFormatterStyleNamed
import platform.Foundation.NSRelativeDateTimeFormatterStyleNumeric
import platform.Foundation.NSRelativeDateTimeFormatterUnitsStyleFull

internal actual fun formatRelativeDueDate(value: RelativeDueDate, localeTag: String): String {
    val formatter = NSRelativeDateTimeFormatter().apply {
        locale = NSLocale(localeIdentifier = localeTag)
        unitsStyle = NSRelativeDateTimeFormatterUnitsStyleFull
        dateTimeStyle = if (value.useNamedDay) {
            NSRelativeDateTimeFormatterStyleNamed
        } else {
            NSRelativeDateTimeFormatterStyleNumeric
        }
    }
    val components = NSDateComponents().apply {
        val amount = value.signedAmount.toLong()
        when (value.unit) {
            DueDateUnit.Day -> setDay(amount)
            DueDateUnit.Week -> setWeekOfMonth(amount)
            DueDateUnit.Month -> setMonth(amount)
            DueDateUnit.Year -> setYear(amount)
        }
    }
    return formatter.localizedStringFromDateComponents(components)
}
