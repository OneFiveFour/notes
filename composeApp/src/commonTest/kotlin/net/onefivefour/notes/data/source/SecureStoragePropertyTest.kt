package net.onefivefour.notes.data.source

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property 7: SecureStorage put/get/delete round-trip
 *
 * *For any* key-value string pair (k, v):
 * (a) put(k, v) then get(k) returns v
 * (b) put(k, v) then delete(k) then get(k) returns null
 * (c) put(k, v1) then put(k, v2) then get(k) returns v2
 *
 * **Validates: Requirements 3.4, 3.5, 3.6**
 */
class SecureStoragePropertyTest : FunSpec({

    test("Property 7a: put then get returns the stored value").config(invocations = 20) {
        checkAll(Arb.string(1..50), Arb.string(0..100)) { key, value ->
            val storage = FakeSecureStorage()
            storage.put(key, value)
            storage.get(key) shouldBe value
        }
    }

    test("Property 7b: put then delete then get returns null").config(invocations = 20) {
        checkAll(Arb.string(1..50), Arb.string(0..100)) { key, value ->
            val storage = FakeSecureStorage()
            storage.put(key, value)
            storage.delete(key)
            storage.get(key).shouldBeNull()
        }
    }

    test("Property 7c: put twice then get returns the second value").config(invocations = 20) {
        checkAll(Arb.string(1..50), Arb.string(0..100), Arb.string(0..100)) { key, v1, v2 ->
            val storage = FakeSecureStorage()
            storage.put(key, v1)
            storage.put(key, v2)
            storage.get(key) shouldBe v2
        }
    }
})
