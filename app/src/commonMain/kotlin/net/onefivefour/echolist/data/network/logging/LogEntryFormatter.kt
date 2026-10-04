package net.onefivefour.echolist.data.network.logging

internal object LogEntryFormatter {

    fun formatRequest(
        method: String,
        url: String,
        headers: Map<String, List<String>>,
        body: ByteArray
    ): String {
        val redactedHeaders = HeaderRedactor.redact(headers)
        val formattedBody = BodyFormatter.format(body)
        return buildString {
            appendLine("--> $method $url")
            appendLine("Headers: $redactedHeaders")
            append("Body: $formattedBody")
        }
    }

    fun formatResponse(
        statusCode: Int,
        headers: Map<String, List<String>>,
        body: ByteArray,
        elapsedMs: Long
    ): String {
        val redactedHeaders = HeaderRedactor.redact(headers)
        val formattedBody = BodyFormatter.format(body)
        return buildString {
            appendLine("<-- $statusCode (${elapsedMs}ms)")
            appendLine("Headers: $redactedHeaders")
            append("Body: $formattedBody")
        }
    }

    fun formatError(url: String, errorMessage: String): String {
        return buildString {
            appendLine("<-- ERROR $url")
            append("Exception: $errorMessage")
        }
    }
}
