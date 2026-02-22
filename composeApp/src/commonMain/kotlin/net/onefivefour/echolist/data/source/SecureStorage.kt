package net.onefivefour.echolist.data.source

interface SecureStorage {
    fun get(key: String): String?
    fun put(key: String, value: String)
    fun delete(key: String)
}

object StorageKeys {
    const val ACCESS_TOKEN = "access_token"
    const val REFRESH_TOKEN = "refresh_token"
    const val BACKEND_URL = "backend_url"
}
