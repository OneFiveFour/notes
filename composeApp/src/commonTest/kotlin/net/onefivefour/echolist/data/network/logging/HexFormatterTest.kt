package net.onefivefour.echolist.data.network.logging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

/**
 * Unit tests for HexFormatter.
 * Tests empty body, exact known outputs, and 1024/1025 byte boundary cases.
 */
class HexFormatterTest : FunSpec({

    test("empty body returns 'empty body'") {
        HexFormatter.format(byteArrayOf()) shouldBe "empty body"
    }

    test("exact known output for 3 bytes") {
        val result = HexFormatter.format(byteArrayOf(0x0A, 0x1B, 0x2C))
        result shouldBe "0A 1B 2C (3 bytes)"
    }

    test("boundary 1024 bytes has no truncation") {
        val bytes = ByteArray(1024) { (it % 256).toByte() }
        val result = HexFormatter.format(bytes)

        result shouldContain "(1024 bytes)"
        result shouldNotContain "truncated"
    }

    test("boundary 1025 bytes is truncated") {
        val bytes = ByteArray(1025) { (it % 256).toByte() }
        val result = HexFormatter.format(bytes)

        result shouldContain "... (truncated, 1025 bytes total)"
    }
})
