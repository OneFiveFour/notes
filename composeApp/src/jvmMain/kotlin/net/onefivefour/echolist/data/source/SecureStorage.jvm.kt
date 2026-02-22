package net.onefivefour.echolist.data.source

/**
 * JVM Desktop SecureStorage stub — uses in-memory map.
 * TODO: Replace with a file-backed or keystore-backed implementation.
 */
class JvmSecureStorage : SecureStorage {

    private val store = mutableMapOf<String, String>()

    override fun get(key: String): String? = store[key]

    override fun put(key: String, value: String) {
        store[key] = value
    }

    override fun delete(key: String) {
        store.remove(key)
    }
}
