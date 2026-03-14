package net.onefivefour.echolist.data.network.logging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Feature: network-request-logging, Property 5: Body formatting correctness
 *
 * Validates: Requirements 4.1, 4.2, 4.3
 */
class BodyFormatterPropertyTest : FunSpec({

    // Feature: network-request-logging, Property 5: Body formatting correctness
    test("Feature: network-request-logging, Property 5: Body formatting correctness") {
        checkAll(PropTestConfig(iterations = 100), Arb.byteArray(Arb.int(1..4096), Arb.byte())) { bytes ->
            val result = BodyFormatter.format(bytes)
            val totalSize = bytes.size
            val isTruncated = totalSize > 1024

            // (a) The decoded text from the formatted bytes is present in the output
            val bytesToFormat = if (isTruncated) bytes.copyOf(1024) else bytes
            val expectedText = bytesToFormat.decodeToString()
            result shouldContain expectedText

            // (b) The total byte count appears in the output
            result shouldContain totalSize.toString()

            // (c) If array exceeds 1024 bytes, truncation indicator is present
            if (isTruncated) {
                result shouldContain "truncated"
                result shouldContain "$totalSize bytes total"
            } else {
                result shouldContain "$totalSize bytes)"
            }
        }
    }
})
