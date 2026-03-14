package net.onefivefour.echolist.data.network.logging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import java.io.ByteArrayOutputStream
import java.io.PrintStream

// Feature: network-request-logging, Property 6: Log level suppression
// Feature: network-request-logging, Property 7: Error status codes logged at WARN

class NetworkLoggingPluginPropertyTest : FunSpec({

    // -- Property 6: Log level suppression --
    // **Validates: Requirements 7.2**
    // For any LogLevel strictly greater than DEBUG, when the plugin's minimum log level
    // is set to that level, normal request and response log entries (which are at DEBUG level)
    // should be suppressed (not emitted).
    test("Property 6: Log level suppression - non-DEBUG minLogLevel suppresses request/response logs") {
        val arbNonDebugLevel = Arb.enum<LogLevel>().filter { it > LogLevel.DEBUG }

        checkAll(PropTestConfig(iterations = 100), arbNonDebugLevel) { level ->
            val mockEngine = MockEngine {
                respond(content = "ok", status = HttpStatusCode.OK)
            }

            val client = HttpClient(mockEngine) {
                install(NetworkLoggingPlugin) {
                    minLogLevel = level
                }
            }

            val outputStream = ByteArrayOutputStream()
            val originalOut = System.out
            System.setOut(PrintStream(outputStream))
            try {
                client.get("http://localhost/test")
            } finally {
                System.setOut(originalOut)
            }

            val output = outputStream.toString()

            // Normal request/response entries are at DEBUG level.
            // Since minLogLevel > DEBUG, they should be suppressed.
            output.contains("-->") shouldBe false
            output.contains("<--") shouldBe false

            client.close()
        }
    }

    // -- Property 7: Error status codes logged at WARN --
    // **Validates: Requirements 8.1**
    // For any HTTP status code in the range 400-599, the plugin should assign WARN log level
    // to the response log entry instead of DEBUG.
    test("Property 7: Error status codes 400-599 produce response log entries at WARN level") {
        val arbErrorStatusCode = Arb.int(400..599)

        checkAll(PropTestConfig(iterations = 100), arbErrorStatusCode) { statusCode ->
            val mockEngine = MockEngine {
                respond(content = "error", status = HttpStatusCode.fromValue(statusCode))
            }

            val client = HttpClient(mockEngine) {
                install(NetworkLoggingPlugin) {
                    minLogLevel = LogLevel.DEBUG
                }
            }

            val outputStream = ByteArrayOutputStream()
            val originalOut = System.out
            System.setOut(PrintStream(outputStream))
            try {
                client.get("http://localhost/test")
            } finally {
                System.setOut(originalOut)
            }

            val output = outputStream.toString()

            // WARN >= DEBUG, so the response log entry should still be emitted
            output shouldContain "<-- $statusCode"

            client.close()
        }
    }

    // Verify that WARN-level entries are NOT suppressed when minLogLevel is WARN
    test("Property 7: Error status codes are emitted even when minLogLevel is WARN") {
        val arbErrorStatusCode = Arb.int(400..599)

        checkAll(PropTestConfig(iterations = 100), arbErrorStatusCode) { statusCode ->
            val mockEngine = MockEngine {
                respond(content = "error", status = HttpStatusCode.fromValue(statusCode))
            }

            val client = HttpClient(mockEngine) {
                install(NetworkLoggingPlugin) {
                    minLogLevel = LogLevel.WARN
                }
            }

            val outputStream = ByteArrayOutputStream()
            val originalOut = System.out
            System.setOut(PrintStream(outputStream))
            try {
                client.get("http://localhost/test")
            } finally {
                System.setOut(originalOut)
            }

            val output = outputStream.toString()

            // WARN >= WARN, so the response log entry should be emitted
            output shouldContain "<-- $statusCode"

            // But request log (DEBUG level) should be suppressed
            output.contains("-->") shouldBe false

            client.close()
        }
    }
})
