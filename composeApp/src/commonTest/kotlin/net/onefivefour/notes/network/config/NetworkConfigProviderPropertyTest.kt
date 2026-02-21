package net.onefivefour.notes.network.config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.notes.data.source.FakeSecureStorage
import net.onefivefour.notes.data.source.StorageKeys

/**
 * Property 12: NetworkConfig uses correct backend URL
 *
 * *For any* backend URL string, after a successful login or app startup with stored tokens,
 * NetworkConfig.baseUrl should equal that backend URL.
 *
 * **Validates: Requirements 8.1, 8.2**
 */
class NetworkConfigProviderPropertyTest : FunSpec({

    test("Property 12a: startup with stored URL initializes config from storage") {
        checkAll(PropTestConfig(iterations = 100), Arb.string(1..100)) { url ->
            val storage = FakeSecureStorage()
            storage.put(StorageKeys.BACKEND_URL, url)

            val provider = NetworkConfigProvider(secureStorage = storage)

            provider.config.baseUrl shouldBe url
        }
    }

    test("Property 12b: updateBaseUrl changes config to the provided URL") {
        checkAll(PropTestConfig(iterations = 100), Arb.string(1..100)) { url ->
            val storage = FakeSecureStorage()
            val provider = NetworkConfigProvider(secureStorage = storage)

            provider.updateBaseUrl(url)

            provider.config.baseUrl shouldBe url
        }
    }

    test("Property 12c: startup without stored URL uses default") {
        val storage = FakeSecureStorage()
        val provider = NetworkConfigProvider(secureStorage = storage)

        provider.config.baseUrl shouldBe NetworkConfigProvider.DEFAULT_BASE_URL
    }
})
