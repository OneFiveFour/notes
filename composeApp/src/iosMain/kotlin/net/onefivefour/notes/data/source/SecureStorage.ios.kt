package net.onefivefour.notes.data.source

/**
 * iOS SecureStorage stub — uses in-memory map.
 * TODO: Replace with Keychain-backed implementation.
 */
class IosSecureStorage : SecureStorage {

    private val store = mutableMapOf<String, String>()

    override fun get(key: String): String? = store[key]

    override fun put(key: String, value: String) {
        store[key] = value
    }

    override fun delete(key: String) {
        store.remove(key)
    }
}
