package net.onefivefour.echolist.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import net.onefivefour.echolist.data.repository.AuthRepository
import net.onefivefour.echolist.data.repository.AuthRepositoryImpl
import net.onefivefour.echolist.network.auth.AuthEvent
import net.onefivefour.echolist.network.auth.AuthInterceptor
import net.onefivefour.echolist.data.repository.NotesRepository
import net.onefivefour.echolist.data.repository.NotesRepositoryImpl
import net.onefivefour.echolist.data.repository.FileRepository
import net.onefivefour.echolist.data.repository.FileRepositoryImpl
import net.onefivefour.echolist.data.repository.TaskListRepository
import net.onefivefour.echolist.data.repository.TaskListRepositoryImpl
import net.onefivefour.echolist.data.source.cache.CacheDataSource
import net.onefivefour.echolist.data.source.cache.CacheDataSourceImpl
import net.onefivefour.echolist.data.source.network.FileRemoteDataSource
import net.onefivefour.echolist.data.source.network.FileRemoteDataSourceImpl
import net.onefivefour.echolist.data.source.network.NoteRemoteDataSource
import net.onefivefour.echolist.data.source.network.NoteRemoteDataSourceImpl
import net.onefivefour.echolist.data.source.network.TaskListRemoteDataSource
import net.onefivefour.echolist.data.source.network.TaskListRemoteDataSourceImpl
import net.onefivefour.echolist.network.client.ConnectRpcClient
import net.onefivefour.echolist.network.client.ConnectRpcClientImpl
import net.onefivefour.echolist.network.config.NetworkConfigProvider
import net.onefivefour.echolist.ui.theme.EchoListClassicTheme
import net.onefivefour.echolist.ui.theme.EchoListTheme2
import net.onefivefour.echolist.ui.theme.ThemeManager
import net.onefivefour.echolist.ui.AuthViewModel
import net.onefivefour.echolist.ui.home.HomeViewModel
import net.onefivefour.echolist.ui.login.LoginViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule: Module = module {
    single { MutableSharedFlow<AuthEvent>() }
    single<AuthRepository> { AuthRepositoryImpl(secureStorage = get()) }
    viewModel { AuthViewModel(secureStorage = get(), authEvents = get()) }
    viewModel { LoginViewModel(authRepository = get(), secureStorage = get(), networkConfigProvider = get()) }
}

val networkModule: Module = module {
    single { NetworkConfigProvider(secureStorage = get()) }

    single {
        val configProvider: NetworkConfigProvider = get()
        val authRepository: AuthRepository = get()
        val authEventFlow: MutableSharedFlow<AuthEvent> = get()
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = configProvider.config.requestTimeoutMs
                connectTimeoutMillis = configProvider.config.connectTimeoutMs
            }
            install(AuthInterceptor) {
                this.authRepository = authRepository
                this.authEventFlow = authEventFlow
            }
        }
    }

    single<ConnectRpcClient> {
        val configProvider: NetworkConfigProvider = get()
        ConnectRpcClientImpl(
            httpClient = get(),
            configProvider = configProvider
        )
    }

    single<NoteRemoteDataSource> {
        NoteRemoteDataSourceImpl(client = get())
    }

    single<FileRemoteDataSource> {
        FileRemoteDataSourceImpl(client = get())
    }

    single<TaskListRemoteDataSource> {
        TaskListRemoteDataSourceImpl(client = get())
    }
}

val dataModule: Module = module {
    single<CacheDataSource> {
        CacheDataSourceImpl(database = get())
    }

    single<NotesRepository> {
        NotesRepositoryImpl(
            noteRemoteDataSource = get(),
            cacheDataSource = get(),
            dispatcher = Dispatchers.Default
        )
    }

    single<FileRepository> {
        FileRepositoryImpl(
            networkDataSource = get(),
            dispatcher = Dispatchers.Default
        )
    }

    single<TaskListRepository> {
        TaskListRepositoryImpl(
            networkDataSource = get(),
            dispatcher = Dispatchers.Default
        )
    }
}

val uiModule: Module = module {
    single {
        ThemeManager(
            availableThemes = listOf(
                EchoListClassicTheme,
                EchoListTheme2
            ),
            initialTheme = EchoListClassicTheme
        )
    }
}

val navigationModule: Module = module {
    viewModel { params ->
        HomeViewModel(
            path = params.get(),
            notesRepository = get(),
            folderRepository = get<FileRepository>()
        )
    }
}

val appModules: List<Module> = listOf(
    authModule,
    networkModule,
    dataModule,
    uiModule,
    navigationModule
)
