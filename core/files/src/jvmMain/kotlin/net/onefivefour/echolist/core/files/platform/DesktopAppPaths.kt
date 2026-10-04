package net.onefivefour.echolist.core.files.platform

import java.nio.file.Files
import java.nio.file.Path

fun echoListAppDirectory(): Path {
    val appData = System.getenv("APPDATA")?.takeIf { it.isNotBlank() }
    val directory = if (appData != null) {
        Path.of(appData, "EchoList")
    } else {
        Path.of(System.getProperty("user.home"), ".echolist")
    }

    Files.createDirectories(directory)
    return directory
}

fun echoListDatabasePath(): Path = echoListAppDirectory().resolve("echolist.db")

fun echoListSecureStoragePath(): Path = echoListAppDirectory().resolve("secure-store.properties")