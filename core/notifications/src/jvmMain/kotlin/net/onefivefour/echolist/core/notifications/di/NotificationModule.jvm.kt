package net.onefivefour.echolist.core.notifications.di

import net.onefivefour.echolist.core.notifications.data.JvmNotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.data.JvmNotificationPermissionRequester
import net.onefivefour.echolist.core.notifications.data.JvmNotificationScheduler
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionRequester
import net.onefivefour.echolist.core.notifications.domain.NotificationScheduler
import org.koin.dsl.module

actual val notificationModule = module {
    single<NotificationScheduler> { JvmNotificationScheduler() }
    single<NotificationPermissionChecker> { JvmNotificationPermissionChecker() }
    single<NotificationPermissionRequester> { JvmNotificationPermissionRequester() }
}