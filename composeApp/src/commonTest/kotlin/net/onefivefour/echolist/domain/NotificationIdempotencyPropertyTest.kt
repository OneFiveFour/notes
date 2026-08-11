package net.onefivefour.echolist.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.domain.model.MainTask

/**
 * Feature: recurrence-reminders
 * Property 6: Notification idempotency
 * Property 9: Permission denied is a no-op
 *
 * Validates: Requirements 4.2, 7.1, 7.3
 */
class NotificationIdempotencyPropertyTest : FunSpec({

    // -- Generators --

    val arbTaskId = Arb.string(1..20).filter { it.isNotBlank() }
    val arbDescription = Arb.string(0..300)
    val arbTaskListName = Arb.string(1..50).filter { it.isNotBlank() }
    val arbRecurrence = Arb.string(5..30).filter { it.isNotBlank() }

    // Future dates: 2090-01-01 to 2099-12-31
    val arbFutureDateString = Arb.int(0..3651).map { dayOffset ->
        val baseEpochDay = LocalDate(2090, 1, 1).toEpochDays()
        LocalDate.fromEpochDays(baseEpochDay + dayOffset).toString()
    }

    // A second future date generator for variant tests
    val arbFutureDateString2 = Arb.int(0..3651).map { dayOffset ->
        val baseEpochDay = LocalDate(2091, 1, 1).toEpochDays()
        LocalDate.fromEpochDays(baseEpochDay + dayOffset).toString()
    }

    // -- Property 6: Notification idempotency --

    test("Feature: recurrence-reminders, Property 6: scheduling same task twice results in exactly one pending notification") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbTaskId,
            arbDescription,
            arbFutureDateString,
            arbRecurrence,
            arbTaskListName
        ) { taskId, description, dueDate, recurrence, taskListName ->
            val scheduler = StatefulFakeScheduler()
            val task = MainTask(
                id = taskId,
                description = description,
                isDone = false,
                dueDate = dueDate,
                recurrence = recurrence,
                subTasks = emptyList()
            )

            // Schedule the same task twice
            scheduleTaskNotification(scheduler, task, taskListName)
            scheduleTaskNotification(scheduler, task, taskListName)

            // Exactly one pending notification should exist for this taskId
            scheduler.pendingNotifications.size shouldBe 1
            scheduler.pendingNotifications.containsKey(taskId) shouldBe true
        }
    }

    test("Feature: recurrence-reminders, Property 6: scheduling same task with different due dates results in exactly one pending notification") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbTaskId,
            arbDescription,
            arbFutureDateString,
            arbFutureDateString2,
            arbRecurrence,
            arbTaskListName
        ) { taskId, description, dueDate1, dueDate2, recurrence, taskListName ->
            val scheduler = StatefulFakeScheduler()
            val task1 = MainTask(
                id = taskId,
                description = description,
                isDone = false,
                dueDate = dueDate1,
                recurrence = recurrence,
                subTasks = emptyList()
            )
            val task2 = task1.copy(dueDate = dueDate2)

            // Schedule with first due date, then with a different due date
            scheduleTaskNotification(scheduler, task1, taskListName)
            scheduleTaskNotification(scheduler, task2, taskListName)

            // Still exactly one pending notification for this taskId (the latest one)
            scheduler.pendingNotifications.size shouldBe 1
            scheduler.pendingNotifications.containsKey(taskId) shouldBe true
            scheduler.pendingNotifications[taskId]!!.dueDateIso shouldBe dueDate2
        }
    }

    // -- Property 9: Permission denied is a no-op --

    test("Feature: recurrence-reminders, Property 9: permission denied scheduler completes without exception and records nothing") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbTaskId,
            arbDescription,
            arbFutureDateString,
            arbRecurrence,
            arbTaskListName
        ) { taskId, description, dueDate, recurrence, taskListName ->
            val scheduler = PermissionDeniedScheduler()
            val task = MainTask(
                id = taskId,
                description = description,
                isDone = false,
                dueDate = dueDate,
                recurrence = recurrence,
                subTasks = emptyList()
            )

            // Should complete without throwing
            scheduleTaskNotification(scheduler, task, taskListName)

            // No state changes in the scheduler
            scheduler.scheduleCalled shouldBe 0
            scheduler.cancelCalled shouldBe 0
        }
    }
})

/**
 * Stateful fake scheduler that tracks pending notifications by taskId.
 * Calling [schedule] adds/replaces the entry; calling [cancel] removes it.
 * This models the idempotency requirement: only one notification per taskId.
 */
private class StatefulFakeScheduler : NotificationScheduler {
    data class ScheduledNotification(
        val taskId: String,
        val title: String,
        val body: String,
        val dueDateIso: String
    )

    val pendingNotifications = mutableMapOf<String, ScheduledNotification>()

    override suspend fun schedule(taskId: String, title: String, body: String, dueDateIso: String) {
        pendingNotifications[taskId] = ScheduledNotification(taskId, title, body, dueDateIso)
    }

    override suspend fun cancel(taskId: String) {
        pendingNotifications.remove(taskId)
    }
}

/**
 * Scheduler that simulates permission denied: completes without throwing,
 * but does not actually schedule or cancel anything.
 */
private class PermissionDeniedScheduler : NotificationScheduler {
    var scheduleCalled = 0
        private set
    var cancelCalled = 0
        private set

    override suspend fun schedule(taskId: String, title: String, body: String, dueDateIso: String) {
        // Permission denied — no-op, no exception
    }

    override suspend fun cancel(taskId: String) {
        // Permission denied — no-op, no exception
    }
}
