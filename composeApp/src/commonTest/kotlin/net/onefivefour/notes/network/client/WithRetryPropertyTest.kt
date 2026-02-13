package net.onefivefour.notes.network.client

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.notes.network.error.NetworkException

/**
 * Property-based tests for withRetry utility function.
 *
 * **Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5**
 */
class WithRetryPropertyTest : FunSpec({

    // -- Generators --

    /** Generates transient (retryable) NetworkExceptions. */
    val arbTransientError: Arb<NetworkException> = Arb.element(
        NetworkException.NetworkError("connection refused"),
        NetworkException.ServerError(500, "internal server error"),
        NetworkException.ServerError(502, "bad gateway"),
        NetworkException.ServerError(503, "service unavailable"),
        NetworkException.TimeoutError("request timed out")
    )

    /** Generates non-retryable ClientErrors. */
    val arbClientError = arbitrary {
        val code = Arb.int(400..499).bind()
        NetworkException.ClientError(code, "client error $code")
    }

    // -- Property 19: Transient Error Retry Behavior --

    test("Property 19: For any transient error, at least one retry occurs before failing") {
        checkAll(PropTestConfig(iterations = 100), arbTransientError) { error ->
            var attemptCount = 0

            val result = withRetry(maxAttempts = 3, delayMs = 1) {
                attemptCount++
                Result.failure<Unit>(error)
            }

            result.isFailure shouldBe true
            attemptCount shouldBeGreaterThanOrEqual 2
        }
    }

    // -- Property 20: Retry Attempt Limit --

    test("Property 20: For any continuously failing request, at most 3 total attempts are made") {
        checkAll(PropTestConfig(iterations = 100), arbTransientError) { error ->
            var attemptCount = 0

            withRetry(maxAttempts = 3, delayMs = 1) {
                attemptCount++
                Result.failure<Unit>(error)
            }

            attemptCount shouldBeLessThanOrEqual 3
        }
    }

    // -- Property 21: Selective Retry by Error Type --

    test("Property 21: ClientError (4xx) causes no retry — only 1 attempt") {
        checkAll(PropTestConfig(iterations = 100), arbClientError) { error ->
            var attemptCount = 0

            val result = withRetry(maxAttempts = 3, delayMs = 1) {
                attemptCount++
                Result.failure<Unit>(error)
            }

            result.isFailure shouldBe true
            attemptCount shouldBe 1
        }
    }

    test("Property 21: Transient errors cause retries — more than 1 attempt") {
        checkAll(PropTestConfig(iterations = 100), arbTransientError) { error ->
            var attemptCount = 0

            withRetry(maxAttempts = 3, delayMs = 1) {
                attemptCount++
                Result.failure<Unit>(error)
            }

            attemptCount shouldBeGreaterThanOrEqual 2
        }
    }

    // -- Property 22: Final Error Preservation --

    test("Property 22: Exhausted retry throws the error from the last attempt") {
        checkAll(PropTestConfig(iterations = 100), Arb.string(1..50)) { uniqueMsg ->
            var attemptCount = 0

            val result = withRetry(maxAttempts = 3, delayMs = 1) {
                attemptCount++
                Result.failure<Unit>(
                    NetworkException.ServerError(500, "attempt-$attemptCount-$uniqueMsg")
                )
            }

            result.isFailure shouldBe true
            val exception = result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ServerError>()
            exception.message shouldBe "attempt-3-$uniqueMsg"
        }
    }
})
