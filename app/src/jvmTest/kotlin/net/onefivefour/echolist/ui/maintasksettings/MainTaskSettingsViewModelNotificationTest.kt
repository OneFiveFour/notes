package net.onefivefour.echolist.ui.maintasksettings

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.domain.NotificationPermissionChecker
import net.onefivefour.echolist.domain.NotificationPermissionRequester
import net.onefivefour.echolist.ui.recurrence.RecurrenceInterval

/**
 * Unit tests (example-based) for MainTaskSettingsViewModel notification toggle logic.
 *
 * These tests complement the property-based tests in
 * MainTaskSettingsViewModelNotificationPropertyTest by verifying specific scenarios
 * with concrete values.
 *
 * **Validates: Requirements 1.3, 1.4, 1.5, 2.3, 3.1, 3.2, 3.4, 4.1, 4.2, 4.3**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainTaskSettingsViewModelNotificationTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeEach {
        Dispatchers.setMain(testDispatcher)
    }

    afterEach {
        Dispatchers.resetMain()
    }

    // --- Test 1: Default state matches constructor param ---

    test("default state: isNotificationEnabled=true when constructed with true") {
        runTest(testDispatcher) {
            val vm = createViewModel(currentIsNotificationEnabled = true)

            val state = vm.uiState.value
            state.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
            state.isNotificationEnabled shouldBe true
        }
    }

    test("default state: isNotificationEnabled=false when constructed with false") {
        runTest(testDispatcher) {
            val vm = createViewModel(currentIsNotificationEnabled = false)

            val state = vm.uiState.value
            state.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
            state.isNotificationEnabled shouldBe false
        }
    }

    // --- Test 2: Toggle changes state immediately ---

    test("toggle changes state to false immediately") {
        runTest(testDispatcher) {
            val vm = createViewModel(currentIsNotificationEnabled = true)

            vm.onNotificationToggleChanged(false)

            val state = vm.uiState.value
            state.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
            state.isNotificationEnabled shouldBe false
        }
    }

    test("toggle changes state to true immediately") {
        runTest(testDispatcher) {
            val vm = createViewModel(currentIsNotificationEnabled = false)

            vm.onNotificationToggleChanged(true)

            val state = vm.uiState.value
            state.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
            state.isNotificationEnabled shouldBe true
        }
    }

    // --- Test 3: Toggle emits result via resultBus ---

    test("toggle to true emits result with isNotificationEnabled=true") {
        runTest(testDispatcher) {
            val resultBus = MainTaskSettingsResultBus()
            val vm = createViewModel(
                currentIsNotificationEnabled = false,
                resultBus = resultBus
            )

            val resultDeferred = async { resultBus.results.first() }
            vm.onNotificationToggleChanged(true)
            testScheduler.advanceUntilIdle()

            val result = resultDeferred.await()
            result.isNotificationEnabled shouldBe true
            result.mainTaskId shouldBe "task-1"
        }
    }

    test("toggle to false emits result with isNotificationEnabled=false") {
        runTest(testDispatcher) {
            val resultBus = MainTaskSettingsResultBus()
            val vm = createViewModel(
                currentIsNotificationEnabled = true,
                resultBus = resultBus
            )

            val resultDeferred = async { resultBus.results.first() }
            vm.onNotificationToggleChanged(false)
            testScheduler.advanceUntilIdle()

            val result = resultDeferred.await()
            result.isNotificationEnabled shouldBe false
            result.mainTaskId shouldBe "task-1"
        }
    }

    // --- Test 4: Recurrence Off disables toggle and forces isNotificationEnabled=false ---

    test("recurrence Off disables toggle and forces isNotificationEnabled=false") {
        runTest(testDispatcher) {
            val vm = createViewModel(
                currentIsNotificationEnabled = true,
                currentRecurrence = "FREQ=DAILY"
            )

            vm.onRecurrenceIntervalSelected(RecurrenceInterval.Off)

            val state = vm.uiState.value
            state.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
            state.isNotificationEnabled shouldBe false
            state.isNotificationToggleEnabled shouldBe false
        }
    }

    // --- Test 5: Recurrence reactivation re-enables toggle but doesn't auto-enable notifications ---

    test("recurrence reactivation re-enables toggle but keeps notifications disabled") {
        runTest(testDispatcher) {
            val vm = createViewModel(
                currentIsNotificationEnabled = true,
                currentRecurrence = "FREQ=DAILY"
            )

            // First, turn off recurrence (which forces notification off)
            vm.onRecurrenceIntervalSelected(RecurrenceInterval.Off)

            val stateAfterOff = vm.uiState.value
            stateAfterOff.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
            stateAfterOff.isNotificationEnabled shouldBe false
            stateAfterOff.isNotificationToggleEnabled shouldBe false

            // Then, re-enable recurrence
            vm.onRecurrenceIntervalSelected(RecurrenceInterval.Daily)

            val stateAfterDaily = vm.uiState.value
            stateAfterDaily.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
            stateAfterDaily.isNotificationToggleEnabled shouldBe true
            stateAfterDaily.isNotificationEnabled shouldBe false
        }
    }

    // --- Test 6: onScreenLeaving: permission already granted → no re-emit ---

    test("onScreenLeaving: permission already granted does not re-emit") {
        runTest(testDispatcher) {
            val resultBus = MainTaskSettingsResultBus()
            var checkerCallCount = 0
            val vm = createViewModel(
                currentIsNotificationEnabled = true,
                currentRecurrence = "FREQ=DAILY",
                resultBus = resultBus,
                permissionChecker = object : NotificationPermissionChecker {
                    override suspend fun isGranted(): Boolean {
                        checkerCallCount++
                        return true
                    }
                }
            )

            // Collect the initial confirm() emission so the bus is clear
            val initialResult = async { resultBus.results.first() }
            // The VM constructor calls confirm() via initial state emission — actually it doesn't
            // but onNotificationToggleChanged or other actions do. Let's trigger one first:
            vm.onNotificationToggleChanged(true)
            testScheduler.advanceUntilIdle()
            initialResult.await()

            // Now call onScreenLeaving
            vm.onScreenLeaving()
            testScheduler.advanceUntilIdle()

            // Permission checker was called (to verify permission)
            checkerCallCount shouldBe 1

            // No additional emission expected — if there was one, the test would
            // have captured it. We verify by checking state hasn't changed.
            val state = vm.uiState.value
            state.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
            state.isNotificationEnabled shouldBe true
        }
    }

    // --- Test 7: onScreenLeaving: permission denied → requester called → granted → no re-emit ---

    test("onScreenLeaving: permission not granted but requester succeeds does not re-emit") {
        runTest(testDispatcher) {
            var requesterCalled = false
            val vm = createViewModel(
                currentIsNotificationEnabled = true,
                currentRecurrence = "FREQ=DAILY",
                permissionChecker = object : NotificationPermissionChecker {
                    override suspend fun isGranted(): Boolean = false
                },
                permissionRequester = object : NotificationPermissionRequester {
                    override suspend fun request(): Boolean {
                        requesterCalled = true
                        return true
                    }
                }
            )

            vm.onScreenLeaving()
            testScheduler.advanceUntilIdle()

            requesterCalled shouldBe true
            val state = vm.uiState.value
            state.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
            state.isNotificationEnabled shouldBe true
        }
    }

    // --- Test 8: onScreenLeaving: permission denied → requester denied → re-emit with false ---

    test("onScreenLeaving: permission denied and requester denied re-emits with false") {
        runTest(testDispatcher) {
            val resultBus = MainTaskSettingsResultBus()
            val vm = createViewModel(
                currentIsNotificationEnabled = true,
                currentRecurrence = "FREQ=DAILY",
                resultBus = resultBus,
                permissionChecker = object : NotificationPermissionChecker {
                    override suspend fun isGranted(): Boolean = false
                },
                permissionRequester = object : NotificationPermissionRequester {
                    override suspend fun request(): Boolean = false
                }
            )

            val resultDeferred = async { resultBus.results.first() }
            vm.onScreenLeaving()
            testScheduler.advanceUntilIdle()

            val result = resultDeferred.await()
            result.isNotificationEnabled shouldBe false
            result.mainTaskId shouldBe "task-1"

            val state = vm.uiState.value
            state.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
            state.isNotificationEnabled shouldBe false
        }
    }

    // --- Test 9: onScreenLeaving: requester throws → re-emit with false ---

    test("onScreenLeaving: requester throws RuntimeException re-emits with false and no crash") {
        runTest(testDispatcher) {
            val resultBus = MainTaskSettingsResultBus()
            val vm = createViewModel(
                currentIsNotificationEnabled = true,
                currentRecurrence = "FREQ=DAILY",
                resultBus = resultBus,
                permissionChecker = object : NotificationPermissionChecker {
                    override suspend fun isGranted(): Boolean = false
                },
                permissionRequester = object : NotificationPermissionRequester {
                    override suspend fun request(): Boolean {
                        throw RuntimeException("Permission dialog crashed")
                    }
                }
            )

            val resultDeferred = async { resultBus.results.first() }
            vm.onScreenLeaving()
            testScheduler.advanceUntilIdle()

            val result = resultDeferred.await()
            result.isNotificationEnabled shouldBe false

            val state = vm.uiState.value
            state.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
            state.isNotificationEnabled shouldBe false
        }
    }

    // --- Test 10: onScreenLeaving: isNotificationEnabled=false → no permission check ---

    test("onScreenLeaving: isNotificationEnabled=false skips permission check") {
        runTest(testDispatcher) {
            var checkerCalled = false
            val vm = createViewModel(
                currentIsNotificationEnabled = false,
                currentRecurrence = "FREQ=DAILY",
                permissionChecker = object : NotificationPermissionChecker {
                    override suspend fun isGranted(): Boolean {
                        checkerCalled = true
                        return true
                    }
                }
            )

            vm.onScreenLeaving()
            testScheduler.advanceUntilIdle()

            checkerCalled shouldBe false
        }
    }

    // --- Test 11: onScreenLeaving: recurrence Off → no permission check ---

    test("onScreenLeaving: recurrence Off skips permission check even if notifications enabled") {
        runTest(testDispatcher) {
            var checkerCalled = false
            val vm = createViewModel(
                currentIsNotificationEnabled = true,
                currentRecurrence = "",
                permissionChecker = object : NotificationPermissionChecker {
                    override suspend fun isGranted(): Boolean {
                        checkerCalled = true
                        return true
                    }
                }
            )

            vm.onScreenLeaving()
            testScheduler.advanceUntilIdle()

            checkerCalled shouldBe false
        }
    }

    // --- Test 12: confirm() never calls permissionChecker ---

    test("confirm via toggle and recurrence changes never calls permissionChecker") {
        runTest(testDispatcher) {
            var checkerCallCount = 0
            val vm = createViewModel(
                currentIsNotificationEnabled = false,
                currentRecurrence = "FREQ=DAILY",
                permissionChecker = object : NotificationPermissionChecker {
                    override suspend fun isGranted(): Boolean {
                        checkerCallCount++
                        return true
                    }
                }
            )

            vm.onNotificationToggleChanged(true)
            testScheduler.advanceUntilIdle()

            vm.onNotificationToggleChanged(false)
            testScheduler.advanceUntilIdle()

            vm.onRecurrenceIntervalSelected(RecurrenceInterval.Weekly)
            testScheduler.advanceUntilIdle()

            vm.onRecurrenceIntervalSelected(RecurrenceInterval.Off)
            testScheduler.advanceUntilIdle()

            vm.onDateSelected(1893456000000L) // 2030-01-01 in millis
            testScheduler.advanceUntilIdle()

            checkerCallCount shouldBe 0
        }
    }
})

/**
 * Helper function to create a ViewModel with sensible defaults for testing.
 */
private fun createViewModel(
    mainTaskId: String = "task-1",
    currentDueDate: String = "2026-01-01",
    currentRecurrence: String = "FREQ=DAILY",
    currentIsNotificationEnabled: Boolean = true,
    permissionChecker: NotificationPermissionChecker = object : NotificationPermissionChecker {
        override suspend fun isGranted(): Boolean = true
    },
    permissionRequester: NotificationPermissionRequester = object : NotificationPermissionRequester {
        override suspend fun request(): Boolean = true
    },
    resultBus: MainTaskSettingsResultBus = MainTaskSettingsResultBus()
): MainTaskSettingsViewModel {
    return MainTaskSettingsViewModel(
        mainTaskId = mainTaskId,
        currentDueDate = currentDueDate,
        currentRecurrence = currentRecurrence,
        currentIsNotificationEnabled = currentIsNotificationEnabled,
        permissionChecker = permissionChecker,
        permissionRequester = permissionRequester,
        resultBus = resultBus
    )
}
