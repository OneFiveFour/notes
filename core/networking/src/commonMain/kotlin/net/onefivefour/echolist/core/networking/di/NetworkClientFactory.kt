package net.onefivefour.echolist.core.networking.di

import io.ktor.client.HttpClient
import net.onefivefour.echolist.core.networking.data.client.ConnectRpcClient
import net.onefivefour.echolist.core.networking.data.client.ConnectRpcClientImpl
import net.onefivefour.echolist.core.networking.data.config.NetworkConfigProvider

fun createConnectRpcClient(httpClient: HttpClient, configProvider: NetworkConfigProvider): ConnectRpcClient =
    ConnectRpcClientImpl(httpClient, configProvider)