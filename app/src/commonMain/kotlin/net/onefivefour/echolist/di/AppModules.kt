package net.onefivefour.echolist.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.core.files.di.filesModule
import net.onefivefour.echolist.core.session.domain.AuthEventBus
import net.onefivefour.echolist.core.session.domain.AuthRepository
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository
import net.onefivefour.echolist.core.files.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.core.networking.data.client.ConnectRpcClient
import net.onefivefour.echolist.core.designsystem.di.designSystemModule
import net.onefivefour.echolist.ui.AuthViewModel
import net.onefivefour.echolist.ui.edittasklist.EditTaskListMode
import net.onefivefour.echolist.ui.edittasklist.EditTaskListViewModel
import net.onefivefour.echolist.ui.maintasksettings.MainTaskSettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

val authModule: Module = module {
    viewModel { AuthViewModel(authRepository = get(), authEventBus = get()) }
}


val dataModule: Module = module {
    single<CoroutineDispatcher> { Dispatchers.Default }


    includes(filesModule, net.onefivefour.echolist.core.tasks.di.tasksModule)



}


val navigationModule: Module = module {
    viewModel { net.onefivefour.echolist.ui.navigation.TaskSettingsChannels() }
    viewModel { params ->
        EditTaskListViewModel(
            mode = params.get<EditTaskListMode>(),
            taskListRepository = get(),
            settingsResults = params.get(),
            notificationScheduler = get()
        )
    }
    viewModel { params ->
        MainTaskSettingsViewModel(
            mainTaskId = params.get(),
            currentDueDate = params.get(),
            currentRecurrence = params.get(),
            currentIsNotificationEnabled = params.get(),
            permissionChecker = get(),
            permissionRequester = get(),
            resultBus = params.get()
        )
    }
}

val appModules: List<Module> = listOf(
    net.onefivefour.echolist.core.session.di.sessionModule,
    authModule,
    net.onefivefour.echolist.feature.browser.di.browserModule,
    net.onefivefour.echolist.feature.login.di.loginModule,
    net.onefivefour.echolist.feature.note.di.noteModule,
    dataModule,
    designSystemModule,
    navigationModule
)
