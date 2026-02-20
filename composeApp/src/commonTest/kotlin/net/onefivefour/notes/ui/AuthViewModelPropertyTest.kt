package net.onefivefour.notes.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.notes.data.source.FakeSecureStorage
import net.onefivefour.notes.data.source.StorageKeys
import net.onefivefour.notes.network.auth.AuthEvent

/**
 * Property 11: AuthState reflects storage
 *
 * *For any* SecureStorage state, AuthState should be Authenticated
 * if and only if SecureStorage.get(ACCESS_TOKEN) returns a non-null value;
 * otherwise it should be Unauthenticated.
 *
 * **Validates: Requirements 6.1, 6.2**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    test("Property 11: AuthState is Authenticated when access token is present") {
        checkAll(PropTestConfig(iterations = 100), Arb.string(1..100)) { token ->
            runTest(testDispatcher) {
                val storage = FakeSecureStorage()
                storage.put(StorageKeys.ACCESS_TOKEN, token)
                val authEvents = MutableSharedFlow<AuthEvent>()

                val vm = AuthViewModel(storage, authEvents)

                vm.authState.value shouldBe AuthState.Authenticated
            }
        }
    }

    test("Property 11: AuthState is Unauthenticated when no access token") {
        checkAll(PropTestConfig(iterations = 100), Arb.boolean()) { _ ->
            runTest(testDispatcher) {
                val storage = FakeSecureStorage()
                // No access token stored
                val authEvents = MutableSharedFlow<AuthEvent>()

                val vm = AuthViewModel(storage, authEvents)

                vm.authState.value shouldBe AuthState.Unauthenticated
            }
        }
    }

    test("Property 11: ReAuthRequired transitions to Unauthenticated") {
        checkAll(PropTestConfig(iterations = 100), Arb.string(1..100)) { token ->
            runTest(testDispatcher) {
                val storage = FakeSecureStorage()
                storage.put(StorageKeys.ACCESS_TOKEN, token)
                val authEvents = MutableSharedFlow<AuthEvent>()

                val vm = AuthViewModel(storage, authEvents)
                // Let the viewModelScope coroutine start collecting
                testScheduler.advanceUntilIdle()
                vm.authState.value shouldBe AuthState.Authenticated

                authEvents.emit(AuthEvent.ReAuthRequired)
                testScheduler.advanceUntilIdle()

                vm.authState.value shouldBe AuthState.Unauthenticated
            }
        }
    }
})
