package net.onefivefour.echolist.core.networking.domain

fun interface BackendUrlStore {
    fun getBackendUrl(): String?
}
