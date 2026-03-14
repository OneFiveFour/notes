package net.onefivefour.echolist.data.network.logging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Feature: network-request-logging, Property 5: Hex formatting correctness
 *
 * Validates: Requirements 4.1, 4.2, 4.3
 */
class HexFormatterPropertyTest : FunSpec({

    // Feature: network-request-logging, Property 5: Hex formatting correctness
    test("Feature: network-request-logging, Property 5: Hex formatting correctness") {
        checkAll(PropTestConfig(iterations = 100), Arb.byteArray(Arb.int(1..4096), Arb.byte())) { bytes ->
            val result = HexFormatter.format(bytes)
            val totalSize = bytes.size
            val isTruncated = totalSize > 1024

            // (a) Each byte is represented as exactly two uppercase hexadecimal characters
            // Extract hex octets from the output (before the parenthetical suffix)
            val hexPart = if (isTruncated) {
                result.substringBefore(" ... (truncated,")
            } else {
                result.substringBefore(" (")
            }
            val octets = hexPart.split(" ")
            val expectedOctetCount = if (isTruncated) 1024 else totalSize
            octets.size shouldBe expectedOctetCount

            for (octet in octets) {
                // Each octet is exactly 2 uppercase hex characters
                octet.length shouldBe 2
                octet shouldBe octet.uppercase()
                octet.all { it in "0123456789ABCDEF" } shouldBe true
            }

            // (b) Octets are space-separated — verified by successful split above

            // (c) The total byte count appears in the output
            result shouldContain totalSize.toString()

            // (d) If array exceeds 1024 bytes, only first 1024 are hex-formatted and truncation indicator is present
            if (isTruncated) {
                result shouldContain "truncated"
                result shouldContain "$totalSize bytes total"
            }
        }
    }
})
