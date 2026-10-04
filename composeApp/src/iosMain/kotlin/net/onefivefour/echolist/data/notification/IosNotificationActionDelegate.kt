package net.onefivefour.echolist.data.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.onefivefour.echolist.domain.NotificationScheduler
import net.onefivefour.echolist.domain.completeRecurringTaskFromNotification
import net.onefivefour.echolist.domain.repository.TaskListRepository
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

/**
 * Handles responses to task-reminder notifications on iOS.
 *
 * When the user taps the "Done" action, the associated recurring task is marked
 * complete via [completeRecurringTaskFromNotification], letting the backend
 * compute the next due date and rescheduling the notification.
 *
 * A strong reference to this delegate must be retained for the lifetime of the
 * app (see the wiring in `MainViewController`).
 */
class IosNotificationActionDelegate(
    private val taskListRepository: TaskListRepository,
    private val notificationScheduler: NotificationScheduler
) : NSObject(), UNUserNotificationCenterDelegateProtocol {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit
    ) {
        if (didReceiveNotificationResponse.actionIdentifier != IosNotificationConstants.DONE_ACTION_ID) {
            withCompletionHandler()
            return
        }

        val userInfo = didReceiveNotificationResponse.notification.request.content.userInfo
        val taskId = userInfo[IosNotificationConstants.USER_INFO_TASK_ID] as? String
        val taskListId = userInfo[IosNotificationConstants.USER_INFO_TASK_LIST_ID] as? String

        if (taskId == null || taskListId == null) {
            withCompletionHandler()
            return
        }

        scope.launch {
            try {
                completeRecurringTaskFromNotification(
                    taskListRepository = taskListRepository,
                    notificationScheduler = notificationScheduler,
                    taskListId = taskListId,
                    taskId = taskId
                )
            } finally {
                withCompletionHandler()
            }
        }
    }

    /**
     * Present reminders as banners even while the app is in the foreground so the
     * "Done" action remains reachable.
     */
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: platform.UserNotifications.UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
    ) {
        withCompletionHandler(UNNotificationPresentationOptionBanner)
    }
}
