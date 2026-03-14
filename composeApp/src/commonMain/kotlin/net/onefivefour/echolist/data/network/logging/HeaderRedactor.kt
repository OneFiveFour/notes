package net.onefivefour.echolist.data.network.logging

object HeaderRedactor {
    private const val REDACTED = "[REDACTED]"

    /**
     * Returns a redacted copy of [headers].
     * Sensitive headers: "Authorization" (case-insensitive) or
     * any header name containing "token" (case-insensitive).
     */
    fun redact(headers: Map<String, List<String>>): Map<String, List<String>> {
        return headers.mapValues { (name, values) ->
            if (isSensitive(name)) listOf(REDACTED) else values
        }
    }

    internal fun isSensitive(name: String): Boolean {
        return name.equals("Authorization", ignoreCase = true) ||
            name.contains("token", ignoreCase = true)
    }
}
