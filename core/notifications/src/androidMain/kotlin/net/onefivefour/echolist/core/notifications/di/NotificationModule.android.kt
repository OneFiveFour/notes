package net.onefivefour.echolist.core.notifications.di

import net.onefivefour.echolist.core.notifications.data.AndroidNotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.data.AndroidNotificationPermissionRequester
import net.onefivefour.echolist.core.notifications.data.AndroidNotificationScheduler
import net.onefivefour.echolist.core.notifications.data.PermissionResultBridge
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionRequester
import net.onefivefour.echolist.core.notifications.domain.NotificationScheduler
import org.koin.dsl.module

actual val notificationModule = module {
    single<NotificationScheduler> { AndroidNotificationScheduler(context = get()) }
    single<NotificationPermissionChecker> { AndroidNotificationPermissionChecker(context = get()) }
    single { PermissionResultBridge() }
    single<NotificationPermissionRequester> {
        AndroidNotificationPermissionRequester(
            context = get(),
            bridge = get()
        )
    }
}
