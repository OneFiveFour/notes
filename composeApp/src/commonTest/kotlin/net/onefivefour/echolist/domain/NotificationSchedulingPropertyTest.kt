package net.onefivefour.echolist.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import net.onefivefour.echolist.domain.model.MainTask

/**
 * Feature: recurrence-reminders
 * Property 5: Schedule and cancel correctness
 * Property 7: Notification content format
 * Property 8: Start-of-day scheduling
 * Property 10: Past date skips scheduling
 *
 * Validates: Requirements 4.1, 4.3, 4.4, 5.1, 5.2, 5.3, 5.4, 5.5, 8.1, 8.2
 */
class NotificationSchedulingPropertyTest : FunSpec({

    // -- Fake NotificationScheduler --

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

    // Past dates: 2000-01-01 to 2019-12-31
    val arbPastDateString = Arb.int(0..7304).map { dayOffset ->
        val baseEpochDay = LocalDate(2000, 1, 1).toEpochDays()
        LocalDate.fromEpochDays(baseEpochDay + dayOffset).toString()
    }

    // -- Unit Tests --

    test("schedule is called when task has valid dueDate and recurrence") {
        val scheduler = FakeNotificationScheduler()
        val task = MainTask(
            id = "task-1",
            description = "Buy groceries",
            isDone = false,
            dueDate = "2095-06-15",
            recurrence = "RRULE:FREQ=DAILY",
            subTasks = emptyList()
        )

        scheduleTaskNotification(scheduler, task, "Shopping List")

        scheduler.scheduleCalls shouldHaveSize 1
        scheduler.cancelCalls.shouldBeEmpty()
        scheduler.scheduleCalls[0].taskId shouldBe "task-1"
        scheduler.scheduleCalls[0].title shouldBe "Task due: Shopping List"
        scheduler.scheduleCalls[0].body shouldBe "Buy groceries"
        scheduler.scheduleCalls[0].dueDateIso shouldBe "2095-06-15"
    }

    test("cancel is called when recurrence is empty") {
        val scheduler = FakeNotificationScheduler()
        val task = MainTask(
            id = "task-2",
            description = "Some task",
            isDone = false,
            dueDate = "2095-06-15",
            recurrence = "",
            subTasks = emptyList()
        )

        scheduleTaskNotification(scheduler, task, "My List")

        scheduler.cancelCalls shouldHaveSize 1
        scheduler.scheduleCalls.shouldBeEmpty()
        scheduler.cancelCalls[0].taskId shouldBe "task-2"
    }

    test("cancel is called when dueDate is empty") {
        val scheduler = FakeNotificationScheduler()
        val task = MainTask(
            id = "task-3",
            description = "Some task",
            isDone = false,
            dueDate = "",
            recurrence = "RRULE:FREQ=WEEKLY",
            subTasks = emptyList()
        )

        scheduleTaskNotification(scheduler, task, "My List")

        scheduler.cancelCalls shouldHaveSize 1
        scheduler.scheduleCalls.shouldBeEmpty()
        scheduler.cancelCalls[0].taskId shouldBe "task-3"
    }

    test("cancel is called when dueDate is blank (whitespace only)") {
        val scheduler = FakeNotificationScheduler()
        val task = MainTask(
            id = "task-4",
            description = "Some task",
            isDone = false,
            dueDate = "   ",
            recurrence = "RRULE:FREQ=DAILY",
            subTasks = emptyList()
        )

        scheduleTaskNotification(scheduler, task, "My List")

        scheduler.cancelCalls shouldHaveSize 1
        scheduler.scheduleCalls.shouldBeEmpty()
    }

    test("past dates skip scheduling - no schedule AND no cancel") {
        val scheduler = FakeNotificationScheduler()
        val task = MainTask(
            id = "task-5",
            description = "Old task",
            isDone = false,
            dueDate = "2010-01-01",
            recurrence = "RRULE:FREQ=DAILY",
            subTasks = emptyList()
        )

        scheduleTaskNotification(scheduler, task, "My List")

        scheduler.scheduleCalls.shouldBeEmpty()
        scheduler.cancelCalls.shouldBeEmpty()
    }

    test("today's date proceeds with scheduling") {
        val scheduler = FakeNotificationScheduler()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val task = MainTask(
            id = "task-6",
            description = "Today task",
            isDone = false,
            dueDate = today.toString(),
            recurrence = "RRULE:FREQ=DAILY",
            subTasks = emptyList()
        )

        scheduleTaskNotification(scheduler, task, "Today List")

        scheduler.scheduleCalls shouldHaveSize 1
        scheduler.cancelCalls.shouldBeEmpty()
        scheduler.scheduleCalls[0].taskId shouldBe "task-6"
    }

    test("body is truncated at 200 characters") {
        val scheduler = FakeNotificationScheduler()
        val longDescription = "A".repeat(250)
        val task = MainTask(
            id = "task-7",
            description = longDescription,
            isDone = false,
            dueDate = "2095-06-15",
            recurrence = "RRULE:FREQ=DAILY",
            subTasks = emptyList()
        )

        scheduleTaskNotification(scheduler, task, "My List")

        scheduler.scheduleCalls shouldHaveSize 1
        scheduler.scheduleCalls[0].body.length shouldBe 200
        scheduler.scheduleCalls[0].body shouldBe "A".repeat(200)
    }

    test("empty description falls back to task list name") {
        val scheduler = FakeNotificationScheduler()
        val task = MainTask(
            id = "task-8",
            description = "",
            isDone = false,
            dueDate = "2095-06-15",
            recurrence = "RRULE:FREQ=DAILY",
            subTasks = emptyList()
        )

        scheduleTaskNotification(scheduler, task, "Shopping List")

        scheduler.scheduleCalls shouldHaveSize 1
        scheduler.scheduleCalls[0].body shouldBe "Shopping List"
    }

    test("cancel is called when dueDate is unparseable (invalid date)") {
        val scheduler = FakeNotificationScheduler()
        val task = MainTask(
            id = "task-9",
            description = "Some task",
            isDone = false,
            dueDate = "not-a-date",
            recurrence = "RRULE:FREQ=DAILY",
            subTasks = emptyList()
        )

        scheduleTaskNotification(scheduler, task, "My List")

        scheduler.cancelCalls shouldHaveSize 1
        scheduler.scheduleCalls.shouldBeEmpty()
        scheduler.cancelCalls[0].taskId shouldBe "task-9"
    }

    // -- Property 5: Schedule and cancel correctness --

    test("Feature: recurrence-reminders, Property 5: non-empty dueDate AND non-empty recurrence triggers schedule") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbTaskId,
            arbDescription,
            arbFutureDateString,
            arbRecurrence,
            arbTaskListName
        ) { taskId, description, dueDate, recurrence, taskListName ->
            val scheduler = FakeNotificationScheduler()
            val task = MainTask(
                id = taskId,
                description = description,
                isDone = false,
                dueDate = dueDate,
                recurrence = recurrence,
                subTasks = emptyList()
            )

            scheduleTaskNotification(scheduler, task, taskListName)

            scheduler.scheduleCalls shouldHaveSize 1
            scheduler.scheduleCalls[0].taskId shouldBe taskId
            scheduler.cancelCalls.shouldBeEmpty()
        }
    }

    test("Feature: recurrence-reminders, Property 5: empty recurrence triggers cancel") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbTaskId,
            arbDescription,
            arbFutureDateString,
            arbTaskListName
        ) { taskId, description, dueDate, taskListName ->
            val scheduler = FakeNotificationScheduler()
            val task = MainTask(
                id = taskId,
                description = description,
                isDone = false,
                dueDate = dueDate,
                recurrence = "",
                subTasks = emptyList()
            )

            scheduleTaskNotification(scheduler, task, taskListName)

            scheduler.cancelCalls shouldHaveSize 1
            scheduler.cancelCalls[0].taskId shouldBe taskId
            scheduler.scheduleCalls.shouldBeEmpty()
        }
    }

    test("Feature: recurrence-reminders, Property 5: empty dueDate triggers cancel") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbTaskId,
            arbDescription,
            arbRecurrence,
            arbTaskListName
        ) { taskId, description, recurrence, taskListName ->
            val scheduler = FakeNotificationScheduler()
            val task = MainTask(
                id = taskId,
                description = description,
                isDone = false,
                dueDate = "",
                recurrence = recurrence,
                subTasks = emptyList()
            )

            scheduleTaskNotification(scheduler, task, taskListName)

            scheduler.cancelCalls shouldHaveSize 1
            scheduler.cancelCalls[0].taskId shouldBe taskId
            scheduler.scheduleCalls.shouldBeEmpty()
        }
    }

    // -- Property 7: Notification content format --

    test("Feature: recurrence-reminders, Property 7: title is 'Task due: {taskListName}'") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbTaskId,
            arbDescription,
            arbFutureDateString,
            arbRecurrence,
            arbTaskListName
        ) { taskId, description, dueDate, recurrence, taskListName ->
            val scheduler = FakeNotificationScheduler()
            val task = MainTask(
                id = taskId,
                description = description,
                isDone = false,
                dueDate = dueDate,
                recurrence = recurrence,
                subTasks = emptyList()
            )

            scheduleTaskNotification(scheduler, task, taskListName)

            scheduler.scheduleCalls shouldHaveSize 1
            scheduler.scheduleCalls[0].title shouldBe "Task due: $taskListName"
        }
    }

    test("Feature: recurrence-reminders, Property 7: body is description (or taskListName if empty), truncated to 200 chars") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbTaskId,
            arbDescription,
            arbFutureDateString,
            arbRecurrence,
            arbTaskListName
        ) { taskId, description, dueDate, recurrence, taskListName ->
            val scheduler = FakeNotificationScheduler()
            val task = MainTask(
                id = taskId,
                description = description,
                isDone = false,
                dueDate = dueDate,
                recurrence = recurrence,
                subTasks = emptyList()
            )

            scheduleTaskNotification(scheduler, task, taskListName)

            scheduler.scheduleCalls shouldHaveSize 1
            val expectedBody = description.ifEmpty { taskListName }.take(200)
            scheduler.scheduleCalls[0].body shouldBe expectedBody
        }
    }

    // -- Property 8: Start-of-day scheduling --

    test("Feature: recurrence-reminders, Property 8: dueDateIso passed to schedule matches task's dueDate") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbTaskId,
            arbDescription,
            arbFutureDateString,
            arbRecurrence,
            arbTaskListName
        ) { taskId, description, dueDate, recurrence, taskListName ->
            val scheduler = FakeNotificationScheduler()
            val task = MainTask(
                id = taskId,
                description = description,
                isDone = false,
                dueDate = dueDate,
                recurrence = recurrence,
                subTasks = emptyList()
            )

            scheduleTaskNotification(scheduler, task, taskListName)

            scheduler.scheduleCalls shouldHaveSize 1
            scheduler.scheduleCalls[0].dueDateIso shouldBe dueDate
        }
    }

    // -- Property 10: Past date skips scheduling --

    test("Feature: recurrence-reminders, Property 10: past dueDate triggers neither schedule nor cancel") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbTaskId,
            arbDescription,
            arbPastDateString,
            arbRecurrence,
            arbTaskListName
        ) { taskId, description, dueDate, recurrence, taskListName ->
            val scheduler = FakeNotificationScheduler()
            val task = MainTask(
                id = taskId,
                description = description,
                isDone = false,
                dueDate = dueDate,
                recurrence = recurrence,
                subTasks = emptyList()
            )

            scheduleTaskNotification(scheduler, task, taskListName)

            scheduler.scheduleCalls.shouldBeEmpty()
            scheduler.cancelCalls.shouldBeEmpty()
        }
    }

    // -- Feature: notification-permission-toggle, Property 5 --

    /**
     * Feature: notification-permission-toggle
     * Property 5: isNotificationEnabled=false → immer cancel
     *
     * For all MainTask instances with isNotificationEnabled = false, regardless of
     * dueDate and recurrence values, scheduleTaskNotification MUST call
     * scheduler.cancel(task.id) and MUST NOT call scheduler.schedule(...).
     *
     * Validates: Requirements 5.1
     */
    test("Feature: notification-permission-toggle, Property 5: isNotificationEnabled=false → immer cancel") {
        // Generator for dueDate: mix of future dates, past dates, blank strings, and invalid strings
        val arbAnyDueDate = Arb.choice(
            arbFutureDateString,
            arbPastDateString,
            Arb.constant(""),
            Arb.constant("   "),
            Arb.of("not-a-date", "2099-13-45", "abc", "12/31/2095")
        )

        // Generator for recurrence: mix of valid recurrence strings and blank strings
        val arbAnyRecurrence = Arb.choice(
            arbRecurrence,
            Arb.constant(""),
            Arb.constant("   ")
        )

        checkAll(
            PropTestConfig(iterations = 100),
            arbTaskId,
            arbDescription,
            arbAnyDueDate,
            arbAnyRecurrence,
            arbTaskListName
        ) { taskId, description, dueDate, recurrence, taskListName ->
            val scheduler = FakeNotificationScheduler()
            val task = MainTask(
                id = taskId,
                description = description,
                isDone = false,
                dueDate = dueDate,
                recurrence = recurrence,
                isNotificationEnabled = false,
                subTasks = emptyList()
            )

            scheduleTaskNotification(scheduler, task, taskListName)

            scheduler.cancelCalls shouldHaveSize 1
            scheduler.cancelCalls[0].taskId shouldBe taskId
            scheduler.scheduleCalls.shouldBeEmpty()
        }
    }
})

/**
 * Fake implementation of [NotificationScheduler] that records all calls for verification.
 */
private class FakeNotificationScheduler : NotificationScheduler {
    data class ScheduleCall(val taskId: String, val title: String, val body: String, val dueDateIso: String)
    data class CancelCall(val taskId: String)

    val scheduleCalls = mutableListOf<ScheduleCall>()
    val cancelCalls = mutableListOf<CancelCall>()

    override suspend fun schedule(taskId: String, title: String, body: String, dueDateIso: String) {
        scheduleCalls.add(ScheduleCall(taskId, title, body, dueDateIso))
    }

    override suspend fun cancel(taskId: String) {
        cancelCalls.add(CancelCall(taskId))
    }
}
