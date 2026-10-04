package net.onefivefour.echolist.data.network.logging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property tests for [LogEntryFormatter].
 */
class LogEntryFormatterPropertyTest : FunSpec({

    // Generator for non-sensitive header names (avoids "Authorization" and "token" substring)
    val nonSensitiveHeaderName = Arb.element(
        "Content-Type",
        "Accept",
        "Cache-Control",
        "X-Request-Id",
        "Host",
        "User-Agent",
        "Content-Length",
        "Accept-Encoding"
    )

    val headerValue = Arb.element(
        "application/proto",
        "text/plain",
        "gzip",
        "utf-8",
        "keep-alive",
        "no-cache"
    )

    val nonSensitiveHeaders: Arb<Map<String, List<String>>> = Arb.map(
        keyArb = nonSensitiveHeaderName,
        valueArb = Arb.list(headerValue, 1..2),
        minSize = 1,
        maxSize = 4
    )

    // Feature: network-request-logging, Property 1: Request log entry contains method, URL, and headers
    test("Feature: network-request-logging, Property 1: Request log entry contains method, URL, and headers") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.element("GET", "POST", "PUT", "DELETE", "PATCH"),
            Arb.string(minSize = 1, maxSize = 50),
            nonSensitiveHeaders
        ) { method, url, headers ->
            val result = LogEntryFormatter.formatRequest(method, url, headers, byteArrayOf())

            // Must contain the HTTP method
            result shouldContain method

            // Must contain the full URL
            result shouldContain url

            // Must contain every header name and value
            headers.forEach { (name, values) ->
                result shouldContain name
                values.forEach { value ->
                    result shouldContain value
                }
            }
        }
    }

    // Feature: network-request-logging, Property 2: Response log entry contains status code, headers, and elapsed time
    test("Feature: network-request-logging, Property 2: Response log entry contains status code, headers, and elapsed time") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.int(100..599),
            nonSensitiveHeaders,
            Arb.long(0L..Long.MAX_VALUE / 2)
        ) { statusCode, headers, elapsedMs ->
            val result = LogEntryFormatter.formatResponse(statusCode, headers, byteArrayOf(), elapsedMs)

            // Must contain the status code
            result shouldContain statusCode.toString()

            // Must contain every header name and value
            headers.forEach { (name, values) ->
                result shouldContain name
                values.forEach { value ->
                    result shouldContain value
                }
            }

            // Must contain the elapsed time
            result shouldContain elapsedMs.toString()
        }
    }

    // Feature: network-request-logging, Property 8: Error entries contain URL and error message
    test("Feature: network-request-logging, Property 8: Error entries contain URL and error message") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(minSize = 1, maxSize = 100),
            Arb.string(minSize = 1, maxSize = 100)
        ) { url, errorMessage ->
            val result = LogEntryFormatter.formatError(url, errorMessage)

            // Must contain the URL
            result shouldContain url

            // Must contain the error message
            result shouldContain errorMessage
        }
    }
})
