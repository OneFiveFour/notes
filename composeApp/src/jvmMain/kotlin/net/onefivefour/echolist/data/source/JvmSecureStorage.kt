package net.onefivefour.echolist.data.source

import net.onefivefour.echolist.platform.echoListSecureStoragePath
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.READ
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.WRITE
import java.util.Properties

/**
 * JVM desktop SecureStorage backed by a local properties file.
 */
class JvmSecureStorage(
    private val storagePath: Path = echoListSecureStoragePath()
) : SecureStorage {

    private val properties = Properties()

    init {
        storagePath.parent?.let(Files::createDirectories)
        loadFromDisk()
    }

    @Synchronized
    override fun get(key: String): String? = properties.getProperty(key)

    @Synchronized
    override fun put(key: String, value: String) {
        properties.setProperty(key, value)
        persist()
    }

    @Synchronized
    override fun delete(key: String) {
        properties.remove(key)
        persist()
    }

    private fun loadFromDisk() {
        if (!Files.exists(storagePath)) return

        try {
            Files.newInputStream(storagePath, READ).use(properties::load)
        } catch (exception: IOException) {
            throw IllegalStateException("Failed to read desktop secure storage at $storagePath", exception)
        }
    }

    private fun persist() {
        try {
            storagePath.parent?.let(Files::createDirectories)
            Files.newOutputStream(storagePath, CREATE, WRITE, TRUNCATE_EXISTING).use { output ->
                properties.store(output, "EchoList desktop secure storage")
            }
        } catch (exception: IOException) {
            throw IllegalStateException("Failed to persist desktop secure storage at $storagePath", exception)
        }
    }
}
