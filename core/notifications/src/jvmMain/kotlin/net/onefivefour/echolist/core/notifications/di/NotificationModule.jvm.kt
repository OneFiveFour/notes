package net.onefivefour.echolist.core.notifications.di

import net.onefivefour.echolist.core.notifications.data.JvmNotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.data.JvmNotificationPermissionRequester
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.onefivefour.echolist.core.notifications.data.SqlReminderStore
import net.onefivefour.echolist.core.notifications.data.JvmReminderPlatform
import net.onefivefour.echolist.core.notifications.data.InProcessReminderBackgroundScheduler
import net.onefivefour.echolist.core.notifications.database.NotificationDatabase
import net.onefivefour.echolist.core.notifications.domain.ReminderStore
import net.onefivefour.echolist.core.notifications.domain.ReminderPlatform
import net.onefivefour.echolist.core.notifications.domain.ReminderBackgroundScheduler
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.NotificationSyncHandler
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionRequester
import org.koin.dsl.module

actual val notificationModule = module {
    includes(reminderModule)
    single<ReminderStore> {
        val directory = Path.of(System.getProperty("user.home"), ".echolist")
        Files.createDirectories(directory)
        val driver = JdbcSqliteDriver(
            "jdbc:sqlite:${directory.resolve("notifications.db")}",
            Properties(),
            NotificationDatabase.Schema.synchronous()
        )
        SqlReminderStore(NotificationDatabase(driver))
    }
    single<ReminderPlatform> { JvmReminderPlatform() }
    single<ReminderBackgroundScheduler> {
        InProcessReminderBackgroundScheduler(
            restore = { get<NotificationCoordinator>().restore() },
            sync = { get<NotificationSyncHandler>().synchronize() }
        )
    }
    single<NotificationPermissionChecker> { JvmNotificationPermissionChecker() }
    single<NotificationPermissionRequester> { JvmNotificationPermissionRequester() }
}