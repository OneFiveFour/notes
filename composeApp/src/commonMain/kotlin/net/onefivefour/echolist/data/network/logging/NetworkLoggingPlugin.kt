package net.onefivefour.echolist.data.network.logging

import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.content.OutgoingContent
import io.ktor.util.AttributeKey
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private val StartTimeMark = AttributeKey<TimeMark>("NetworkLoggingStartTime")

val NetworkLoggingPlugin = createClientPlugin("NetworkLoggingPlugin", ::NetworkLoggingConfig) {
    val minLogLevel = pluginConfig.minLogLevel

    onRequest { request, _ ->
        try {
            request.attributes.put(StartTimeMark, TimeSource.Monotonic.markNow())

            if (LogLevel.DEBUG >= minLogLevel) {
                val method = request.method.value
                val url = request.url.buildString()
                val headers = request.headers.entries().associate { (name, values) -> name to values }
                val bodyBytes = try {
                    when (val body = request.body) {
                        is OutgoingContent.ByteArrayContent -> body.bytes()
                        is ByteArray -> body
                        else -> byteArrayOf()
                    }
                } catch (_: Throwable) {
                    byteArrayOf()
                }
                val logEntry = LogEntryFormatter.formatRequest(method, url, headers, bodyBytes)
                println(logEntry)
            }
        } catch (_: Throwable) {
            // Logging must never break the HTTP pipeline
        }
    }

    on(Send) { request ->
        try {
            val originalCall = try {
                proceed(request)
            } catch (e: Throwable) {
                try {
                    if (LogLevel.ERROR >= minLogLevel) {
                        val url = request.url.buildString()
                        val logEntry = LogEntryFormatter.formatError(url, e.message ?: "Unknown error")
                        println(logEntry)
                    }
                } catch (_: Throwable) {
                    // Logging must never break the HTTP pipeline
                }
                throw e
            }

            try {
                val startMark = request.attributes.getOrNull(StartTimeMark)
                val elapsedMs = startMark?.elapsedNow()?.inWholeMilliseconds ?: 0L

                val statusCode = originalCall.response.status.value
                val logLevel = if (statusCode in 400..599) LogLevel.WARN else LogLevel.DEBUG

                if (logLevel >= minLogLevel) {
                    val headers = originalCall.response.headers.entries().associate { (name, values) -> name to values }
                    val bodyBytes = try {
                        originalCall.response.bodyAsBytes()
                    } catch (_: Throwable) {
                        byteArrayOf()
                    }
                    val logEntry = LogEntryFormatter.formatResponse(statusCode, headers, bodyBytes, elapsedMs)
                    println(logEntry)
                }
            } catch (_: Throwable) {
                // Logging must never break the HTTP pipeline
            }

            originalCall
        } catch (e: Throwable) {
            throw e
        }
    }
}
