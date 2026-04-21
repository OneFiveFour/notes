package net.onefivefour.echolist.ui.maintasksettings

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import kotlinx.datetime.LocalDate

/**
 * Feature: main-task-settings-screen, Property 2: Date conversion round-trip
 *
 * *For any* valid `YYYY-MM-DD` date string, converting it to UTC milliseconds via
 * `dueDateToUtcMillis()` and back via `utcMillisToDueDate()` shall produce the original
 * date string.
 *
 * **Validates: Requirements 3.1, 4.3**
 */
class DateConversionsPropertyTest : FunSpec({

    val arbDateString: Arb<String> = Arb.int(0..36523).map { dayOffset ->
        val baseDate = LocalDate(2000, 1, 1)
        val epochDay = baseDate.toEpochDays() + dayOffset
        LocalDate.fromEpochDays(epochDay).toString()
    }

    test("Property 2: Date conversion round-trip — dueDateToUtcMillis then utcMillisToDueDate returns original date string") {
        checkAll(PropTestConfig(iterations = 100), arbDateString) { dateString ->
            val millis = dueDateToUtcMillis(dateString)
            millis shouldBe dueDateToUtcMillis(dateString) // non-null check implicit
            utcMillisToDueDate(millis!!) shouldBe dateString
        }
    }
})
