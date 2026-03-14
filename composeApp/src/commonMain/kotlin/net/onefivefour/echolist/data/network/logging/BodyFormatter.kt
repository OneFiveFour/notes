package net.onefivefour.echolist.data.network.logging

object BodyFormatter {
    private const val MAX_BYTES = 1024

    /**
     * Formats [bytes] as a UTF-8 string for logging.
     * - Empty array → "empty body"
     * - ≤1024 bytes → decoded string + "(N bytes)"
     * - >1024 bytes → first 1024 bytes decoded + "... (truncated, N bytes total)"
     */
    fun format(bytes: ByteArray): String {
        if (bytes.isEmpty()) return "empty body"

        val totalSize = bytes.size
        val isTruncated = totalSize > MAX_BYTES
        val bytesToFormat = if (isTruncated) bytes.copyOf(MAX_BYTES) else bytes

        val text = bytesToFormat.decodeToString()

        return if (isTruncated) {
            "$text ... (truncated, $totalSize bytes total)"
        } else {
            "$text ($totalSize bytes)"
        }
    }
}
