package net.onefivefour.echolist

import androidx.compose.ui.window.ComposeUIViewController
import net.onefivefour.echolist.data.notification.IosNotificationActionDelegate
import net.onefivefour.echolist.di.initKoin
import net.onefivefour.echolist.domain.NotificationScheduler
import net.onefivefour.echolist.domain.repository.TaskListRepository
import org.koin.mp.KoinPlatform
import platform.UserNotifications.UNUserNotificationCenter

// Retained for the lifetime of the app so UNUserNotificationCenter keeps a valid delegate.
private var notificationActionDelegate: IosNotificationActionDelegate? = null

fun mainViewController() = run {
    initKoin()
    registerNotificationActionDelegate()
    ComposeUIViewController { App() }
}

private fun registerNotificationActionDelegate() {
    val koin = KoinPlatform.getKoin()
    val delegate = IosNotificationActionDelegate(
        taskListRepository = koin.get<TaskListRepository>(),
        notificationScheduler = koin.get<NotificationScheduler>()
    )
    notificationActionDelegate = delegate
    UNUserNotificationCenter.currentNotificationCenter().setDelegate(delegate)
}
