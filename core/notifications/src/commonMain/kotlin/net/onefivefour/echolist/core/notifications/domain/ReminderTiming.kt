package net.onefivefour.echolist.core.notifications.domain

import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** Uses the next calendar day, rather than adding 24 hours across a daylight-saving transition. */
fun nextReminderMidnight(now: Instant, zone: TimeZone): Instant =
    (now.toLocalDateTime(zone).date + DatePeriod(days = 1)).atStartOfDayIn(zone)