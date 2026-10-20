package net.onefivefour.echolist.core.tasks.di

import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.core.tasks.data.repository.TaskListRepositoryImpl
import net.onefivefour.echolist.core.tasks.data.source.network.TaskListRemoteDataSource
import net.onefivefour.echolist.core.tasks.data.source.network.TaskListRemoteDataSourceImpl
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository
import net.onefivefour.echolist.core.tasks.domain.completeRecurringTaskFromNotification
import net.onefivefour.echolist.core.notifications.domain.TaskCompletionHandler
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.NotificationSyncHandler
import net.onefivefour.echolist.core.notifications.domain.UpcomingReminder
import org.koin.dsl.module

val tasksModule = module {
    single<TaskListRemoteDataSource> { TaskListRemoteDataSourceImpl(get()) }
    single<TaskListRepository> { TaskListRepositoryImpl(get(), Dispatchers.Default, get(), get()) }
    single<NotificationSyncHandler> {
        NotificationSyncHandler {
            get<NotificationCoordinator>().synchronize { fromDate ->
                get<TaskListRepository>().listUpcomingMainTasks(fromDate).map { tasks ->
                    tasks.map { UpcomingReminder(it.taskId, it.taskListId, it.title, it.dueDate) }
                }
            }
        }
    }
    single<TaskCompletionHandler> {
        object : TaskCompletionHandler {
            override suspend fun complete(taskListId: String, taskId: String): Result<Unit> =
                completeRecurringTaskFromNotification(get(), get(), taskListId, taskId)
            override suspend fun completeOccurrence(taskListId: String, taskId: String, dueDate: String): Result<Unit> =
                completeRecurringTaskFromNotification(get(), get(), taskListId, taskId, expectedDueDate = dueDate)
        }
    }
}