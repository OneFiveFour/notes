package net.onefivefour.echolist.core.notifications.data

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.util.Properties
import net.onefivefour.echolist.core.notifications.database.NotificationDatabase
import net.onefivefour.echolist.core.notifications.domain.Reminder
import net.onefivefour.echolist.core.notifications.domain.ReminderState
import net.onefivefour.echolist.core.notifications.domain.reminderKey

class SqlReminderStoreTest : FunSpec({
    test("preferences, delivery history and checkpoints survive closing and reopening the SQLite database") {
        val file = Files.createTempFile("echolist-reminder-test", ".db")
        val url = "jdbc:sqlite:$file"
        val state = ReminderState(
            activeAccount = "account",
            reminders = listOf(
                Reminder(
                    "account",
                    "task",
                    "list",
                    "Title",
                    "Body",
                    "2026-10-20",
                    deliveredDates = setOf("2026-10-20")
                )
            ),
            preferences = mapOf(reminderKey("account", "task") to false),
            syncDates = mapOf("account" to "2026-10-20")
        )
        try {
            JdbcSqliteDriver(url, Properties(), NotificationDatabase.Schema.synchronous()).use { driver ->
                val store = SqlReminderStore(NotificationDatabase(driver))
                store.read() shouldBe ReminderState()
                store.write(state)
            }
            JdbcSqliteDriver(url, Properties(), NotificationDatabase.Schema.synchronous()).use { driver ->
                SqlReminderStore(NotificationDatabase(driver)).read() shouldBe state
            }
        } finally {
            Files.deleteIfExists(file)
        }
    }
})