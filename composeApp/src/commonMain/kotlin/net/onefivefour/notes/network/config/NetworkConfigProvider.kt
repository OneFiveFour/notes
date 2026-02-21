package net.onefivefour.notes.network.config

import net.onefivefour.notes.data.source.SecureStorage
import net.onefivefour.notes.data.source.StorageKeys

/**
 * Provides the current [NetworkConfig] for the app.
 *
 * On construction, reads the stored backend URL from [SecureStorage].
 * After a successful login, call [updateBaseUrl] to point the network
 * layer at the user-provided backend.
 */
class NetworkConfigProvider(
    private val secureStorage: SecureStorage,
    private val defaultBaseUrl: String = DEFAULT_BASE_URL
) {

    companion object {
        const val DEFAULT_BASE_URL = "http://localhost:9090"
    }

    var config: NetworkConfig = buildConfig(
        secureStorage.get(StorageKeys.BACKEND_URL) ?: defaultBaseUrl
    )
        private set

    /**
     * Updates the base URL used by the network layer.
     * Called after a successful login or when the backend URL changes.
     */
    fun updateBaseUrl(baseUrl: String) {
        config = buildConfig(baseUrl)
    }

    private fun buildConfig(baseUrl: String): NetworkConfig {
        return NetworkConfig(baseUrl = baseUrl)
    }
}
