package net.onefivefour.echolist.ui.maintasksettings

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal fun dueDateToUtcMillis(dueDate: String): Long? = runCatching {
    LocalDate.parse(dueDate)
        .atStartOfDayIn(TimeZone.UTC)
        .toEpochMilliseconds()
}.getOrNull()

internal fun utcMillisToDueDate(utcMillis: Long): String =
    Instant.fromEpochMilliseconds(utcMillis)
        .toLocalDateTime(TimeZone.UTC)
        .date
        .toString()
