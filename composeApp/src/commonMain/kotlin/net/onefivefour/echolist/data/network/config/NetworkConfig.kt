package net.onefivefour.echolist.data.network.config

data class NetworkConfig(
    val baseUrl: String,
    val requestTimeoutMs: Long = 5_000,
    val connectTimeoutMs: Long = 2_000,
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 1_000
) {
    companion object {
        fun default(baseUrl: String) = NetworkConfig(baseUrl = baseUrl)
    }
}