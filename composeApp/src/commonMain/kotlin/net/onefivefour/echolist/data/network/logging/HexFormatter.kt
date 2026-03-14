package net.onefivefour.echolist.data.network.logging

object HexFormatter {
    private const val MAX_BYTES = 1024

    /**
     * Formats [bytes] as space-separated uppercase hex octets.
     * - Empty array → "empty body"
     * - ≤1024 bytes → "0A 1B 2C (3 bytes)"
     * - >1024 bytes → first 1024 octets + "... (truncated, N bytes total)"
     */
    fun format(bytes: ByteArray): String {
        if (bytes.isEmpty()) return "empty body"

        val totalSize = bytes.size
        val isTruncated = totalSize > MAX_BYTES
        val bytesToFormat = if (isTruncated) bytes.copyOf(MAX_BYTES) else bytes

        val hex = bytesToFormat.joinToString(" ") { byte ->
            byte.toInt().and(0xFF).toString(16).uppercase().padStart(2, '0')
        }

        return if (isTruncated) {
            "$hex ... (truncated, $totalSize bytes total)"
        } else {
            "$hex ($totalSize bytes)"
        }
    }
}
