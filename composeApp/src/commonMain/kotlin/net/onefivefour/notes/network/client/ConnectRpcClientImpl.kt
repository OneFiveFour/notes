package net.onefivefour.notes.network.client

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import net.onefivefour.notes.network.config.NetworkConfig
import net.onefivefour.notes.network.config.NetworkConfigProvider
import net.onefivefour.notes.network.error.NetworkException

internal class ConnectRpcClientImpl(
    private val httpClient: HttpClient,
    private val configSupplier: () -> NetworkConfig
) : ConnectRpcClient {

    /**
     * Convenience constructor using a [NetworkConfigProvider] (used by DI).
     */
    constructor(httpClient: HttpClient, configProvider: NetworkConfigProvider) : this(
        httpClient = httpClient,
        configSupplier = { configProvider.config }
    )

    /**
     * Convenience constructor using a static [NetworkConfig] (used by tests and temp clients).
     */
    constructor(httpClient: HttpClient, config: NetworkConfig) : this(
        httpClient = httpClient,
        configSupplier = { config }
    )

    override suspend fun <Req, Res> call(
        path: String,
        request: Req,
        requestSerializer: (Req) -> ByteArray,
        responseDeserializer: (ByteArray) -> Res
    ): Result<Res> {
        val config = configSupplier()
        return withRetry(
            maxAttempts = config.maxRetries,
            delayMs = config.retryDelayMs
        ) {
            try {
                val requestBody = requestSerializer(request)

                val response = httpClient.post("${config.baseUrl}$path") {
                    contentType(ContentType("application", "proto"))
                    header("Connect-Protocol-Version", "1")
                    setBody(requestBody)
                }

                when (val statusCode = response.status.value) {
                    in 200..299 -> {
                        try {
                            val responseBody = response.readRawBytes()
                            Result.success(responseDeserializer(responseBody))
                        } catch (e: Exception) {
                            Result.failure(
                                NetworkException.SerializationError(
                                    "Failed to deserialize response: ${e.message}",
                                    e
                                )
                            )
                        }
                    }
                    in 400..499 -> {
                        val errorMessage = response.bodyAsText()
                        Result.failure(NetworkException.ClientError(statusCode, errorMessage))
                    }
                    in 500..599 -> {
                        val errorMessage = response.bodyAsText()
                        Result.failure(NetworkException.ServerError(statusCode, errorMessage))
                    }
                    else -> Result.failure(
                        NetworkException.NetworkError("Unexpected status: $statusCode")
                    )
                }
            } catch (e: NetworkException) {
                Result.failure(e)
            } catch (e: HttpRequestTimeoutException) {
                Result.failure(NetworkException.TimeoutError("Request timed out: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(
                    NetworkException.NetworkError("Network error: ${e.message}", e)
                )
            }
        }
    }
}
