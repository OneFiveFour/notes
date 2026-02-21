package net.onefivefour.notes.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import net.onefivefour.notes.data.repository.AuthRepository
import net.onefivefour.notes.data.repository.AuthRepositoryImpl
import net.onefivefour.notes.network.auth.AuthEvent
import net.onefivefour.notes.network.auth.AuthInterceptor
import net.onefivefour.notes.data.repository.NotesRepository
import net.onefivefour.notes.data.repository.NotesRepositoryImpl
import net.onefivefour.notes.data.source.cache.CacheDataSource
import net.onefivefour.notes.data.source.cache.CacheDataSourceImpl
import net.onefivefour.notes.data.source.network.NetworkDataSource
import net.onefivefour.notes.data.source.network.NetworkDataSourceImpl
import net.onefivefour.notes.network.client.ConnectRpcClient
import net.onefivefour.notes.network.client.ConnectRpcClientImpl
import net.onefivefour.notes.network.config.NetworkConfigProvider
import net.onefivefour.notes.ui.theme.EchoListClassicTheme
import net.onefivefour.notes.ui.theme.EchoListTheme2
import net.onefivefour.notes.ui.theme.ThemeManager
import net.onefivefour.notes.ui.AuthViewModel
import net.onefivefour.notes.ui.home.HomeViewModel
import net.onefivefour.notes.ui.login.LoginViewModel
import net.onefivefour.notes.ui.notedetail.NoteDetailViewModel
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

    single<NetworkDataSource> {
        NetworkDataSourceImpl(client = get())
    }
}

val dataModule: Module = module {
    single<CacheDataSource> {
        CacheDataSourceImpl(database = get())
    }

    single<NotesRepository> {
        NotesRepositoryImpl(
            networkDataSource = get(),
            cacheDataSource = get(),
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
            repository = get()
        )
    }
    viewModel { params ->
        NoteDetailViewModel(
            noteId = params.get(),
            repository = get()
        )
    }
}

val appModules: List<Module> = listOf(authModule, networkModule, dataModule, uiModule, navigationModule)
