package net.onefivefour.echolist.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.core.files.di.filesModule
import net.onefivefour.echolist.core.session.domain.AuthEventBus
import net.onefivefour.echolist.core.session.domain.AuthRepository
import net.onefivefour.echolist.domain.repository.NotesRepository
import net.onefivefour.echolist.data.repository.NotesRepositoryImpl
import net.onefivefour.echolist.data.repository.FileRepositoryImpl
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository
import net.onefivefour.echolist.data.source.cache.CacheDataSource
import net.onefivefour.echolist.data.source.cache.CacheDataSourceImpl
import net.onefivefour.echolist.data.source.network.FileRemoteDataSource
import net.onefivefour.echolist.data.source.network.FileRemoteDataSourceImpl
import net.onefivefour.echolist.data.source.network.NoteRemoteDataSource
import net.onefivefour.echolist.data.source.network.NoteRemoteDataSourceImpl
import net.onefivefour.echolist.core.files.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.domain.repository.FileRepository
import net.onefivefour.echolist.core.networking.data.client.ConnectRpcClient
import net.onefivefour.echolist.core.designsystem.di.designSystemModule
import net.onefivefour.echolist.ui.AuthViewModel
import net.onefivefour.echolist.ui.editnote.EditNoteMode
import net.onefivefour.echolist.ui.editnote.EditNoteViewModel
import net.onefivefour.echolist.ui.edittasklist.EditTaskListMode
import net.onefivefour.echolist.ui.edittasklist.EditTaskListViewModel
import net.onefivefour.echolist.ui.home.CreateFolderViewModel
import net.onefivefour.echolist.ui.home.HomeViewModel
import net.onefivefour.echolist.ui.home.RenameFolderViewModel
import net.onefivefour.echolist.ui.maintasksettings.MainTaskSettingsResultBus
import net.onefivefour.echolist.ui.maintasksettings.MainTaskSettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

val authModule: Module = module {
    viewModel { AuthViewModel(authRepository = get(), authEventBus = get()) }
}

val networkModule: Module = module {
    single<NoteRemoteDataSource> {
        NoteRemoteDataSourceImpl(client = get())
    }

    single<FileRemoteDataSource> {
        FileRemoteDataSourceImpl(client = get())
    }

}

val dataModule: Module = module {
    single<CoroutineDispatcher> { Dispatchers.Default }

    single<CacheDataSource> {
        CacheDataSourceImpl(database = get())
    }

    includes(filesModule, net.onefivefour.echolist.core.tasks.di.tasksModule)

    single<NotesRepository> {
        NotesRepositoryImpl(
            noteRemoteDataSource = get(),
            cacheDataSource = get(),
            directoryChangeNotifier = get(),
            dispatcher = Dispatchers.Default
        )
    } withOptions {
        onClose { (it as? AutoCloseable)?.close() }
    }

    single<FileRepository> {
        FileRepositoryImpl(
            networkDataSource = get(),
            directoryChangeNotifier = get(),
            dispatcher = Dispatchers.Default
        )
    }

}


val navigationModule: Module = module {
    single { MainTaskSettingsResultBus() }
    viewModel { params ->
        HomeViewModel(
            parentDir = params.get(),
            fileRepository = get(),
            directoryChangeNotifier = get()
        )
    }
    viewModel { params ->
        CreateFolderViewModel(
            parentDir = params.get(),
            fileRepository = get()
        )
    }
    viewModel { params ->
        RenameFolderViewModel(
            parentDir = params.get(),
            fileRepository = get()
        )
    }
    viewModel { params ->
        EditNoteViewModel(
            mode = params.get<EditNoteMode>(),
            notesRepository = get()
        )
    }
    viewModel { params ->
        EditTaskListViewModel(
            mode = params.get<EditTaskListMode>(),
            taskListRepository = get(),
            settingsResultBus = get(),
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
            resultBus = get()
        )
    }
}

val appModules: List<Module> = listOf(
    net.onefivefour.echolist.core.session.di.sessionModule,
    authModule,
    net.onefivefour.echolist.feature.login.di.loginModule,
    networkModule,
    dataModule,
    designSystemModule,
    navigationModule
)
