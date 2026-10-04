package net.onefivefour.echolist.core.tasks.di

import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.core.tasks.data.repository.TaskListRepositoryImpl
import net.onefivefour.echolist.core.tasks.data.source.network.TaskListRemoteDataSource
import net.onefivefour.echolist.core.tasks.data.source.network.TaskListRemoteDataSourceImpl
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository
import net.onefivefour.echolist.core.tasks.domain.completeRecurringTaskFromNotification
import net.onefivefour.echolist.core.notifications.domain.TaskCompletionHandler
import org.koin.dsl.module

val tasksModule = module {
    single<TaskListRemoteDataSource> { TaskListRemoteDataSourceImpl(get()) }
    single<TaskListRepository> { TaskListRepositoryImpl(get(), Dispatchers.Default, get()) }
    single<TaskCompletionHandler> {
        TaskCompletionHandler { taskListId, taskId ->
            completeRecurringTaskFromNotification(get(), get(), taskListId, taskId)
        }
    }
}