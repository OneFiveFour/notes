package net.onefivefour.echolist.data.network.logging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for HeaderRedactor.
 * Tests mixed sensitive and non-sensitive headers.
 */
class HeaderRedactorTest : FunSpec({

    test("mixed sensitive and non-sensitive headers are correctly redacted") {
        val headers = mapOf(
            "Authorization" to listOf("Bearer eyJhbGciOiJIUzI1NiJ9.secret"),
            "X-Token-Id" to listOf("abc123token"),
            "Content-Type" to listOf("application/proto"),
            "Accept" to listOf("application/json")
        )

        val redacted = HeaderRedactor.redact(headers)

        // Sensitive headers should be redacted
        redacted["Authorization"] shouldBe listOf("[REDACTED]")
        redacted["X-Token-Id"] shouldBe listOf("[REDACTED]")

        // Non-sensitive headers should be preserved
        redacted["Content-Type"] shouldBe listOf("application/proto")
        redacted["Accept"] shouldBe listOf("application/json")
    }
})
