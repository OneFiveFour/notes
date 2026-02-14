package net.onefivefour.notes.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.Dispatchers
import net.onefivefour.notes.data.repository.NotesRepository
import net.onefivefour.notes.data.repository.NotesRepositoryImpl
import net.onefivefour.notes.data.source.cache.CacheDataSource
import net.onefivefour.notes.data.source.cache.CacheDataSourceImpl
import net.onefivefour.notes.data.source.network.NetworkDataSource
import net.onefivefour.notes.data.source.network.NetworkDataSourceImpl
import net.onefivefour.notes.network.client.ConnectRpcClient
import net.onefivefour.notes.network.client.ConnectRpcClientImpl
import net.onefivefour.notes.network.config.NetworkConfig
import org.koin.core.module.Module
import org.koin.dsl.module

val networkModule: Module = module {
    single { NetworkConfig.default(baseUrl = "http://localhost:8080") }

    single {
        val config: NetworkConfig = get()
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = config.requestTimeoutMs
                connectTimeoutMillis = config.connectTimeoutMs
            }
        }
    }

    single<ConnectRpcClient> {
        ConnectRpcClientImpl(
            httpClient = get(),
            config = get()
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

val appModules: List<Module> = listOf(networkModule, dataModule)
