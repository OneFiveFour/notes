package net.onefivefour.echolist.di

import net.onefivefour.echolist.data.notification.AndroidNotificationPermissionChecker
import net.onefivefour.echolist.data.notification.AndroidNotificationPermissionRequester
import net.onefivefour.echolist.data.notification.AndroidNotificationScheduler
import net.onefivefour.echolist.domain.NotificationPermissionChecker
import net.onefivefour.echolist.domain.NotificationPermissionRequester
import net.onefivefour.echolist.domain.NotificationScheduler
import org.koin.dsl.module

val notificationModule = module {
    single<NotificationScheduler> { AndroidNotificationScheduler(context = get()) }
    single<NotificationPermissionChecker> { AndroidNotificationPermissionChecker(context = get()) }
    single<NotificationPermissionRequester> { AndroidNotificationPermissionRequester(context = get()) }
}
