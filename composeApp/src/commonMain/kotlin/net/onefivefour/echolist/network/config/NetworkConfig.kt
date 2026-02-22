package net.onefivefour.echolist.network.config

data class NetworkConfig(
    val baseUrl: String,
    val requestTimeoutMs: Long = 30_000,
    val connectTimeoutMs: Long = 10_000,
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 1_000
) {
    companion object {
        fun default(baseUrl: String) = NetworkConfig(baseUrl = baseUrl)
    }
}
