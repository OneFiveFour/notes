package net.onefivefour.echolist.data.source

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.nio.file.Files

class JvmSecureStorageTest : FunSpec({

    test("stored values survive a new storage instance") {
        val tempDir = Files.createTempDirectory("jvm-secure-storage")
        val storagePath = tempDir.resolve("secure-store.properties")

        JvmSecureStorage(storagePath).put(StorageKeys.ACCESS_TOKEN, "token-123")

        JvmSecureStorage(storagePath).get(StorageKeys.ACCESS_TOKEN) shouldBe "token-123"
    }

    test("delete removes values from persisted storage") {
        val tempDir = Files.createTempDirectory("jvm-secure-storage")
        val storagePath = tempDir.resolve("secure-store.properties")
        val storage = JvmSecureStorage(storagePath)

        storage.put(StorageKeys.BACKEND_URL, "http://localhost:9090")
        storage.delete(StorageKeys.BACKEND_URL)

        JvmSecureStorage(storagePath).get(StorageKeys.BACKEND_URL).shouldBeNull()
    }

    test("put overwrites the existing persisted value") {
        val tempDir = Files.createTempDirectory("jvm-secure-storage")
        val storagePath = tempDir.resolve("secure-store.properties")
        val storage = JvmSecureStorage(storagePath)

        storage.put(StorageKeys.REFRESH_TOKEN, "first")
        storage.put(StorageKeys.REFRESH_TOKEN, "second")

        JvmSecureStorage(storagePath).get(StorageKeys.REFRESH_TOKEN) shouldBe "second"
    }
})
