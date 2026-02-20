package net.onefivefour.notes.data.source

/**
 * In-memory [SecureStorage] implementation for use in common tests.
 */
class FakeSecureStorage : SecureStorage {

    private val store = mutableMapOf<String, String>()

    override fun get(key: String): String? = store[key]

    override fun put(key: String, value: String) {
        store[key] = value
    }

    override fun delete(key: String) {
        store.remove(key)
    }
}
