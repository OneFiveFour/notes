package net.onefivefour.echolist.core.notifications.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

class ReminderTimingTest : FunSpec({
    test("next local midnight follows a 23-hour day at the spring daylight-saving transition") {
        nextReminderMidnight(Instant.parse("2026-03-28T23:00:00Z"), TimeZone.of("Europe/Berlin")) shouldBe
            Instant.parse("2026-03-29T22:00:00Z")
    }
    test("next local midnight follows a 25-hour day at the autumn daylight-saving transition") {
        nextReminderMidnight(Instant.parse("2026-10-24T22:00:00Z"), TimeZone.of("Europe/Berlin")) shouldBe
            Instant.parse("2026-10-25T23:00:00Z")
    }
    test("midnight already passed means the next calendar day") {
        nextReminderMidnight(Instant.parse("2026-10-20T00:01:00Z"), TimeZone.UTC) shouldBe
            Instant.parse("2026-10-21T00:00:00Z")
    }
})