package net.onefivefour.notes.data.source

import kotlinx.browser.localStorage

class WasmJsSecureStorage : SecureStorage {

    override fun get(key: String): String? = localStorage.getItem(key)

    override fun put(key: String, value: String) {
        localStorage.setItem(key, value)
    }

    override fun delete(key: String) {
        localStorage.removeItem(key)
    }
}
