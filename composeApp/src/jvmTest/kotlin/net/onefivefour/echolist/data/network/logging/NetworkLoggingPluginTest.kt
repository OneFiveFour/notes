package net.onefivefour.echolist.data.network.logging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.ConnectException

class NetworkLoggingPluginTest : FunSpec({

    test("request and response logging emits both entries") {
        val responseBody = byteArrayOf(0x0A, 0x1B, 0x2C)
        val mockEngine = MockEngine {
            respond(
                content = responseBody,
                status = HttpStatusCode.OK
            )
        }

        val client = HttpClient(mockEngine) {
            install(NetworkLoggingPlugin)
        }

        val outputStream = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outputStream))
        try {
            client.get("http://localhost/test-path")
        } finally {
            System.setOut(originalOut)
        }

        val output = outputStream.toString()

        // Request log entry
        output shouldContain "-->"
        output shouldContain "GET"
        output shouldContain "http://localhost/test-path"

        // Response log entry
        output shouldContain "<-- 200"
        output shouldContain "ms)"

        client.close()
    }

    test("empty body request logs empty body") {
        val mockEngine = MockEngine {
            respond(content = byteArrayOf(), status = HttpStatusCode.OK)
        }

        val client = HttpClient(mockEngine) {
            install(NetworkLoggingPlugin)
        }

        val outputStream = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outputStream))
        try {
            client.get("http://localhost/empty")
        } finally {
            System.setOut(originalOut)
        }

        val output = outputStream.toString()

        // Request body should be empty (GET has no body)
        output shouldContain "empty body"

        client.close()
    }

    test("timeout/exception error entry is logged at ERROR level") {
        val mockEngine = MockEngine {
            throw ConnectException("Connection refused")
        }

        val client = HttpClient(mockEngine) {
            install(NetworkLoggingPlugin)
        }

        val outputStream = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outputStream))
        try {
            try {
                client.get("http://localhost/timeout-test")
            } catch (_: Throwable) {
                // Expected — the mock throws
            }
        } finally {
            System.setOut(originalOut)
        }

        val output = outputStream.toString()

        // Error log entry
        output shouldContain "<-- ERROR"
        output shouldContain "http://localhost/timeout-test"
        output shouldContain "Connection refused"

        client.close()
    }

    test("POST request with body logs hex-formatted body") {
        val mockEngine = MockEngine {
            respond(content = "ok", status = HttpStatusCode.OK)
        }

        val client = HttpClient(mockEngine) {
            install(NetworkLoggingPlugin)
        }

        val outputStream = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outputStream))
        try {
            client.post("http://localhost/data") {
                contentType(ContentType.Application.OctetStream)
                setBody(byteArrayOf(0x0A, 0x1B))
            }
        } finally {
            System.setOut(originalOut)
        }

        val output = outputStream.toString()

        output shouldContain "--> POST"
        output shouldContain "0A 1B"

        client.close()
    }

    test("error response status code does not suppress response log") {
        val mockEngine = MockEngine {
            respond(content = "not found", status = HttpStatusCode.NotFound)
        }

        val client = HttpClient(mockEngine) {
            install(NetworkLoggingPlugin)
        }

        val outputStream = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outputStream))
        try {
            client.get("http://localhost/missing")
        } finally {
            System.setOut(originalOut)
        }

        val output = outputStream.toString()

        output shouldContain "<-- 404"

        client.close()
    }
})
