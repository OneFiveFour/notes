package net.onefivefour.echolist.network.client

import kotlinx.coroutines.delay
import net.onefivefour.echolist.network.error.NetworkException

/**
 * Executes [block] with retry logic using exponential backoff.
 *
 * Retries only on transient errors: [NetworkException.NetworkError],
 * [NetworkException.ServerError], and [NetworkException.TimeoutError].
 * Does NOT retry on [NetworkException.ClientError] or [NetworkException.SerializationError].
 *
 * @param maxAttempts Total number of attempts (1 initial + retries). Must be >= 1.
 * @param delayMs Base delay in milliseconds for exponential backoff (delayMs * 2^attempt).
 * @param block The suspending block to execute and potentially retry.
 * @return The [Result] from the first successful attempt, or the last failure.
 */
internal suspend fun <T> withRetry(
    maxAttempts: Int = 3,
    delayMs: Long = 1_000,
    block: suspend () -> Result<T>
): Result<T> {
    require(maxAttempts >= 1) { "maxAttempts must be at least 1" }

    var lastResult: Result<T>? = null

    for (attempt in 0 until maxAttempts) {
        val result = block()

        if (result.isSuccess) {
            return result
        }

        lastResult = result

        val exception = result.exceptionOrNull()

        val shouldRetry = when (exception) {
            is NetworkException.NetworkError -> true
            is NetworkException.ServerError -> true
            is NetworkException.TimeoutError -> true
            else -> false
        }

        if (!shouldRetry) {
            return result
        }

        // Don't delay after the last attempt
        if (attempt < maxAttempts - 1) {
            delay(delayMs * (1L shl attempt))
        }
    }

    return lastResult ?: Result.failure(Exception("Unknown error"))
}
