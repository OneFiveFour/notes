package net.onefivefour.echolist.data.notification

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.coroutines.resume
import kotlin.time.Clock
import net.onefivefour.echolist.domain.NotificationScheduler
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationAction
import platform.UserNotifications.UNNotificationActionOptionNone
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationCategoryOptionHiddenPreviewsShowTitle
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSettings
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS implementation of [NotificationScheduler] using UNUserNotificationCenter.
 *
 * Schedules local notifications via UNCalendarNotificationTrigger at midnight local time.
 * Uses the task ID as notification request identifier for idempotent replacement.
 * Registers a notification category with hiddenPreviewsBodyPlaceholder for lock screen privacy.
 */
class IosNotificationScheduler : NotificationScheduler {

    override suspend fun schedule(
        taskId: String,
        taskListId: String,
        title: String,
        body: String,
        dueDateIso: String
    ) {
        // Parse the due date
        val dueDate = runCatching { LocalDate.parse(dueDateIso) }.getOrNull()
        if (dueDate == null) {
            println("[IosNotificationScheduler] Warning: Cannot parse due date '$dueDateIso' for task $taskId. Skipping.")
            return
        }

        // Skip scheduling if due date is strictly before today
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        if (dueDate < today) {
            println("[IosNotificationScheduler] Warning: Due date $dueDateIso is in the past for task $taskId. Skipping.")
            return
        }

        // Check notification permission
        val settings = getNotificationSettings()
        val authStatus = settings?.authorizationStatus
        if (authStatus != UNAuthorizationStatusAuthorized && authStatus != UNAuthorizationStatusProvisional) {
            println("[IosNotificationScheduler] Warning: Notification permission not granted for task $taskId. Status: $authStatus")
            return
        }

        val center = UNUserNotificationCenter.currentNotificationCenter()

        // Register notification category with a "Done" action and hidden previews placeholder
        val doneAction = UNNotificationAction.actionWithIdentifier(
            identifier = IosNotificationConstants.DONE_ACTION_ID,
            title = IosNotificationConstants.DONE_ACTION_TITLE,
            options = UNNotificationActionOptionNone
        )
        val category = UNNotificationCategory.categoryWithIdentifier(
            identifier = IosNotificationConstants.CATEGORY_ID,
            actions = listOf(doneAction),
            intentIdentifiers = emptyList<Any>(),
            hiddenPreviewsBodyPlaceholder = IosNotificationConstants.HIDDEN_PREVIEW_PLACEHOLDER,
            options = UNNotificationCategoryOptionHiddenPreviewsShowTitle
        )
        center.setNotificationCategories(setOf(category))

        // Create notification content, carrying the ids the "Done" action needs.
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setCategoryIdentifier(IosNotificationConstants.CATEGORY_ID)
            setUserInfo(
                mapOf(
                    IosNotificationConstants.USER_INFO_TASK_ID to taskId,
                    IosNotificationConstants.USER_INFO_TASK_LIST_ID to taskListId
                )
            )
        }

        // Create date components for midnight on the due date
        val dateComponents = NSDateComponents().apply {
            setYear(dueDate.year.toLong())
            @Suppress("DEPRECATION")
            setMonth(dueDate.monthNumber.toLong())
            @Suppress("DEPRECATION")
            setDay(dueDate.dayOfMonth.toLong())
            setHour(0)
            setMinute(0)
            setSecond(0)
        }

        // Create calendar trigger (non-repeating)
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents,
            repeats = false
        )

        // Create request with taskId as identifier (replaces any existing with same ID)
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = taskId,
            content = content,
            trigger = trigger
        )

        // Add the notification request
        suspendCancellableCoroutine { continuation ->
            center.addNotificationRequest(request) { error ->
                if (error != null) {
                    println("[IosNotificationScheduler] Warning: Failed to schedule notification for task $taskId: ${error.localizedDescription}")
                }
                continuation.resume(Unit)
            }
        }
    }

    override suspend fun cancel(taskId: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        // Remove pending (scheduled but not yet fired) notifications
        center.removePendingNotificationRequestsWithIdentifiers(listOf(taskId))
        // Remove already delivered notifications
        center.removeDeliveredNotificationsWithIdentifiers(listOf(taskId))
    }

    /**
     * Bridges the async UNUserNotificationCenter settings callback to a coroutine.
     */
    private suspend fun getNotificationSettings(): UNNotificationSettings? {
        return suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter.currentNotificationCenter()
                .getNotificationSettingsWithCompletionHandler { settings ->
                    continuation.resume(settings)
                }
        }
    }
}
