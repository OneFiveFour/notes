package net.onefivefour.notes.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import net.onefivefour.notes.cache.NotesDatabase
import net.onefivefour.notes.data.source.cache.CacheDataSourceImpl
import net.onefivefour.notes.data.source.network.NetworkDataSourceImpl
import net.onefivefour.notes.network.client.ConnectRpcClientImpl
import net.onefivefour.notes.network.config.NetworkConfig

object NotesRepositoryFactory {
    fun create(
        config: NetworkConfig,
        database: NotesDatabase
    ): NotesRepository {
        val httpClient = HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = config.requestTimeoutMs
                connectTimeoutMillis = config.connectTimeoutMs
            }
        }
        val connectRpcClient = ConnectRpcClientImpl(httpClient, config)
        val networkDataSource = NetworkDataSourceImpl(connectRpcClient)
        val cacheDataSource = CacheDataSourceImpl(database)
        return NotesRepositoryImpl(networkDataSource, cacheDataSource)
    }
}
