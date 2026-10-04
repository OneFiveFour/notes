package net.onefivefour.echolist.core.session.data.storage

import net.onefivefour.echolist.core.session.domain.SecureStorage

import kotlinx.browser.localStorage

class JsSecureStorage : SecureStorage {

    override fun get(key: String): String? = localStorage.getItem(key)

    override fun put(key: String, value: String) {
        localStorage.setItem(key, value)
    }

    override fun delete(key: String) {
        localStorage.removeItem(key)
    }
}
