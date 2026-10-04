package net.onefivefour.echolist.data.network.logging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

/**
 * Unit tests for BodyFormatter.
 * Tests empty body, exact known outputs, and 1024/1025 byte boundary cases.
 */
class BodyFormatterTest : FunSpec({

    test("empty body returns 'empty body'") {
        BodyFormatter.format(byteArrayOf()) shouldBe "empty body"
    }

    test("exact known output for ASCII string") {
        val bytes = "hello".encodeToByteArray()
        val result = BodyFormatter.format(bytes)
        result shouldBe "hello (5 bytes)"
    }

    test("boundary 1024 bytes has no truncation") {
        val bytes = ByteArray(1024) { 'A'.code.toByte() }
        val result = BodyFormatter.format(bytes)

        result shouldContain "(1024 bytes)"
        result shouldNotContain "truncated"
    }

    test("boundary 1025 bytes is truncated") {
        val bytes = ByteArray(1025) { 'B'.code.toByte() }
        val result = BodyFormatter.format(bytes)

        result shouldContain "... (truncated, 1025 bytes total)"
    }
})
